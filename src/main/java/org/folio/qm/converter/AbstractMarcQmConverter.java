package org.folio.qm.converter;

import static org.apache.commons.lang3.StringUtils.SPACE;

import static org.folio.qm.converter.elements.Constants.BLANK_REPLACEMENT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.MarcFactoryImpl;
import org.springframework.lang.NonNull;

import org.folio.qm.converternew.FieldItemConverter;
import org.folio.qm.converternew.qm.AdditionalCharacteristicsFieldItemConverter;
import org.folio.qm.converternew.qm.CommonFieldItemConverter;
import org.folio.qm.converternew.qm.ControlFieldItemConverter;
import org.folio.qm.converternew.qm.GeneralInformationAuthorityFieldItemConverter;
import org.folio.qm.converternew.qm.GeneralInformationBibliographicFieldItemConverter;
import org.folio.qm.converternew.qm.GeneralInformationHoldingsFieldItemConverter;
import org.folio.qm.converternew.qm.LccnFieldItemConverter;
import org.folio.qm.converternew.qm.PhysicalMaterialFieldItemConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.util.QmMarcJsonWriter;
import org.folio.rest.jaxrs.model.AdditionalInfo;
import org.folio.rest.jaxrs.model.ExternalIdsHolder;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

public abstract class AbstractMarcQmConverter implements MarcQmConverter {

  private final MarcFactory factory = new MarcFactoryImpl();
  private final List<FieldItemConverter> fieldItemConverters = List.of(
    new CommonFieldItemConverter(), new LccnFieldItemConverter(), new AdditionalCharacteristicsFieldItemConverter(),
    new ControlFieldItemConverter(), new GeneralInformationAuthorityFieldItemConverter(),
    new GeneralInformationBibliographicFieldItemConverter(), new GeneralInformationHoldingsFieldItemConverter(),
    new PhysicalMaterialFieldItemConverter()
  );

  @Override
  public ParsedRecordDto convert(@NonNull QuickMarc source) {
    try {
      JsonNode jsonNode = convertToJson(source);

      return new ParsedRecordDto()
        .id(source.getParsedRecordDtoId())
        .recordType(supportedType())
        .externalIdsHolder(constructExternalIdsHolder(source))
        .relatedRecordVersion(source.getRelatedRecordVersion())
        .parsedRecord(new ParsedRecord().id(source.getParsedRecordId()).content(contentMap))
        .additionalInfo(new AdditionalInfo().suppressDiscovery(source.getSuppressDiscovery()));
    } catch (Exception e) {
      throw new ConverterException(e);
    }
  }

  private JsonNode convertToJson(QuickMarc source) throws IOException {
    Record marcRecord = toMarcRecord(source);
    try (ByteArrayOutputStream os = new ByteArrayOutputStream();
         QmMarcJsonWriter writer = new QmMarcJsonWriter(os)) {
      writer.write(marcRecord);
      return new ObjectMapper().readTree(os.toByteArray());
    }
  }

  protected abstract ExternalIdsHolder constructExternalIdsHolder(QuickMarc quickMarc);

  private Record toMarcRecord(QuickMarc quickMarcJson) {
    Record marcRecord = factory.newRecord();
    Leader leader = factory.newLeader(restoreBlanks(quickMarcJson.getLeader()));

    quickMarcJson.getFields()
      .stream()
      .map(field -> toVariableField(field, quickMarcJson.getMarcFormat()))
      .forEach(marcRecord::addVariableField);

    marcRecord.setLeader(leader);

    return marcRecord;
  }

  private VariableField toVariableField(FieldItem field, MarcFormat marcFormat) {
    return fieldItemConverters.stream()
      .filter(fieldItemConverter -> fieldItemConverter.canProcess(field, marcFormat))
      .findFirst()
      .map(fieldItemConverter -> fieldItemConverter.convert(field))
      .orElseThrow(() -> new IllegalArgumentException("Field converter not found"));
  }

  private String restoreBlanks(String sourceString) {
    return sourceString.replace(BLANK_REPLACEMENT, SPACE);
  }
}
