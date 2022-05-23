package org.folio.qm.service.impl;

import static org.folio.qm.util.MarcUtils.updateRecordTimestamp;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.qm.client.DICSFieldProtectionSettingsClient;
import org.folio.qm.client.SRMChangeManagerClient;
import org.folio.qm.client.UsersClient;
import org.folio.qm.converter.MarcConverterFactory;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.exception.FieldsValidationException;
import org.folio.qm.mapper.CreationStatusMapper;
import org.folio.qm.mapper.UserMapper;
import org.folio.qm.service.CreationStatusService;
import org.folio.qm.service.MarcRecordsService;
import org.folio.qm.service.RecordCreationService;
import org.folio.qm.service.ValidationService;
import org.folio.rest.jaxrs.model.MarcFieldProtectionSettingsCollection;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarcRecordsServiceImpl implements MarcRecordsService {

  private static final String RECORD_NOT_FOUND_MESSAGE = "Record with id [%s] was not found";

  private final SRMChangeManagerClient srmClient;
  private final DICSFieldProtectionSettingsClient discClient;
  private final RecordCreationService recordCreationService;
  private final UsersClient usersClient;
  private final ValidationService validationService;
  private final CreationStatusService statusService;
  private final DefaultValuesPopulationService defaultValuesPopulationService;

  private final CreationStatusMapper statusMapper;
  private final UserMapper userMapper;
  private final FolioExecutionContext folioExecutionContext;
  private final MarcConverterFactory marcConverterFactory;

  @Override
  public QuickMarc findByExternalId(UUID externalId) {
    var parsedRecordDto = srmClient.getParsedRecordByExternalId(externalId.toString());
    MarcFieldProtectionSettingsCollection fieldProtectionSettingsMarc = discClient.getFieldProtectionSettingsMarc();
    var quickMarc = marcConverterFactory.findConverter(parsedRecordDto.getRecordType(), fieldProtectionSettingsMarc)
      .convert(parsedRecordDto);
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
  public void updateById(UUID parsedRecordId, QuickMarc quickMarc) {
    validationService.validateIdsMatch(quickMarc, parsedRecordId);
    populateWithDefaultValuesAndValidateMarcRecord(quickMarc);
    var parsedRecordDto =
      marcConverterFactory.findConverter(quickMarc.getMarcFormat()).convert(updateRecordTimestamp(quickMarc));
    srmClient.putParsedRecordByInstanceId(String.valueOf(quickMarc.getParsedRecordDtoId()), parsedRecordDto);
  }

  @Override
  public CreationStatus getCreationStatusByQmRecordId(UUID qmRecordId) {
    validationService.validateQmRecordId(qmRecordId);
    return statusService.findById(qmRecordId).map(statusMapper::fromEntity)
      .orElseThrow(() -> new NotFoundException(String.format(RECORD_NOT_FOUND_MESSAGE, qmRecordId)));
  }

  @Override
  public CreationStatus createNewRecord(QuickMarc quickMarc) {
    validationService.validateUserId(folioExecutionContext);
    populateWithDefaultValuesAndValidateMarcRecord(quickMarc);
    return recordCreationService.createRecord(quickMarc);
  }

  private void populateWithDefaultValuesAndValidateMarcRecord(QuickMarc quickMarc) {
    defaultValuesPopulationService.populate(quickMarc);
    var validationResult = validationService.validate(quickMarc);
    if (!validationResult.isValid()) {
      throw new FieldsValidationException(validationResult);
    }
  }

}
