package org.folio.converter;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.folio.converter.elements.FixedLengthDataElements.VALUE;
import static org.folio.converter.elements.MaterialTypeConfiguration.UNKNOWN;
import static org.folio.converter.Constants.ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD;
import static org.folio.converter.Constants.BLVL;
import static org.folio.converter.Constants.DESC;
import static org.folio.converter.Constants.ELVL;
import static org.folio.converter.Constants.GENERAL_INFORMATION_CONTROL_FIELD;
import static org.folio.converter.Constants.PHYSICAL_DESCRIPTIONS_CONTROL_FIELD;
import static org.folio.converter.Constants.SPECIFIC_ELEMENTS_BEGIN_INDEX;
import static org.folio.converter.Constants.SPECIFIC_ELEMENTS_END_INDEX;
import static org.folio.converter.Constants.TYPE;
import static org.folio.util.ErrorUtils.buildError;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.folio.converter.elements.FixedLengthDataElements;
import org.folio.converter.elements.MaterialTypeConfiguration;
import org.folio.converter.elements.PhysicalDescriptionFixedFieldElements;
import org.folio.exception.ConverterException;
import org.folio.rest.jaxrs.model.Field;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.folio.srs.model.ParsedRecordDto;
import org.folio.util.ErrorUtils;
import org.marc4j.MarcJsonReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import io.vertx.core.json.JsonObject;

@Component
public class ParsedRecordDtoToQuickMarcConverter implements Converter<ParsedRecordDto, QuickMarcJson> {
  private MaterialTypeConfiguration materialTypeConfiguration;

  @Override
  public QuickMarcJson convert(ParsedRecordDto parsedRecordDto) {
    ParsedRecord parsedRecord = parsedRecordDto.getParsedRecord();

    try (InputStream input = IOUtils.toInputStream(JsonObject.mapFrom(parsedRecord).encode(), StandardCharsets.UTF_8)) {
      Record marcRecord = new MarcJsonReader(input).next();
      Leader leader = marcRecord.getLeader();
      materialTypeConfiguration = MaterialTypeConfiguration.resolveContentType(leader);

      List<Field> fields = marcRecord.getControlFields()
        .stream()
        .map(cf -> new Field().withTag(cf.getTag()).withContent(processControlField(cf, leader)))
        .collect(Collectors.toList());

      fields.addAll(marcRecord.getDataFields()
        .stream()
        .map(this::dataFieldToQuickMarcField)
        .collect(Collectors.toList()));

      return new QuickMarcJson().withParsedRecordId(parsedRecord.getId())
        .withLeader(leader.marshal())
        .withFields(fields)
        .withParsedRecordDtoId(parsedRecordDto.getId())
        .withInstanceId(parsedRecordDto.getExternalIdsHolder().getInstanceId())
        .withSuppressDiscovery(parsedRecordDto.getAdditionalInfo().getSuppressDiscovery());
    } catch (IOException e) {
      throw new ConverterException(buildError(ErrorUtils.ErrorType.INTERNAL,"Generic converter error"));
    }
  }

  private Object processControlField(ControlField controlField, Leader leader) {
    switch (controlField.getTag()) {
    case ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD:
      return splitAdditionalCharacteristicsControlField(controlField.getData());
    case PHYSICAL_DESCRIPTIONS_CONTROL_FIELD:
      return splitPhysicalDescriptionsControlField(controlField.getData());
    case GENERAL_INFORMATION_CONTROL_FIELD:
      return splitGeneralInformationControlField(controlField.getData(), leader);
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

  private Map<String, Object> splitGeneralInformationControlField(String content, Leader leader) {
    Map<String, Object> fieldItems = new LinkedHashMap<>();
    fieldItems.put(TYPE, leader.getTypeOfRecord());
    fieldItems.put(BLVL, leader.getImplDefined1()[0]);
    fieldItems.put(ELVL, leader.getImplDefined2()[0]);
    fieldItems.put(DESC, leader.getImplDefined2()[1]);
    fieldItems.putAll(fillContentMap(MaterialTypeConfiguration.getCommonItems(), content));
    fieldItems.putAll(fillContentMap(materialTypeConfiguration.getFixedLengthControlFieldItems(), content.substring(SPECIFIC_ELEMENTS_BEGIN_INDEX, SPECIFIC_ELEMENTS_END_INDEX)));
    return fieldItems;
  }

  private Map<String, Object> splitPhysicalDescriptionsControlField(String content) {
    PhysicalDescriptionFixedFieldElements physicalDescriptionFixedFieldElements = PhysicalDescriptionFixedFieldElements
      .resolveByCode(content.charAt(0));
    return physicalDescriptionFixedFieldElements.getItems()
      .stream()
      .collect(toMap(FixedLengthDataElements::getName, element -> (element.getLength() != 0) ? extractElementFromContent(content, element) : content));
  }

  private Map<String, Object> fillContentMap(List<FixedLengthDataElements> items, String content) {
    return items.stream()
      .collect(toMap(FixedLengthDataElements::getName, element -> element.isArray() ? Arrays.asList(extractElementFromContent(content, element).split(EMPTY)) : extractElementFromContent(content, element)));
  }

  private String extractElementFromContent(String content, FixedLengthDataElements element) {
    return content.substring(element.getPosition(), element.getPosition() + element.getLength());
  }

  private Field dataFieldToQuickMarcField(DataField dataField) {
    return new Field().withTag(dataField.getTag())
      .withIndicators(Arrays.asList(Character.toString(dataField.getIndicator1()), Character.toString(dataField.getIndicator2())))
      .withContent(dataField.getSubfields()
        .stream()
        .map(subfield -> new StringBuilder("$").append(subfield.getCode())
          .append(SPACE)
          .append(subfield.getData()))
        .collect(Collectors.joining(SPACE)));
  }
}
