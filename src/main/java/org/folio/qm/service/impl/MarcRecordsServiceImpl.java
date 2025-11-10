package org.folio.qm.service.impl;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.folio.qm.converter.elements.Constants.TAG_001_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.TAG_999_FIELD;
import static org.folio.qm.domain.dto.CreationStatus.StatusEnum.CREATED;
import static org.folio.qm.util.AdditionalFieldsUtil.SUBFIELD_I;
import static org.folio.qm.util.AdditionalFieldsUtil.TAG_001;
import static org.folio.qm.util.AdditionalFieldsUtil.TAG_003;
import static org.folio.qm.util.AdditionalFieldsUtil.TAG_999;
import static org.folio.qm.util.AdditionalFieldsUtil.addControlledFieldToMarcRecord;
import static org.folio.qm.util.AdditionalFieldsUtil.addFieldToMarcRecord;
import static org.folio.qm.util.AdditionalFieldsUtil.getControlFieldValue;
import static org.folio.qm.util.AdditionalFieldsUtil.normalize035;
import static org.folio.qm.util.AdditionalFieldsUtil.removeField;
import static org.folio.qm.util.JsonUtils.objectToJsonString;
import static org.folio.qm.util.TenantContextUtils.runInFolioContext;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import org.folio.RawRecord;
import org.folio.processing.exceptions.EventProcessingException;
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
import org.folio.qm.client.model.AdditionalInfo;
import org.folio.qm.client.model.ExternalIdsHolder;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.Record;
import org.folio.qm.client.model.RecordTypeEnum;
import org.folio.qm.client.model.Snapshot;
import org.folio.qm.client.model.SourceRecord;
import org.folio.qm.domain.dto.AuthoritySearchParameter;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.entity.HoldingsRecord;
import org.folio.qm.domain.entity.RecordCreationStatus;
import org.folio.qm.domain.entity.RecordCreationStatusEnum;
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
import org.folio.qm.util.AdditionalFieldsUtil;
import org.folio.qm.util.ErrorUtils;
import org.folio.qm.util.StatusUtils;
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

  private static final String PERMANENT_LOCATION_ID_ERROR_MESSAGE =
    "Can`t create Holding entity: 'permanentLocationId' is empty";
  private static final String FIELD_004_MARC_HOLDINGS_NOT_NULL = "The field 004 for marc holdings must be not null";
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
    log.debug("updateById:: trying to update quickMarc by parsedRecordId: {}", parsedRecordId);
    defaultValuesPopulationService.populate(quickMarc);
    validateOnUpdate(parsedRecordId, quickMarc);
    var parsedRecordDto = conversionService.convert(quickMarc, ParsedRecordDto.class);
    updateResult.onCompletion(updateLinksTask(folioExecutionContext, quickMarc, updateResult));

    switch (Objects.requireNonNull(parsedRecordDto).getRecordType()) {
      case HOLDING -> updateHolding(parsedRecordId, updateResult, parsedRecordDto);
      case AUTHORITY -> updateAuthority(parsedRecordId, updateResult, parsedRecordDto);
      case BIB -> updateInstance(parsedRecordId, updateResult, parsedRecordDto);
      default -> setErrorResult(parsedRecordId, updateResult);
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
    var snapshotId = UUID.randomUUID().toString();
    var status = StatusUtils.getStatus(UUID.fromString(snapshotId), RecordCreationStatusEnum.CREATED);

    switch (Objects.requireNonNull(recordDto).getRecordType()) {
      case HOLDING -> createHolding(recordDto, snapshotId, status);
      case AUTHORITY -> createAuthority(recordDto, snapshotId, status);
      case BIB -> createInstance(recordDto, snapshotId, status);
      default -> throw new UnexpectedException("Unsupported record type: " + recordDto.getRecordType());
    }
    return getCreationStatus(status);
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

  private void updateInstance(UUID parsedRecordId, DeferredResult<ResponseEntity<Void>> updateResult,
                              ParsedRecordDto parsedRecordDto) {
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
      var titlesJson = JsonObject.mapFrom(titles);
      org.folio.qm.client.model.PrecedingSucceedingTitleCollection titleCollection = titlesJson.mapTo(
        org.folio.qm.client.model.PrecedingSucceedingTitleCollection.class);
      var response = precedingSucceedingTitlesClient.updateTitles(instanceId, titleCollection);
      log.info("updateById:: instance update response for id: {} response: {}", instanceId,
        response.getStatusCode());
      handleSrsRecordUpdateResult(parsedRecordId, updateResult, response, parsedRecordDto);
    } else {
      log.error("updateById:: failed to update quickMarc by parsedRecordId: {} response status: {}",
        parsedRecordId, updatedResponse.getStatusCode().value());
      setErrorResult(parsedRecordId, updateResult);
    }
  }

  private void updateAuthority(UUID parsedRecordId, DeferredResult<ResponseEntity<Void>> updateResult,
                               ParsedRecordDto parsedRecordDto) {
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
    handleSrsRecordUpdateResult(parsedRecordId, updateResult, response, parsedRecordDto);
  }

  private void updateHolding(UUID parsedRecordId, DeferredResult<ResponseEntity<Void>> updateResult,
                             ParsedRecordDto parsedRecordDto) {
    var mappingMetadata = mappingMetadataProvider.getMappingData("marc-holdings");
    var mappingRules = mappingMetadata.mappingRules();
    var mappingParameters = mappingMetadata.mappingParameters();
    var parsedRecord = new ParsedRecord().withContent(parsedRecordDto.getParsedRecord().getContent());
    var parsedRecordJson = retrieveParsedContent(parsedRecord);

    RecordMapper<Holdings> recordMapper = RecordMapperBuilder.buildMapper("MARC_HOLDINGS");
    var mappedHoldings = recordMapper.mapRecord(parsedRecordJson, mappingParameters, mappingRules);

    var source = holdingsSourceClient.getHoldingSourceByName(MARC_NAME);
    mappedHoldings.setSourceId(source.getSourceId());
    var holdingId = parsedRecordDto.getExternalIdsHolder().getHoldingsId().toString();
    var holding = holdingsStorageClient.getHoldingById(holdingId);
    var updatedHolding = mergeRecords(holding, mappedHoldings);
    var holdingRecord = populateUpdatedByUserIdIfNeeded(updatedHolding.result());

    ResponseEntity<Void> response = holdingsStorageClient.updateHolding(holdingRecord.getId(), holdingRecord);
    handleSrsRecordUpdateResult(parsedRecordId, updateResult, response, parsedRecordDto);
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

  private Future<org.folio.qm.client.model.instance.Instance> mergeRecords(
    org.folio.qm.client.model.Instance existingInstance,
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
      org.folio.qm.client.model.instance.Instance mergedInstance = org.folio.qm.client.model.instance.Instance
        .fromJson(mergedInstanceAsJson);
      return Future.succeededFuture(mergedInstance);
    } catch (Exception e) {
      log.error("Error updating instance", e);
      return Future.failedFuture(e);
    }
  }

  private JsonObject mergeInstances(JsonObject existing, JsonObject mapped) {
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

  private HoldingsRecord populateUpdatedByUserIdIfNeeded(HoldingsRecord holding) {
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

  private void setErrorResult(UUID parsedRecordId, DeferredResult<ResponseEntity<Void>> updateResult) {
    var error = ErrorUtils.buildError(ErrorUtils.ErrorType.EXTERNAL_OR_UNDEFINED,
      "Failed to update parsedRecordDto for quickMarc with parsedRecordId: " + parsedRecordId);
    updateResult.setErrorResult(ResponseEntity.badRequest().body(error));
  }

  private JsonObject retrieveParsedContent(ParsedRecord parsedRecord) {
    return parsedRecord.getContent() instanceof String
      ? new JsonObject(parsedRecord.getContent().toString())
      : JsonObject.mapFrom(parsedRecord.getContent());
  }

  private void handleSrsRecordUpdateResult(UUID parsedRecordId, DeferredResult<ResponseEntity<Void>> updateResult,
                                           ResponseEntity<Void> response, ParsedRecordDto parsedRecordDto) {
    if (response.getStatusCode().is2xxSuccessful()) {
      //store updated parsedRecordDto in the SRS module
      var result = sourceStorageClient.putParsedRecordDto(parsedRecordDto);
      if (result.getStatusCode().is2xxSuccessful()) {
        log.info("updateById:: quickMarc updated by parsedRecordId: {}", parsedRecordId);
        updateResult.setResult(ResponseEntity.accepted().build());
      } else {
        log.error("updateById:: failed to update quickMarc by parsedRecordId: {} response status: {}", parsedRecordId,
          result.getStatusCode().value());
        setErrorResult(parsedRecordId, updateResult);
      }
    } else {
      log.error("updateById:: failed to update MARC record by parsedRecordId: {}", parsedRecordId);
      setErrorResult(parsedRecordId, updateResult);
    }
  }

  private void createInstance(ParsedRecordDto recordDto, String snapshotId, RecordCreationStatus status) {
    var mappedInstance = getMappedInstance(recordDto);
    mappedInstance.setDiscoverySuppress(recordDto.getAdditionalInfo().isSuppressDiscovery());
    setMetadata(mappedInstance);
    var createdInstanceResponse = instanceStorageClient.createInstance(
      JsonObject.mapFrom(mappedInstance).mapTo(org.folio.qm.client.model.Instance.class));
    if (!createdInstanceResponse.getStatusCode().is2xxSuccessful()) {
      log.error("createNewRecord:: failed to create Instance entity, response status: {}",
        createdInstanceResponse.getStatusCode().value());
      throw new UnexpectedException("Failed to create Instance entity");
    }
    var externalInstance = createdInstanceResponse.getBody();
    var mappedInstanceJson = JsonObject.mapFrom(mappedInstance);
    var mappedInstanceWithTitles = org.folio.qm.client.model.instance.Instance.fromJson(mappedInstanceJson);
    var titles = precedingSucceedingTitlesHelper.createPrecedingSucceedingTitles(mappedInstanceWithTitles);
    log.info("createNewRecord:: preceding/succeeding titles for instance id: {}", externalInstance.getId());
    var titlesJson = JsonObject.mapFrom(titles);
    org.folio.qm.client.model.PrecedingSucceedingTitleCollection titleCollection = titlesJson.mapTo(
      org.folio.qm.client.model.PrecedingSucceedingTitleCollection.class);
    log.info("createNewRecord:: posting preceding/succeeding titles for instance id: {} ", externalInstance.getId());
    // Create preceding/succeeding titles in the mod-inventory-storage module
    titleCollection.getPrecedingSucceedingTitles().forEach(precedingSucceedingTitlesClient::createTitles);
    // Create snapshot in the SRS module
    createSnapshot(snapshotId);
    var srsRecord = getInstanceSrsRecord(snapshotId, recordDto, externalInstance);
    // Create SRS record in the SRS module
    var createdSrsRecordResponse = sourceStorageClient.createSrsRecord(srsRecord);
    if (!createdSrsRecordResponse.getStatusCode().is2xxSuccessful()) {
      log.error("createNewRecord:: failed to create SRS record for Instance entity, response status: {}",
        createdSrsRecordResponse.getStatusCode().value());
      throw new UnexpectedException("Failed to create SRS record for Instance entity");
    }
    status.setExternalId(UUID.fromString(mappedInstance.getId()));
    status.setMarcId(UUID.fromString(srsRecord.getId()));
  }

  private void createHolding(ParsedRecordDto recordDto, String snapshotId, RecordCreationStatus status) {
    var mappedHoldings = getMappedHoldings(recordDto);
    // instanceId can be received from UI (parsedRecordDtoId or externalId), need to extend the QuickMarcCreate model
    // and avoid to receive it via MARC record field 004 (hrid) and call instanceStorageClient.getInstanceIdByHrid()
    var instanceHrid = getInstanceHrid(mappedHoldings, recordDto);
    var instanceId = instanceStorageClient.getInstanceIdByHrid(instanceHrid).getInstanceId();
    mappedHoldings.setInstanceId(instanceId);
    var source = holdingsSourceClient.getHoldingSourceByName(MARC_NAME);
    mappedHoldings.setSourceId(source.getSourceId());
    mappedHoldings.setDiscoverySuppress(recordDto.getAdditionalInfo().isSuppressDiscovery());
    setMetadata(mappedHoldings);
    var holdingsRecord = JsonObject.mapFrom(mappedHoldings).mapTo(HoldingsRecord.class);

    //Create Holding in the mod-inventory-storage module
    var response = holdingsStorageClient.createHolding(holdingsRecord);
    if (!response.getStatusCode().is2xxSuccessful()) {
      log.error("createNewRecord:: failed to create Holding entity, response status: {}",
        response.getStatusCode().value());
      throw new UnexpectedException("Failed to create Holding entity");
    }
    var externalHolding = response.getBody();
    // Create snapshot in the SRS module
    createSnapshot(snapshotId);
    // Verify instance hrid
    verifyInstanceHrid(instanceHrid);
    // Create SRS record in the SRS module
    assert externalHolding != null;
    var srsRecord = getHoldingSrsRecord(snapshotId, recordDto, externalHolding, instanceHrid);
    var createdSrsRecordResponse = sourceStorageClient.createSrsRecord(srsRecord);
    if (!createdSrsRecordResponse.getStatusCode().is2xxSuccessful()) {
      log.error("createNewRecord:: failed to create SRS record for Holding entity, response status: {}",
        createdSrsRecordResponse.getStatusCode().value());
      throw new UnexpectedException("Failed to create SRS record for Holding entity");
    }
    status.setExternalId(UUID.fromString(mappedHoldings.getId()));
    status.setMarcId(UUID.fromString(srsRecord.getId()));
  }

  private void createAuthority(ParsedRecordDto recordDto, String snapshotId, RecordCreationStatus status) {
    var authorityId = UUID.randomUUID().toString();
    var authority = getAuthority(recordDto, authorityId);
    // Create authority in the mod-entities-links
    var createdAuthorityResponse = createAuthority(authority);
    // Create snapshot in the SRS module
    createSnapshot(snapshotId);
    // Create SRS record  in the SRS module
    var srsRecord = getAuthoritySrsRecord(snapshotId, recordDto, authorityId);
    srsRecord.setMetadata(createdAuthorityResponse.getMetadata());
    var createdSrsRecordResponse = sourceStorageClient.createSrsRecord(srsRecord);
    if (!createdSrsRecordResponse.getStatusCode().is2xxSuccessful()) {
      log.error("createNewRecord:: failed to create SRS record for Authority entity, response status: {}",
        createdSrsRecordResponse.getStatusCode().value());
      throw new UnexpectedException("Failed to create SRS record for Authority entity");
    }
    status.setExternalId(UUID.fromString(authorityId));
    status.setMarcId(UUID.fromString(srsRecord.getId()));
  }

  private Authority createAuthority(Authority mappedAuthority) {
    ResponseEntity<Authority> response = authorityStorageClient.createAuthority(mappedAuthority);
    if (response.getStatusCode().is2xxSuccessful()) {
      log.info("createNewRecord:: new authority created with id: {}", response.getBody().getId());
      return response.getBody();
    } else {
      log.error("createNewRecord:: failed to create new authority, response status: {}",
        response.getStatusCode().value());
      throw new UnexpectedException("Failed to create new authority record");
    }
  }

  private CreationStatus getCreationStatus(RecordCreationStatus status) {
    var statusCreation = statusService.save(status);
    log.info("createNewRecord:: new quickMarc created with qmRecordId: {}", status.getJobExecutionId());
    var creationStatus = new CreationStatus();
    creationStatus.setQmRecordId(statusCreation.getId());
    creationStatus.setStatus(CREATED);
    creationStatus.setMarcId(statusCreation.getMarcId());
    creationStatus.setJobExecutionId(statusCreation.getJobExecutionId());
    return creationStatus;
  }

  private String getInstanceHrid(Holdings mappedHoldings, ParsedRecordDto recordDto) {
    if (isEmpty(mappedHoldings.getInstanceId())) {
      var instanceHrid = getControlFieldValue(recordDto.getParsedRecord().getContent(), "004");
      if (isBlank(instanceHrid)) {
        log.warn(FIELD_004_MARC_HOLDINGS_NOT_NULL);
        throw new EventProcessingException(FIELD_004_MARC_HOLDINGS_NOT_NULL);
      }
      return instanceHrid;
    }
    return null;
  }

  private Record getAuthoritySrsRecord(String snapshotId, ParsedRecordDto recordDto, String authorityId) {
    var srsRecord = new Record();
    var recordId = UUID.randomUUID().toString();
    srsRecord.setId(recordId);
    srsRecord.setOrder(0);
    srsRecord.setMatchedId(recordId);
    srsRecord.setRecordType(Record.RecordType.MARC_AUTHORITY);
    srsRecord.setState(Record.State.ACTUAL);
    srsRecord.setSnapshotId(snapshotId);
    srsRecord.setRawRecord(toRawRecord(recordDto.getParsedRecord(), recordId));
    srsRecord.setParsedRecord(new org.folio.qm.client.model.ParsedRecord()
      .setId(UUID.fromString(recordId))
      .setContent(recordDto.getParsedRecord().getContent()));
    setExternalIdsHolderForAuthority(authorityId, srsRecord);
    setMetadata(srsRecord);
    addFieldToMarcRecord(srsRecord, TAG_999, SUBFIELD_I, authorityId);
    return srsRecord;
  }

  private Record getHoldingSrsRecord(String snapshotId, ParsedRecordDto recordDto, HoldingsRecord externalHolding,
                                     String instanceHrid) {
    var srsRecord = new Record();
    var recordId = UUID.randomUUID().toString();
    srsRecord.setId(recordId);
    srsRecord.setOrder(0);
    srsRecord.setMatchedId(recordId);
    srsRecord.setRecordType(Record.RecordType.MARC_HOLDING);
    srsRecord.setState(Record.State.ACTUAL);
    srsRecord.setMetadata(externalHolding.getMetadata());
    srsRecord.setSnapshotId(snapshotId);
    srsRecord.setAdditionalInfo(new AdditionalInfo().setSuppressDiscovery(externalHolding.getDiscoverySuppress()));
    srsRecord.setRawRecord(toRawRecord(recordDto.getParsedRecord(), recordId));
    srsRecord.setParsedRecord(new org.folio.qm.client.model.ParsedRecord()
      .setId(UUID.fromString(recordId))
      .setContent(recordDto.getParsedRecord().getContent()));
    setExternalIdsHolderForHolding(externalHolding.getId(), srsRecord, instanceHrid);
    addFieldToMarcRecord(srsRecord, TAG_999, SUBFIELD_I, externalHolding.getId());

    addControlledFieldToMarcRecord(srsRecord, TAG_001, externalHolding.getHrid(),
      AdditionalFieldsUtil::addControlledFieldToMarcRecord);
    removeField(srsRecord, TAG_003);
    return srsRecord;
  }

  private Record getInstanceSrsRecord(String snapshotId, ParsedRecordDto recordDto,
                                      org.folio.qm.client.model.Instance externalInstance) {
    var srsRecord = new Record();
    var recordId = UUID.randomUUID().toString();
    srsRecord.setId(recordId);
    srsRecord.setOrder(0);
    srsRecord.setMatchedId(recordId);
    srsRecord.setRecordType(Record.RecordType.MARC_BIB);
    srsRecord.setState(Record.State.ACTUAL);
    srsRecord.setMetadata(externalInstance.getMetadata());
    srsRecord.setSnapshotId(snapshotId);
    srsRecord.setAdditionalInfo(new AdditionalInfo().setSuppressDiscovery(externalInstance.getDiscoverySuppress()));
    srsRecord.setRawRecord(toRawRecord(recordDto.getParsedRecord(), recordId));
    srsRecord.setParsedRecord(new org.folio.qm.client.model.ParsedRecord()
      .setId(UUID.fromString(recordId))
      .setContent(recordDto.getParsedRecord().getContent()));
    setExternalIdsHolderForInstance(externalInstance.getId(), srsRecord, externalInstance.getHrid());
    addFieldToMarcRecord(srsRecord, TAG_999, SUBFIELD_I, externalInstance.getId());
    addControlledFieldToMarcRecord(srsRecord, TAG_001, externalInstance.getHrid(),
      AdditionalFieldsUtil::addControlledFieldToMarcRecord);

    removeField(srsRecord, TAG_003);
    normalize035(srsRecord);
    return srsRecord;
  }

  private void setMetadata(Record srsRecord) {
    var userId = folioExecutionContext.getUserId();
    var metadata = new Metadata()
      .withCreatedByUserId(userId != null ? userId.toString() : null)
      .withUpdatedByUserId(userId != null ? userId.toString() : null);
    srsRecord.setMetadata(metadata);
  }

  private void setMetadata(Holdings holdings) {
    var userId = folioExecutionContext.getUserId();
    var metadata = new Metadata()
      .withCreatedByUserId(userId != null ? userId.toString() : null)
      .withUpdatedByUserId(userId != null ? userId.toString() : null)
      .withCreatedDate(new Date())
      .withUpdatedDate(new Date());
    holdings.setMetadata(metadata);
  }

  private void setMetadata(org.folio.Instance instance) {
    var userId = folioExecutionContext.getUserId();
    var metadata = new Metadata()
      .withCreatedByUserId(userId != null ? userId.toString() : null)
      .withUpdatedByUserId(userId != null ? userId.toString() : null)
      .withCreatedDate(new Date())
      .withUpdatedDate(new Date());
    instance.setMetadata(metadata);
  }

  private RawRecord toRawRecord(org.folio.qm.client.model.ParsedRecord parsedRecord, String recordId) {
    var jsonString = objectToJsonString(parsedRecord.getContent());
    return new RawRecord()
      .withId(recordId)
      .withContent(jsonString);
  }

  private void setExternalIdsHolderForAuthority(String authorityId, Record srsRecord) {
    var externalIdsHolder = new ExternalIdsHolder();
    externalIdsHolder.setAuthorityId(UUID.fromString(authorityId));
    Optional.ofNullable(getControlFieldValue(srsRecord, TAG_001))
      .map(String::trim)
      .ifPresent(externalIdsHolder::setAuthorityHrid);
    srsRecord.setExternalIdsHolder(externalIdsHolder);
  }

  private void setExternalIdsHolderForHolding(String holdingId, Record srsRecord, String instanceHrid) {
    var externalIdsHolder = new ExternalIdsHolder();
    externalIdsHolder.setHoldingsId(UUID.fromString(holdingId));
    Optional.ofNullable(instanceHrid)
      .map(String::trim)
      .ifPresent(externalIdsHolder::setHoldingsHrid);
    srsRecord.setExternalIdsHolder(externalIdsHolder);
  }

  private void setExternalIdsHolderForInstance(String instanceId, Record srsRecord, String instanceHrid) {
    var externalIdsHolder = new ExternalIdsHolder();
    externalIdsHolder.setInstanceId(UUID.fromString(instanceId));
    Optional.ofNullable(instanceHrid)
      .map(String::trim)
      .ifPresent(externalIdsHolder::setInstanceHrid);
    srsRecord.setExternalIdsHolder(externalIdsHolder);
  }

  private Holdings getMappedHoldings(ParsedRecordDto recordDto) {
    var mappingMetadata = mappingMetadataProvider.getMappingData("marc-holdings");
    var mappingRules = mappingMetadata.mappingRules();
    var mappingParameters = mappingMetadata.mappingParameters();
    var parsedRecord = new ParsedRecord().withContent(recordDto.getParsedRecord().getContent());
    var parsedRecordJson = retrieveParsedContent(parsedRecord);
    RecordMapper<Holdings> recordMapper = RecordMapperBuilder.buildMapper("MARC_HOLDINGS");
    var mappedHoldings = recordMapper.mapRecord(parsedRecordJson, mappingParameters, mappingRules);
    var holdingId = UUID.randomUUID().toString();
    mappedHoldings.setId(holdingId);
    if (isEmpty(mappedHoldings.getPermanentLocationId())) {
      log.warn(PERMANENT_LOCATION_ID_ERROR_MESSAGE);
      throw new UnexpectedException(PERMANENT_LOCATION_ID_ERROR_MESSAGE);
    }
    return mappedHoldings;
  }

  private org.folio.Instance getMappedInstance(ParsedRecordDto recordDto) {
    var mappingMetadata = mappingMetadataProvider.getMappingData("marc-bib");
    var mappingRules = mappingMetadata.mappingRules();
    var mappingParameters = mappingMetadata.mappingParameters();
    var parsedRecord = new ParsedRecord().withContent(recordDto.getParsedRecord().getContent());
    var parsedRecordJson = retrieveParsedContent(parsedRecord);
    RecordMapper<org.folio.Instance> recordMapper = RecordMapperBuilder.buildMapper(RecordTypeEnum.BIB.getValue());
    var mappedInstance = recordMapper.mapRecord(parsedRecordJson, mappingParameters, mappingRules);
    var instanceId = UUID.randomUUID().toString();
    mappedInstance.setId(instanceId);
    return mappedInstance;
  }

  private void verifyInstanceHrid(String instanceHrid) {
    assert instanceHrid != null;
    var isMarcBibValid = sourceStorageClient.verifyMarcBibRecords(List.of(instanceHrid)).isMarcBibIdsValid();
    if (!isMarcBibValid) {
      //getInvalidMarcBibIdsForConsortium
      log.error("createNewRecord:: Failed to verify instance hrid: {}", instanceHrid);
      throw new UnexpectedException("Failed to verify instance hrid: " + instanceHrid);
    }
  }

  private Authority getAuthority(ParsedRecordDto recordDto, String authorityId) {
    var mappingMetadata = mappingMetadataProvider.getMappingData("marc-authority");
    var mappingRules = mappingMetadata.mappingRules();
    var mappingParameters = mappingMetadata.mappingParameters();

    var parsedRecord = new ParsedRecord().withContent(recordDto.getParsedRecord().getContent());

    RecordMapper<Authority> recordMapper = isAuthorityExtendedMode()
      ? RecordMapperBuilder.buildMapper(ActionProfile.FolioRecord.MARC_AUTHORITY_EXTENDED.value())
      : RecordMapperBuilder.buildMapper(RecordTypeEnum.AUTHORITY.getValue());
    var parsedRecordJson = retrieveParsedContent(parsedRecord);
    var mappedAuthority = recordMapper.mapRecord(parsedRecordJson, mappingParameters, mappingRules);

    mappedAuthority.setId(authorityId);
    mappedAuthority.setSource(Authority.Source.MARC);
    return mappedAuthority;
  }

  private void createSnapshot(String snapshotId) {
    var snapshot = new Snapshot();
    snapshot.setJobExecutionId(snapshotId);
    snapshot.setStatus(Snapshot.Status.COMMITTED);
    snapshot.setProcessingStartedDate(new Date());
    var snapshotResponse = sourceStorageClient.createSnapshot(snapshot);
    if (snapshotResponse.getStatusCode().is2xxSuccessful()) {
      log.info("createNewRecord:: snapshot created for new authority with id: {}", snapshotId);
    } else {
      log.error("createNewRecord:: failed to create snapshot for new authority, response status: {}",
        snapshotResponse.getStatusCode().value());
      throw new UnexpectedException("Failed to create snapshot for new authority record");
    }
  }
}
