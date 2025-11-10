package org.folio.qm.service.impl;

import static org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.folio.qm.client.model.RecordTypeEnum.BIB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.client.InstanceStorageClient;
import org.folio.qm.client.PrecedingSucceedingTitlesClient;
import org.folio.qm.client.SourceStorageClient;
import org.folio.qm.client.model.Instance;
import org.folio.qm.client.model.MappingRecordTypeEnum;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.RecordTypeEnum;
import org.folio.qm.mapper.ExternalIdsHolderMapper;
import org.folio.qm.mapper.InstanceRecordMapper;
import org.folio.qm.service.RecordService;
import org.folio.qm.service.support.MappingMetadataProvider;
import org.folio.qm.service.support.PrecedingSucceedingTitlesHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

@Service
@Log4j2
public class InstanceRecordService extends RecordService<org.folio.Instance> {

  private final InstanceStorageClient instanceStorageClient;
  private final PrecedingSucceedingTitlesHelper precedingSucceedingTitlesHelper;
  private final PrecedingSucceedingTitlesClient precedingSucceedingTitlesClient;
  private final InstanceRecordMapper mapper;

  protected InstanceRecordService(MappingMetadataProvider mappingMetadataProvider,
                                  SourceStorageClient sourceStorageClient,
                                  ExternalIdsHolderMapper externalIdsHolderMapper,
                                  InstanceStorageClient instanceStorageClient,
                                  PrecedingSucceedingTitlesHelper precedingSucceedingTitlesHelper,
                                  PrecedingSucceedingTitlesClient precedingSucceedingTitlesClient,
                                  InstanceRecordMapper mapper) {
    super(mappingMetadataProvider, sourceStorageClient, externalIdsHolderMapper);
    this.instanceStorageClient = instanceStorageClient;
    this.precedingSucceedingTitlesHelper = precedingSucceedingTitlesHelper;
    this.precedingSucceedingTitlesClient = precedingSucceedingTitlesClient;
    this.mapper = mapper;
  }

  @Override
  public RecordTypeEnum supportedType() {
    return BIB;
  }

  @Override
  public void update(UUID parsedRecordId, DeferredResult<ResponseEntity<Void>> updateResult,
                     ParsedRecordDto parsedRecordDto) {
    try {
      org.folio.Instance mappedInstance = getMappedRecord(parsedRecordDto, MappingRecordTypeEnum.MARC_BIB.getValue(),
        BIB.getValue());
      if (mappedInstance == null) {
        handleError(parsedRecordDto.getId(), updateResult,
          String.format("getMappedRecord:: mapping metadata not found for MARC-BIB record with parsedRecordId: %s",
            parsedRecordId));
        return;
      }
      var instanceId = parsedRecordDto.getExternalIdsHolder().getInstanceId().toString();
      var existingInstance = instanceStorageClient.getInstanceById(instanceId);
      if (existingInstance == null) {
        handleError(parsedRecordId, updateResult, String.format("Instance record with id: %s not found", instanceId));
        return;
      }
      var updatedInstance = updatedInstance(existingInstance, mappedInstance, instanceId);
      updateTitles(updatedInstance, instanceId);
      updateSrsRecord(parsedRecordId, updateResult, parsedRecordDto);
    } catch (Exception e) {
      handleError(parsedRecordId, updateResult,
        String.format("Error updating Instance record for parsedRecordId: %s, error: %s",
          parsedRecordId, e.getMessage()), e);
    }
  }

  private Instance updatedInstance(Instance existingInstance, org.folio.Instance mappedInstance, String instanceId) {
    var updatedInstance = mergeRecords(existingInstance, mappedInstance);
    instanceStorageClient.updateInstance(instanceId, updatedInstance);
    log.debug("Instance record with id: {} has been updated successfully", instanceId);
    return updatedInstance;
  }

  private void updateTitles(Instance updatedInstance, String instanceId) {
    var titles = precedingSucceedingTitlesHelper.updatePrecedingSucceedingTitles(updatedInstance);
    precedingSucceedingTitlesClient.updateTitles(instanceId, titles);
    log.debug("Preceding/succeeding title records for instance id: {} have been updated successfully", instanceId);
  }

  private Instance mergeRecords(Instance existingInstance, org.folio.Instance mappedInstance) {

    log.debug("mergeRecords:: Merging existing instance with id: {} and mapped instance",
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
      mergeInstances(existingInstance, mappedInstance);
      return existingInstance;
    } catch (Exception e) {
      log.error("Error updating instance", e);
      throw e;
    }
  }

  private void mergeInstances(Instance existing, org.folio.Instance mapped) {
    var statisticalCodeIds = new HashSet<>(Optional.ofNullable(existing.getStatisticalCodeIds())
      .orElse(Collections.emptySet()));
    var natureOfContentTermIds = new HashSet<>(Optional.ofNullable(existing.getNatureOfContentTermIds())
      .orElse(Collections.emptySet()));
    @SuppressWarnings("VariableDeclarationUsageDistance")
    var administrativeNotes = new ArrayList<>(Optional.ofNullable(existing.getAdministrativeNotes())
      .orElse(Collections.emptyList()));
    mapper.merge(mapped, existing);
    existing.setStatisticalCodeIds(statisticalCodeIds);
    existing.setNatureOfContentTermIds(natureOfContentTermIds);
    existing.setAdministrativeNotes(administrativeNotes);
  }
}
