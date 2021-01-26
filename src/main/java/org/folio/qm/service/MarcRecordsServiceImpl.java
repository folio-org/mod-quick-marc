package org.folio.qm.service;

import static org.folio.qm.converter.Constants.DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD;
import static org.folio.qm.util.MarcUtils.encodeToMarcDateTime;
import static org.folio.qm.util.MarcUtils.getFieldByTag;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import org.folio.qm.client.SRMChangeManagerClient;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

@Service
public class MarcRecordsServiceImpl implements MarcRecordsService {

  @Autowired
  private SRMChangeManagerClient srmClient;
  @Autowired
  private ValidationService validationService;

  @Autowired
  private Converter<ParsedRecordDto, QuickMarc> parsedRecordToQuickMarcConverter;
  @Autowired
  private Converter<QuickMarc, ParsedRecordDto> quickMarcToParsedRecordConverter;

  @Override
  public QuickMarc findByInstanceId(UUID instanceId) {
    return parsedRecordToQuickMarcConverter.convert(srmClient.getParsedRecordByInstanceId(instanceId.toString()));
  }

  @Override
  public void updateById(UUID instanceId, QuickMarc quickMarc) {
    validationService.validateIds(quickMarc, instanceId);
    ParsedRecordDto parsedRecordDto = quickMarcToParsedRecordConverter.convert(updateRecordTimestamp(quickMarc));
    srmClient.putParsedRecordByInstanceId(quickMarc.getParsedRecordDtoId(), parsedRecordDto);
  }

  private QuickMarc updateRecordTimestamp(QuickMarc quickMarc) {
    getFieldByTag(quickMarc, DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD)
      .ifPresent(field -> field.setContent(encodeToMarcDateTime(LocalDateTime.now())));
    return quickMarc;
  }
}
