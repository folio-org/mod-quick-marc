package org.folio.qm.service.impl;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.folio.qm.converter.elements.Constants.TAG_001_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.TAG_999_FIELD;
import static org.folio.qm.domain.entity.JobProfileAction.CREATE;
import static org.folio.qm.util.TenantContextUtils.runInFolioContext;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.ActionProfile;
import org.folio.Authority;
import org.folio.Holdings;
import org.folio.Metadata;
import org.folio.ParsedRecord;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.processing.mapping.defaultmapper.RecordMapperBuilder;
import org.folio.qm.client.AuthorityStorageClient;
import org.folio.qm.client.HoldingsSourceClient;
import org.folio.qm.client.HoldingsStorageClient;
import org.folio.qm.client.InstanceStorageClient;
import org.folio.qm.client.LinksSuggestionsClient;
import org.folio.qm.client.PrecedingSucceedingTitlesClient;
import org.folio.qm.client.SourceStorageClient;
import org.folio.qm.client.UsersClient;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.RecordTypeEnum;
import org.folio.qm.client.model.SourceRecord;
import org.folio.qm.client.model.instance.Instance;
import org.folio.qm.domain.dto.AuthoritySearchParameter;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.entity.HoldingsRecord;
import org.folio.qm.domain.entity.JobProfileAction;
import org.folio.qm.exception.FieldsValidationException;
import org.folio.qm.exception.OptimisticLockingException;
import org.folio.qm.exception.UnexpectedException;
import org.folio.qm.mapper.CreationStatusMapper;
import org.folio.qm.mapper.LinksSuggestionsMapper;
import org.folio.qm.mapper.UserMapper;
import org.folio.qm.service.ChangeManagerService;
import org.folio.qm.service.DataImportJobService;
import org.folio.qm.service.FieldProtectionSetterService;
import org.folio.qm.service.LinksService;
import org.folio.qm.service.MarcRecordsService;
import org.folio.qm.service.StatusService;
import org.folio.qm.service.ValidationService;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.folio.qm.service.support.PrecedingSucceedingTitlesHelper;
import org.folio.qm.util.ErrorUtils;
import org.folio.qm.validation.SkippedValidationError;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

@Service
@RequiredArgsConstructor
@Log4j2
public class MarcRecordsServiceImpl implements MarcRecordsService {

  private static final String RECORD_NOT_FOUND_MESSAGE = "Record with id [%s] was not found";
  private static final String MARC_NAME = "MARC";
  private static final String AUTHORITY_EXTENDED = "AUTHORITY_EXTENDED";
  private static final String STATISTICAL_CODE_IDS_PROPERTY = "statisticalCodeIds";
  private static final String NATURE_OF_CONTENT_TERM_IDS_PROPERTY = "natureOfContentTermIds";
  private static final String ADMINISTRATIVE_NOTES_PROPERTY = "administrativeNotes";
  private static final String PARENT_INSTANCES_PROPERTY = "parentInstances";
  private static final String CHILDREN_INSTANCES_PROPERTY = "childInstances";
  private static final Predicate<FieldItem> FIELD_001_PREDICATE =
    qmFields -> qmFields.getTag().equals(TAG_001_CONTROL_FIELD);
  private static final Predicate<FieldItem> FIELD_999_PREDICATE = qmFields -> qmFields.getTag().equals(TAG_999_FIELD);
  private static final Predicate<FieldItem> FIELD_EMPTY_PREDICATE = qmFields -> {
    final var content = qmFields.getContent();
    return content instanceof String stringContent && StringUtils.isEmpty(stringContent);
  };

  private final ChangeManagerService changeManagerService;
  private final DataImportJobService dataImportJobService;
  private final DefaultValuesPopulationService defaultValuesPopulationService;
  private final FieldProtectionSetterService protectionSetterService;
  private final ConversionService conversionService;
  private final ValidationService validationService;
  private final StatusService statusService;
  private final LinksService linksService;
  private final MappingMetadataProvider mappingMetadataProvider;
  private final PrecedingSucceedingTitlesHelper precedingSucceedingTitlesHelper;

  private final UsersClient usersClient;
  private final HoldingsSourceClient holdingsSourceClient;
  private final LinksSuggestionsClient linksSuggestionsClient;
  private final HoldingsStorageClient holdingsStorageClient;
  private final AuthorityStorageClient authorityStorageClient;
  private final InstanceStorageClient instanceStorageClient;
  private final PrecedingSucceedingTitlesClient precedingSucceedingTitlesClient;
  private final SourceStorageClient sourceStorageClient;

  private final LinksSuggestionsMapper linksSuggestionsMapper;
  private final CreationStatusMapper statusMapper;
  private final UserMapper userMapper;

  private final FolioExecutionContext folioExecutionContext;

  @Override
  public QuickMarcView findByExternalId(UUID externalId) {
    log.debug("findByExternalId:: trying to find quickMarc by externalId: {}", externalId);
    var sourceRecord = changeManagerService.getSourceRecordByExternalId(externalId.toString());
    var quickMarc = conversionService.convert(sourceRecord, QuickMarcView.class);
    protectionSetterService.applyFieldProtection(quickMarc);
    linksService.setRecordLinks(quickMarc);
    setUserInfo(quickMarc, sourceRecord);
    log.info("findByExternalId:: quickMarc loaded by externalId: {}", externalId);
    return quickMarc;
  }

  @Override
  public void updateById(UUID parsedRecordId, QuickMarcEdit quickMarc,
                         DeferredResult<ResponseEntity<Void>> updateResult) {
    try {
      log.debug("updateById:: trying to update quickMarc by parsedRecordId: {}", parsedRecordId);
      defaultValuesPopulationService.populate(quickMarc);
      validateOnUpdate(parsedRecordId, quickMarc);
      var parsedRecordDto = conversionService.convert(quickMarc, ParsedRecordDto.class);
      updateResult.onCompletion(updateLinksTask(folioExecutionContext, quickMarc, updateResult));

      if (parsedRecordDto != null && parsedRecordDto.getRecordType() != null) {
        if (RecordTypeEnum.HOLDING.equals(parsedRecordDto.getRecordType())) {
          var mappingMetadata = mappingMetadataProvider.getMappingData("marc-holdings");
          var mappingRules = mappingMetadata.mappingRules();
          var mappingParameters = mappingMetadata.mappingParameters();
          var holdingId = parsedRecordDto.getExternalIdsHolder().getHoldingsId().toString();
          var parsedRecord = new ParsedRecord().withContent(parsedRecordDto.getParsedRecord().getContent());
          var parsedRecordJson = retrieveParsedContent(parsedRecord);
          RecordMapper<Holdings> recordMapper = RecordMapperBuilder.buildMapper("MARC_HOLDINGS");
          var mappedHoldings = recordMapper.mapRecord(parsedRecordJson, mappingParameters, mappingRules);
          var source = holdingsSourceClient.getHoldingSourceByName(MARC_NAME);
          mappedHoldings.setSourceId(source.getSourceId());
          var holding = holdingsStorageClient.getHoldingById(holdingId);
          var updatedHolding = mergeRecords(holding, mappedHoldings);
          var holdingRecord = populateUpdatedByUserIdIfNeeded(updatedHolding.result());
          ResponseEntity<Void> response = holdingsStorageClient.updateHolding(holdingRecord.getId(), holdingRecord);
          processResponse(parsedRecordId, updateResult, response, parsedRecordDto);
        }
        if (RecordTypeEnum.AUTHORITY.equals(parsedRecordDto.getRecordType())) {
          var mappingMetadata = mappingMetadataProvider.getMappingData("marc-authority");
          var mappingRules = mappingMetadata.mappingRules();
          var mappingParameters = mappingMetadata.mappingParameters();
          var authorityId = parsedRecordDto.getExternalIdsHolder().getAuthorityId().toString();
          var parsedRecord = new ParsedRecord().withContent(parsedRecordDto.getParsedRecord().getContent());
          RecordMapper<Authority> recordMapper = isAuthorityExtendedMode()
            ? RecordMapperBuilder.buildMapper(ActionProfile.FolioRecord.MARC_AUTHORITY_EXTENDED.value())
            : RecordMapperBuilder.buildMapper(RecordTypeEnum.AUTHORITY.getValue());
          var parsedRecordJson = retrieveParsedContent(parsedRecord);
          var mappedAuthority = recordMapper.mapRecord(parsedRecordJson, mappingParameters, mappingRules);
          var existingAuthority = authorityStorageClient.getAuthorityById(authorityId);
          var updatedAuthority = mergeRecords(existingAuthority, mappedAuthority);
          ResponseEntity<Void> response = authorityStorageClient.updateAuthority(authorityId,
            updatedAuthority.result());
          processResponse(parsedRecordId, updateResult, response, parsedRecordDto);
        }
        if (RecordTypeEnum.BIB.equals(parsedRecordDto.getRecordType())) {
          var mappingMetadata = mappingMetadataProvider.getMappingData("marc-bib");
          var mappingRules = mappingMetadata.mappingRules();
          var mappingParameters = mappingMetadata.mappingParameters();
          var instanceId = parsedRecordDto.getExternalIdsHolder().getInstanceId().toString();
          var parsedRecord = new ParsedRecord().withContent(parsedRecordDto.getParsedRecord().getContent());
          var parsedRecordJson = retrieveParsedContent(parsedRecord);

          RecordMapper<org.folio.Instance> recordMapper = RecordMapperBuilder
            .buildMapper(RecordTypeEnum.BIB.getValue());
          var mappedInstance = recordMapper.mapRecord(parsedRecordJson, mappingParameters, mappingRules);

          org.folio.qm.client.model.Instance existingInstance = instanceStorageClient.getInstanceById(instanceId);

          var updatedInstance = mergeRecords(existingInstance, mappedInstance);
          log.info("updateById:: updated instance id: {} updatedInstance: {}", instanceId, updatedInstance.result());

          var updatedResponse = instanceStorageClient.updateInstance(instanceId,
            updatedInstance.result().getJsonForStorage().mapTo(org.folio.qm.client.model.Instance.class));
          if (updatedResponse.getStatusCode().is2xxSuccessful()) {
            var titles = precedingSucceedingTitlesHelper.updatePrecedingSucceedingTitles(
              updatedInstance.result());
            var response = precedingSucceedingTitlesClient.updateTitles(instanceId, titles);
            log.info("updateById:: instance update response for id: {} response: {}", instanceId,
              response.getStatusCode());
            processResponse(parsedRecordId, updateResult, response, parsedRecordDto);
          } else {
            log.error("updateById:: failed to update quickMarc by parsedRecordId: {} response status: {}",
              parsedRecordId, updatedResponse.getStatusCode().value());
            var error = ErrorUtils.buildError(ErrorUtils.ErrorType.EXTERNAL_OR_UNDEFINED,
              "Failed to update parsedRecordDto for quickMarc with parsedRecordId: " + parsedRecordId);
            updateResult.setErrorResult(ResponseEntity.badRequest().body(error));
          }
        }
      }
      log.info("updateById:: quickMarc updated by parsedRecordId: {}", parsedRecordId);
    } catch (Exception e) {
      log.error("updateById:: failed to update Mrc record", e);
    }
  }

  private JsonObject retrieveParsedContent(ParsedRecord parsedRecord) {
    return parsedRecord.getContent() instanceof String
      ? new JsonObject(parsedRecord.getContent().toString())
      : JsonObject.mapFrom(parsedRecord.getContent());
  }

  private void processResponse(UUID parsedRecordId, DeferredResult<ResponseEntity<Void>> updateResult,
                               ResponseEntity<Void> response, ParsedRecordDto parsedRecordDto) {
    if (response.getStatusCode().is2xxSuccessful()) {
      var result = sourceStorageClient.putParsedRecordDto(parsedRecordDto);
      if (result.getStatusCode().is2xxSuccessful()) {
        log.info("updateById:: quickMarc updated by parsedRecordId: {}", parsedRecordId);
        updateResult.setResult(ResponseEntity.accepted().build());
      } else {
        log.error("updateById:: failed to update quickMarc by parsedRecordId: {} response status: {}", parsedRecordId,
          result.getStatusCode().value());
        var error = ErrorUtils.buildError(ErrorUtils.ErrorType.EXTERNAL_OR_UNDEFINED,
          "Failed to update parsedRecordDto for quickMarc with parsedRecordId: " + parsedRecordId);
        updateResult.setErrorResult(ResponseEntity.badRequest().body(error));
      }
    } else {
      log.error("updateById:: failed to update holding for quickMarc by parsedRecordId: {}", parsedRecordId);
      var error = ErrorUtils.buildError(ErrorUtils.ErrorType.EXTERNAL_OR_UNDEFINED,
        "Failed to update parsedRecordDto for quickMarc with parsedRecordId: " + parsedRecordId);
      updateResult.setErrorResult(ResponseEntity.badRequest().body(error));
    }
  }

  @Override
  public CreationStatus getCreationStatusByQmRecordId(UUID qmRecordId) {
    log.debug("getCreationStatusByQmRecordId:: trying to get creationStatus by qmRecordId: {}", qmRecordId);
    return statusService.findById(qmRecordId)
      .map(status -> {
        log.info("getCreationStatusByQmRecordId:: creationStatus: {} loaded by qmRecordId: {}",
          status.getStatus(), qmRecordId);
        return statusMapper.fromEntity(status);
      })
      .orElseThrow(() -> new NotFoundException(String.format(RECORD_NOT_FOUND_MESSAGE, qmRecordId)));
  }

  @Override
  public CreationStatus createNewRecord(QuickMarcCreate quickMarc) {
    log.debug("createNewRecord:: trying to create a new quickMarc");
    defaultValuesPopulationService.populate(quickMarc);
    validateOnCreate(quickMarc);
    var recordDto = conversionService.convert(prepareRecord(quickMarc), ParsedRecordDto.class);
    var status = runImportAndGetStatus(recordDto, CREATE);
    log.info("createNewRecord:: new quickMarc created with qmRecordId: {}", status.getQmRecordId());
    return status;
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

  private QuickMarcCreate prepareRecord(QuickMarcCreate quickMarc) {
    var fieldItemPredicate = FIELD_EMPTY_PREDICATE.or(FIELD_999_PREDICATE);
    if (quickMarc.getMarcFormat() != MarcFormat.AUTHORITY) {
      fieldItemPredicate = fieldItemPredicate.or(FIELD_001_PREDICATE);
    }
    quickMarc.getFields().removeIf(fieldItemPredicate);
    return quickMarc;
  }

  private CreationStatus runImportAndGetStatus(ParsedRecordDto recordDto, JobProfileAction action) {
    var jobId = dataImportJobService.executeDataImportJob(recordDto, action);
    return statusService.findByJobExecutionId(jobId)
      .map(statusMapper::fromEntity)
      .orElseThrow(() -> new UnexpectedException(String.format(RECORD_NOT_FOUND_MESSAGE, jobId)));
  }

  private void validateOnCreate(QuickMarcCreate quickMarc) {
    var skippedValidationError = new SkippedValidationError(TAG_001_CONTROL_FIELD, MarcRuleCode.MISSING_FIELD);
    validationService.validateMarcRecord(quickMarc, List.of(skippedValidationError));
    validateMarcRecord(quickMarc);
  }

  private void validateOnUpdate(UUID parsedRecordId, QuickMarcEdit quickMarc) {
    var requestVersion = quickMarc.getSourceVersion();
    var storedVersion =
      changeManagerService.getSourceRecordByExternalId(quickMarc.getExternalId().toString()).getGeneration();
    if (requestVersion != null && !requestVersion.equals(storedVersion)) {
      throw new OptimisticLockingException(parsedRecordId, storedVersion, requestVersion);
    }
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

  private Runnable updateLinksTask(FolioExecutionContext executionContext,
                                   QuickMarcEdit quickMarc,
                                   DeferredResult<ResponseEntity<Void>> updateResult) {
    var newContext = new DefaultFolioExecutionContext(executionContext.getFolioModuleMetadata(),
      executionContext.getAllHeaders());
    return () -> {
      @SuppressWarnings("unchecked")
      var result = (ResponseEntity<Void>) updateResult.getResult();
      if (result == null || result.getStatusCode().isError()) {
        return;
      }
      runInFolioContext(newContext, () -> linksService.updateRecordLinks(quickMarc));
    };
  }

  private Future<HoldingsRecord> mergeRecords(HoldingsRecord existingRecord, Holdings mappedRecord) {
    try {
      mappedRecord.setId(existingRecord.getId());
      mappedRecord.setVersion(existingRecord.getVersion());
      JsonObject existing = JsonObject.mapFrom(existingRecord);
      JsonObject mapped = JsonObject.mapFrom(mappedRecord);
      JsonObject merged = existing.mergeIn(mapped);
      HoldingsRecord mergedHoldingsRecord = merged.mapTo(HoldingsRecord.class);
      updateCallNumberFields(mergedHoldingsRecord, mappedRecord);
      return Future.succeededFuture(mergedHoldingsRecord);
    } catch (Exception e) {
      log.error("Error updating holdings", e);
      return Future.failedFuture(e);
    }
  }

  private Future<Authority> mergeRecords(Authority existingRecord, Authority mappedRecord) {
    try {
      mappedRecord.setId(existingRecord.getId());
      mappedRecord.setVersion(existingRecord.getVersion());
      JsonObject mapped = JsonObject.mapFrom(mappedRecord);
      Authority mergedAuthorityRecord = mapped.mapTo(Authority.class);
      mergedAuthorityRecord.setSource(Authority.Source.MARC);
      return Future.succeededFuture(mergedAuthorityRecord);
    } catch (Exception e) {
      log.error("Error updating authority", e);
      return Future.failedFuture(e);
    }
  }

  private Future<Instance> mergeRecords(org.folio.qm.client.model.Instance existingInstance,
                                        org.folio.Instance mappedInstance) {
    log.info("mergeRecords:: Merging existing instance with id: {} and mapped instance",
      existingInstance.getId());
    try {
      mappedInstance.setId(existingInstance.getId());
      mappedInstance.setVersion(existingInstance.getVersion());
      if (isNotTrue(existingInstance.getDeleted()) && isTrue(mappedInstance.getDeleted())) {
        mappedInstance.withDiscoverySuppress(true);
        mappedInstance.withStaffSuppress(true);
      } else {
        mappedInstance.withStaffSuppress(existingInstance.getStaffSuppress());
        mappedInstance.withDiscoverySuppress(existingInstance.getDiscoverySuppress());
      }

      JsonObject existing = JsonObject.mapFrom(existingInstance);
      JsonObject mapped = JsonObject.mapFrom(mappedInstance);
      JsonObject mergedInstanceAsJson = mergeInstances(existing, mapped);
      Instance mergedInstance = Instance.fromJson(mergedInstanceAsJson);
      return Future.succeededFuture(mergedInstance);
    } catch (Exception e) {
      log.error("Error updating instance", e);
      return Future.failedFuture(e);
    }
  }

  public JsonObject mergeInstances(JsonObject existing, JsonObject mapped) {
    JsonArray statisticalCodeIds = existing.getJsonArray(STATISTICAL_CODE_IDS_PROPERTY);
    JsonArray natureOfContentTermIds = existing.getJsonArray(NATURE_OF_CONTENT_TERM_IDS_PROPERTY);
    JsonArray administrativeNotes = existing.getJsonArray(ADMINISTRATIVE_NOTES_PROPERTY);
    JsonArray parents = existing.getJsonArray(PARENT_INSTANCES_PROPERTY);
    JsonArray children = existing.getJsonArray(CHILDREN_INSTANCES_PROPERTY);
    JsonObject mergedInstanceAsJson = existing.mergeIn(mapped);
    mergedInstanceAsJson.put(STATISTICAL_CODE_IDS_PROPERTY, statisticalCodeIds);
    mergedInstanceAsJson.put(NATURE_OF_CONTENT_TERM_IDS_PROPERTY, natureOfContentTermIds);
    mergedInstanceAsJson.put(ADMINISTRATIVE_NOTES_PROPERTY, administrativeNotes);
    mergedInstanceAsJson.put(PARENT_INSTANCES_PROPERTY, parents);
    mergedInstanceAsJson.put(CHILDREN_INSTANCES_PROPERTY, children);
    return mergedInstanceAsJson;
  }

  private void updateCallNumberFields(HoldingsRecord existingRecord, Holdings mappedHoldings) {
    existingRecord.setShelvingTitle(mappedHoldings.getShelvingTitle());
    existingRecord.setCopyNumber(mappedHoldings.getCopyNumber());
    existingRecord.setCallNumberTypeId(mappedHoldings.getCallNumberTypeId());
    existingRecord.setCallNumberPrefix(mappedHoldings.getCallNumberPrefix());
    existingRecord.setCallNumber(mappedHoldings.getCallNumber());
    existingRecord.setCallNumberSuffix(mappedHoldings.getCallNumberSuffix());
  }

  public HoldingsRecord populateUpdatedByUserIdIfNeeded(HoldingsRecord holding) {
    if (holding.getMetadata() == null) {
      holding.setMetadata(new Metadata());
    }

    if (StringUtils.isBlank(holding.getMetadata().getUpdatedByUserId())) {
      holding.getMetadata().setUpdatedByUserId(getUserId());
    }
    return holding;
  }

  private String getUserId() {
    if (StringUtils.isNotBlank(folioExecutionContext.getUserId().toString())) {
      return folioExecutionContext.getUserId().toString();
    }
    return null;
  }

  private boolean isAuthorityExtendedMode() {
    return Boolean.parseBoolean(
      System.getenv().getOrDefault(AUTHORITY_EXTENDED, "false"));
  }
}
