package org.folio.qm.converter;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

import static org.folio.qm.converter.Constants.ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD;
import static org.folio.qm.converter.Constants.BLANK_REPLACEMENT;
import static org.folio.qm.converter.Constants.BLVL;
import static org.folio.qm.converter.Constants.BLVL_LEADER_POS;
import static org.folio.qm.converter.Constants.DESC;
import static org.folio.qm.converter.Constants.DESC_LEADER_POS;
import static org.folio.qm.converter.Constants.ELVL;
import static org.folio.qm.converter.Constants.ELVL_LEADER_POS;
import static org.folio.qm.converter.Constants.GENERAL_INFORMATION_CONTROL_FIELD;
import static org.folio.qm.converter.Constants.PHYSICAL_DESCRIPTIONS_CONTROL_FIELD;
import static org.folio.qm.converter.Constants.SPECIFIC_ELEMENTS_BEGIN_INDEX;
import static org.folio.qm.converter.Constants.SPECIFIC_ELEMENTS_END_INDEX;
import static org.folio.qm.converter.Constants.TYPE;
import static org.folio.qm.converter.Constants.TYPE_OF_RECORD_LEADER_POS;
import static org.folio.qm.converter.elements.FixedLengthDataElements.VALUE;
import static org.folio.qm.converter.elements.MaterialTypeConfiguration.UNKNOWN;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.marc4j.MarcJsonReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import org.folio.qm.converter.elements.FixedLengthDataElements;
import org.folio.qm.converter.elements.MaterialTypeConfiguration;
import org.folio.qm.converter.elements.PhysicalDescriptionFixedFieldElements;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.QuickMarcFields;
import org.folio.qm.domain.dto.QuickMarcUpdateInfo;
import org.folio.qm.exception.ConverterException;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

@Component
public class ParsedRecordDtoToQuickMarcConverter implements Converter<ParsedRecordDto, QuickMarc> {

  private MaterialTypeConfiguration materialTypeConfiguration;

  @Override
  public QuickMarc convert(ParsedRecordDto parsedRecordDto) {
    ParsedRecord parsedRecord = parsedRecordDto.getParsedRecord();

    try (InputStream input = IOUtils
      .toInputStream(new ObjectMapper().writeValueAsString(parsedRecord), StandardCharsets.UTF_8)) {
      Record marcRecord = new MarcJsonReader(input).next();
      String leader = masqueradeBlanks(marcRecord.getLeader().marshal());
      materialTypeConfiguration = MaterialTypeConfiguration.resolveContentType(leader);

      List<QuickMarcFields> fields = marcRecord.getControlFields()
        .stream()
        .map(cf -> new QuickMarcFields().tag(cf.getTag()).content(processControlField(cf, leader)))
        .collect(Collectors.toList());

      fields.addAll(marcRecord.getDataFields()
        .stream()
        .map(this::dataFieldToQuickMarcField)
        .collect(Collectors.toList()));

      return new QuickMarc().parsedRecordId(parsedRecord.getId())
        .leader(leader)
        .fields(fields)
        .parsedRecordDtoId(parsedRecordDto.getId())
        .instanceId(parsedRecordDto.getExternalIdsHolder().getInstanceId())
        .suppressDiscovery(parsedRecordDto.getAdditionalInfo().getSuppressDiscovery())
        .updateInfo(new QuickMarcUpdateInfo()
          .recordState(QuickMarcUpdateInfo.RecordStateEnum.fromValue(parsedRecordDto.getRecordState().value()))
          .updateDate(parsedRecordDto.getMetadata().getUpdatedDate()));
    } catch (Exception e) {
      throw new ConverterException(e, this.getClass());
    }
  }

  private Object processControlField(ControlField controlField, String leader) {
    switch (controlField.getTag()) {
      case ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD:
        return splitAdditionalCharacteristicsControlField(masqueradeBlanks(controlField.getData()));
      case PHYSICAL_DESCRIPTIONS_CONTROL_FIELD:
        return splitPhysicalDescriptionsControlField(masqueradeBlanks(controlField.getData()));
      case GENERAL_INFORMATION_CONTROL_FIELD:
        return splitGeneralInformationControlField(masqueradeBlanks(controlField.getData()), leader);
      default:
        return controlField.getData();
    }
  }

  private Object splitAdditionalCharacteristicsControlField(String content) {
    if (materialTypeConfiguration.equals(UNKNOWN)) {
      return Collections.singletonMap(VALUE.getName(), content);
    }
    return fillContentMap(materialTypeConfiguration.getFixedLengthControlFieldItems(), content.substring(1));
  }

  private Map<String, Object> splitGeneralInformationControlField(String content, String leader) {
    Map<String, Object> fieldItems = new LinkedHashMap<>();
    fieldItems.put(TYPE, leader.charAt(TYPE_OF_RECORD_LEADER_POS));
    fieldItems.put(BLVL, leader.charAt(BLVL_LEADER_POS));
    fieldItems.put(ELVL, leader.charAt(ELVL_LEADER_POS));
    fieldItems.put(DESC, leader.charAt(DESC_LEADER_POS));
    fieldItems.putAll(fillContentMap(MaterialTypeConfiguration.getCommonItems(), content));
    fieldItems.putAll(fillContentMap(materialTypeConfiguration.getFixedLengthControlFieldItems(),
      content.substring(SPECIFIC_ELEMENTS_BEGIN_INDEX, SPECIFIC_ELEMENTS_END_INDEX)));
    return fieldItems;
  }

  private Map<String, Object> splitPhysicalDescriptionsControlField(String content) {
    PhysicalDescriptionFixedFieldElements physicalDescriptionFixedFieldElements = PhysicalDescriptionFixedFieldElements
      .resolveByCode(content.charAt(0));
    return physicalDescriptionFixedFieldElements.getItems()
      .stream()
      .collect(toMap(FixedLengthDataElements::getName,
        element -> (element.getLength() != 0) ? extractElementFromContent(content, element) : content));
  }

  private Map<String, Object> fillContentMap(List<FixedLengthDataElements> items, String content) {
    return items.stream()
      .collect(toMap(FixedLengthDataElements::getName, element -> element.isArray() ?
        Arrays.asList(extractElementFromContent(content, element).split(EMPTY)) :
        extractElementFromContent(content, element)));
  }

  private String extractElementFromContent(String content, FixedLengthDataElements element) {
    return StringUtils.substring(content, element.getPosition(), element.getPosition() + element.getLength());
  }

  private QuickMarcFields dataFieldToQuickMarcField(DataField dataField) {
    return new QuickMarcFields().tag(dataField.getTag())
      .indicators(Arrays.asList(masqueradeBlanks(Character.toString(dataField.getIndicator1())),
        masqueradeBlanks(Character.toString(dataField.getIndicator2()))))
      .content(dataField.getSubfields()
        .stream()
        .map(subfield -> new StringBuilder("$").append(subfield.getCode())
          .append(SPACE)
          .append(subfield.getData()))
        .collect(Collectors.joining(SPACE)));
  }

  private String masqueradeBlanks(String sourceString) {
    return sourceString.replace(SPACE, BLANK_REPLACEMENT);
  }
}
