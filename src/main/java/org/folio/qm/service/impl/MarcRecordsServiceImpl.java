package org.folio.qm.service.impl;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.qm.converter.elements.Constants.TAG_001_CONTROL_FIELD;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.client.LinksSuggestionsClient;
import org.folio.qm.client.UsersClient;
import org.folio.qm.client.model.SourceRecord;
import org.folio.qm.converter.QuickMarcRecordConverter;
import org.folio.qm.converter.SourceRecordConverter;
import org.folio.qm.domain.QuickMarcRecord;
import org.folio.qm.domain.dto.AuthoritySearchParameter;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.exception.FieldsValidationException;
import org.folio.qm.mapper.LinksSuggestionsMapper;
import org.folio.qm.mapper.UserMapper;
import org.folio.qm.service.FieldProtectionSetterService;
import org.folio.qm.service.LinksService;
import org.folio.qm.service.MarcRecordsService;
import org.folio.qm.service.ValidationService;
import org.folio.qm.service.storage.source.SourceRecordService;
import org.folio.qm.validation.SkippedValidationError;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class MarcRecordsServiceImpl implements MarcRecordsService {

  private final SourceRecordService sourceRecordService;
  private final FieldProtectionSetterService protectionSetterService;
  private final ValidationService validationService;
  private final LinksService linksService;
  private final MarcRecordServiceRegistry marcRecordServiceRegistry;

  private final UsersClient usersClient;
  private final LinksSuggestionsClient linksSuggestionsClient;

  private final LinksSuggestionsMapper linksSuggestionsMapper;
  private final UserMapper userMapper;
  private final Converter<QuickMarcCreate, QuickMarcRecord> quickMarcCreateQuickMarcRecordConverter;
  private final Converter<QuickMarcEdit, QuickMarcRecord> quickMarcEditQuickMarcRecordConverter;
  private final QuickMarcRecordConverter quickMarcRecordConverter;
  private final SourceRecordConverter sourceRecordConverter;

  @Override
  public QuickMarcView findByExternalId(UUID externalId) {
    log.debug("findByExternalId:: trying to find quickMarc by externalId: {}", externalId);
    var sourceRecord = sourceRecordService.getByExternalId(externalId);
    var quickMarc = sourceRecordConverter.convert(sourceRecord);
    protectionSetterService.applyFieldProtection(quickMarc);
    linksService.setRecordLinks(quickMarc);
    setUserInfo(quickMarc, sourceRecord);
    log.info("findByExternalId:: quickMarc loaded by externalId: {}", externalId);
    return quickMarc;
  }

  @Override
  public void updateById(UUID parsedRecordId, QuickMarcEdit quickMarc) {
    log.debug("updateById:: trying to update quickMarc by parsedRecordId: {}", parsedRecordId);
    var quickMarcRecord = quickMarcEditQuickMarcRecordConverter.convert(quickMarc);
    validateOnUpdate(parsedRecordId, quickMarc);
    var recordService = marcRecordServiceRegistry.get(quickMarc.getMarcFormat());
    recordService.update(quickMarcRecord);
    linksService.updateRecordLinks(quickMarc);
    log.info("updateById:: quickMarc updated by parsedRecordId: {}", parsedRecordId);
  }

  @Override
  public QuickMarcView createRecord(QuickMarcCreate quickMarc) {
    log.debug("createRecord:: trying to create a new quickMarc");

    var quickMarcRecord = quickMarcCreateQuickMarcRecordConverter.convert(quickMarc);

    validateOnCreate(quickMarc);
    var recordService = marcRecordServiceRegistry.get(quickMarc.getMarcFormat());
    recordService.create(quickMarcRecord);
    log.info("createRecord:: new quickMarc created with qmRecordId: {}", quickMarcRecord.getExternalId());
    return quickMarcRecordConverter.convert(quickMarcRecord);
  }

  @Override
  public QuickMarcView suggestLinks(QuickMarcView quickMarcView, AuthoritySearchParameter authoritySearchParameter,
                                    Boolean ignoreAutoLinkingEnabled) {
    log.debug("suggestLinks:: trying to suggest links");
    var srsRecords = linksSuggestionsMapper.map(List.of(quickMarcView));
    var srsRecordsWithSuggestions = linksSuggestionsClient.postLinksSuggestions(srsRecords, authoritySearchParameter,
      ignoreAutoLinkingEnabled);
    var quickMarcRecordsWithSuggestions = linksSuggestionsMapper.map(srsRecordsWithSuggestions);
    if (isNotEmpty(quickMarcRecordsWithSuggestions)) {
      log.info("suggestLinks:: links was suggested");
      return quickMarcRecordsWithSuggestions.getFirst();
    }
    return quickMarcView;
  }

  private void validateOnCreate(BaseMarcRecord quickMarc) {
    var skippedValidationError = new SkippedValidationError(TAG_001_CONTROL_FIELD, MarcRuleCode.MISSING_FIELD);
    validationService.validateMarcRecord(quickMarc, List.of(skippedValidationError));
    validateMarcRecord(quickMarc);
  }

  private void validateOnUpdate(UUID parsedRecordId, QuickMarcEdit quickMarc) {
    validationService.validateMarcRecord(quickMarc, Collections.emptyList());
    validationService.validateIdsMatch(quickMarc, parsedRecordId);
    validateMarcRecord(quickMarc);
  }

  private void validateMarcRecord(BaseMarcRecord marcRecord) {
    var validationResult = validationService.validate(marcRecord);
    if (!validationResult.isValid()) {
      throw new FieldsValidationException(validationResult);
    }
  }

  private void setUserInfo(QuickMarcView quickMarc, SourceRecord sourceRecord) {
    if (sourceRecord.getMetadata() != null && sourceRecord.getMetadata().getUpdatedByUserId() != null) {
      usersClient.fetchUserById(sourceRecord.getMetadata().getUpdatedByUserId())
        .ifPresent(userDto -> {
          var userInfo = userMapper.fromDto(userDto);
          Objects.requireNonNull(quickMarc).getUpdateInfo().setUpdatedBy(userInfo);
        });
    }
  }
}
