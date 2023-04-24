package org.folio.qm.converter;

import static org.folio.qm.util.ErrorCodes.ILLEGAL_MARC_FORMAT;
import static org.folio.qm.util.ErrorUtils.buildInternalError;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import org.folio.qm.domain.dto.ExternalIdsHolder;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.mapper.MarcTypeMapper;
import org.marc4j.marc.MarcFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class MarcQmEditConverter extends MarcQmConverter<QuickMarcEdit> {

  public MarcQmEditConverter(MarcFactory factory, ObjectMapper objectMapper, MarcTypeMapper typeMapper,
                             MarcFieldsConverter fieldsConverter) {
    super(factory, objectMapper, typeMapper, fieldsConverter);
  }

  @Override
  public ParsedRecordDto convert(@NonNull QuickMarcEdit source) {
    var parsedRecordDto = Objects.requireNonNull(super.convert(updateRecordTimestamp(source)));
    parsedRecordDto.getParsedRecord().setId(source.getParsedRecordId());
    return parsedRecordDto
      .id(source.getParsedRecordDtoId())
      .externalIdsHolder(convertExternalIdsHolder(source))
      .relatedRecordVersion(source.getRelatedRecordVersion());
  }

  private ExternalIdsHolder convertExternalIdsHolder(QuickMarcEdit quickMarc) {
    var externalIdsHolder = new org.folio.qm.domain.dto.ExternalIdsHolder();
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
