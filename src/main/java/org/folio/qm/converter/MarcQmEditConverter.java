package org.folio.qm.converter;

import static org.folio.qm.util.ErrorCodes.ILLEGAL_MARC_FORMAT;
import static org.folio.qm.util.ErrorUtils.buildInternalError;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import org.folio.qm.client.model.ExternalIdsHolder;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.mapper.MarcTypeMapper;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class MarcQmEditConverter extends MarcQmConverter<QuickMarcEdit> {

  public MarcQmEditConverter(ObjectMapper objectMapper, MarcTypeMapper typeMapper,
                             Converter<BaseMarcRecord, Record> recordConverter) {
    super(objectMapper, typeMapper, recordConverter);
  }

  @Override
  public ParsedRecordDto convert(@NonNull QuickMarcEdit source) {
    var parsedRecordDto = Objects.requireNonNull(super.convert(updateRecordTimestamp(source)));
    parsedRecordDto.getParsedRecord().setId(source.getParsedRecordId());
    return parsedRecordDto
      .setId(source.getParsedRecordDtoId())
      .setExternalIdsHolder(convertExternalIdsHolder(source));
  }

  private ExternalIdsHolder convertExternalIdsHolder(QuickMarcEdit quickMarc) {
    var externalIdsHolder = new ExternalIdsHolder();
    switch (quickMarc.getMarcFormat()) {
      case BIBLIOGRAPHIC -> {
        externalIdsHolder.setInstanceId(quickMarc.getExternalId());
        externalIdsHolder.setInstanceHrid(quickMarc.getExternalHrid());
      }
      case HOLDINGS -> {
        externalIdsHolder.setHoldingsId(quickMarc.getExternalId());
        externalIdsHolder.setHoldingsHrid(quickMarc.getExternalHrid());
      }
      case AUTHORITY -> {
        externalIdsHolder.setAuthorityId(quickMarc.getExternalId());
        externalIdsHolder.setAuthorityHrid(quickMarc.getExternalHrid());
      }
      default -> throw new ConverterException(buildInternalError(ILLEGAL_MARC_FORMAT, "Unexpected marc format"));
    }
    return externalIdsHolder;
  }
}
