package org.folio.qm.service.impl;

import static org.folio.qm.converter.Constants.DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD;
import static org.folio.qm.util.MarcUtils.encodeToMarcDateTime;
import static org.folio.qm.util.MarcUtils.getFieldByTag;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import org.folio.qm.client.SRMChangeManagerClient;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.mapper.CreationStatusMapper;
import org.folio.qm.service.CreationStatusService;
import org.folio.qm.service.MarcRecordsService;
import org.folio.qm.service.ValidationService;
import org.folio.rest.jaxrs.model.ParsedRecordDto;
import org.folio.spring.exception.NotFoundException;

@Service
@RequiredArgsConstructor
public class MarcRecordsServiceImpl implements MarcRecordsService {

  private static final String RECORD_NOT_FOUND_MESSAGE = "Record with id [%s] was not found";

  private final SRMChangeManagerClient srmClient;
  private final ValidationService validationService;
  private final CreationStatusService statusService;

  private final Converter<ParsedRecordDto, QuickMarc> parsedRecordToQuickMarcConverter;
  private final Converter<QuickMarc, ParsedRecordDto> quickMarcToParsedRecordConverter;
  private final CreationStatusMapper statusMapper;

  @Override
  public QuickMarc findByInstanceId(UUID instanceId) {
    return parsedRecordToQuickMarcConverter.convert(srmClient.getParsedRecordByInstanceId(instanceId.toString()));
  }

  @Override
  public void updateById(UUID instanceId, QuickMarc quickMarc) {
    validationService.validateIdsMatch(quickMarc, instanceId);
    ParsedRecordDto parsedRecordDto = quickMarcToParsedRecordConverter.convert(updateRecordTimestamp(quickMarc));
    srmClient.putParsedRecordByInstanceId(quickMarc.getParsedRecordDtoId(), parsedRecordDto);
  }

  @Override
  public CreationStatus getCreationStatusByQmRecordId(UUID qmRecordId) {
    validationService.validateQmRecordId(qmRecordId);
    return statusService.findById(qmRecordId).map(statusMapper::fromEntity)
      .orElseThrow(() -> new NotFoundException(String.format(RECORD_NOT_FOUND_MESSAGE, qmRecordId)));
  }

  private QuickMarc updateRecordTimestamp(QuickMarc quickMarc) {
    getFieldByTag(quickMarc, DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD)
      .ifPresent(field -> field.setContent(encodeToMarcDateTime(LocalDateTime.now())));
    return quickMarc;
  }
}
