package org.folio.qm.service.impl;

import static org.folio.qm.domain.entity.JobProfileAction.CREATE;
import static org.folio.qm.domain.entity.JobProfileAction.DELETE;
import static org.folio.qm.domain.entity.JobProfileAction.UPDATE;
import static org.folio.qm.util.MarcUtils.updateRecordTimestamp;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import org.folio.qm.client.UsersClient;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.RecordActionStatus;
import org.folio.qm.domain.entity.JobProfileAction;
import org.folio.qm.exception.FieldsValidationException;
import org.folio.qm.exception.UnexpectedException;
import org.folio.qm.mapper.ActionStatusMapper;
import org.folio.qm.mapper.UserMapper;
import org.folio.qm.service.SRMService;
import org.folio.qm.service.DataImportJobService;
import org.folio.qm.service.FieldProtectionSetterService;
import org.folio.qm.service.MarcRecordsService;
import org.folio.qm.service.StatusService;
import org.folio.qm.service.ValidationService;
import org.folio.spring.exception.NotFoundException;

@Service
@RequiredArgsConstructor
public class MarcRecordsServiceImpl implements MarcRecordsService {

  private static final String RECORD_NOT_FOUND_MESSAGE = "Record with id [%s] was not found";

  private final SRMService srmService;
  private final DataImportJobService dataImportJobService;
  private final UsersClient usersClient;
  private final ValidationService validationService;
  private final StatusService statusService;
  private final FieldProtectionSetterService protectionSetterService;

  private final ActionStatusMapper statusMapper;
  private final UserMapper userMapper;
  private final Converter<QuickMarc, ParsedRecordDto> qmConverter;
  private final Converter<ParsedRecordDto, QuickMarc> dtoConverter;

  @Override
  public QuickMarc findRecordByExternalId(UUID externalId) {
    var parsedRecordDto = srmService.getParsedRecordByExternalId(externalId.toString());
    var quickMarc = protectionSetterService.applyFieldProtection(dtoConverter.convert(parsedRecordDto));
    if (parsedRecordDto.getMetadata() != null && parsedRecordDto.getMetadata().getUpdatedByUserId() != null) {
      usersClient.fetchUserById(parsedRecordDto.getMetadata().getUpdatedByUserId())
        .ifPresent(userDto -> {
          var userInfo = userMapper.fromDto(userDto);
          Objects.requireNonNull(quickMarc).getUpdateInfo().setUpdatedBy(userInfo);
        });
    }
    return quickMarc;
  }

  @Override
  public RecordActionStatus createRecord(QuickMarc quickMarc) {
    validateMarcFields(quickMarc);
    var recordDto = qmConverter.convert(prepareRecordForCreation(quickMarc));
    return runImportAndGetStatus(recordDto, CREATE);
  }

  @Override
  public RecordActionStatus updateById(UUID parsedRecordId, QuickMarc quickMarc) {
    validationService.validateIdsMatch(quickMarc, parsedRecordId);
    validateMarcFields(quickMarc);
    var parsedRecordDto = qmConverter.convert(updateRecordTimestamp(quickMarc));
    return runImportAndGetStatus(parsedRecordDto, UPDATE);
  }

  @Override
  public RecordActionStatus deleteRecordByExternalId(UUID externalId) {
    var recordDto = srmService.getParsedRecordByExternalId(externalId.toString());
    return runImportAndGetStatus(recordDto, DELETE);
  }

  @Override
  public RecordActionStatus getActionStatusByActionId(UUID actionId) {
    return statusService.findById(actionId).map(statusMapper::fromEntity)
      .orElseThrow(() -> new NotFoundException(String.format(RECORD_NOT_FOUND_MESSAGE, actionId)));
  }

  private RecordActionStatus runImportAndGetStatus(ParsedRecordDto recordDto, JobProfileAction delete) {
    var jobId = dataImportJobService.executeDataImportJob(recordDto, delete);
    return statusService.findByJobExecutionId(jobId)
      .map(statusMapper::fromEntity)
      .orElseThrow(() -> new UnexpectedException(String.format(RECORD_NOT_FOUND_MESSAGE, jobId)));
  }

  private QuickMarc prepareRecordForCreation(QuickMarc quickMarc) {
    clearFields(quickMarc);
    updateRecordTimestamp(quickMarc);
    return quickMarc;
  }

  private void clearFields(QuickMarc quickMarc) {
    final Predicate<FieldItem> field999Predicate = qmFields -> qmFields.getTag().equals("999");
    final Predicate<FieldItem> emptyContentPredicate = qmFields -> {
      final var content = qmFields.getContent();
      return content instanceof String && StringUtils.isEmpty((String) content);
    };
    quickMarc.getFields().removeIf(field999Predicate.or(emptyContentPredicate));
    quickMarc.setParsedRecordId(null);
    quickMarc.setParsedRecordDtoId(null);
    quickMarc.setExternalId(null);
    quickMarc.setExternalHrid(null);
  }

  private void validateMarcFields(QuickMarc quickMarc) {
    var validationResult = validationService.validate(quickMarc);
    if (!validationResult.isValid()) {
      throw new FieldsValidationException(validationResult);
    }
  }
}
