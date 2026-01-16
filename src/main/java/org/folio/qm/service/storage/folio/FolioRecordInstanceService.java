package org.folio.qm.service.storage.folio;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.client.InstanceStorageClient;
import org.folio.qm.client.PrecedingSucceedingTitlesClient;
import org.folio.qm.domain.model.InstanceRecord;
import org.folio.qm.service.support.PrecedingSucceedingTitlesHelper;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class FolioRecordInstanceService implements FolioRecordService<InstanceRecord> {

  private final InstanceStorageClient storageClient;
  private final PrecedingSucceedingTitlesClient precedingSucceedingTitlesClient;

  @Override
  public InstanceRecord get(UUID id) {
    log.debug("get:: Retrieving instance record by id: {}", id);
    return storageClient.getInstanceById(id)
      .orElseThrow(() -> {
        log.error("get:: Instance record not found with id: {}", id);
        return new NotFoundException(String.format("Instance record with id: %s not found", id));
      });
  }

  @Override
  public InstanceRecord create(InstanceRecord folioRecord) {
    log.debug("create:: Creating instance record");
    var instance = storageClient.createInstance(folioRecord);
    log.info("create:: Instance record created with id: {}", instance.getId());
    folioRecord.setId(instance.getId());
    updateTitles(instance.getId(), folioRecord);
    return instance;
  }

  @Override
  public void update(UUID id, InstanceRecord folioRecord) {
    log.debug("update:: Updating instance record with id: {}", id);
    storageClient.updateInstance(id, folioRecord);
    log.info("update:: Instance record updated successfully with id: {}", id);
    updateTitles(id.toString(), folioRecord);
  }

  public String getInstanceIdByHrid(String instanceHrid) {
    var response = storageClient.getInstanceByHrid(instanceHrid);
    long totalRecords = response.getTotalRecords() != null ? response.getTotalRecords() : 0;

    if (totalRecords == 0) {
      log.error("getInstanceIdByHrid:: No instance found for HRID: {}", instanceHrid);
      throw new NotFoundException(String.format("No instance found for HRID: %s", instanceHrid));
    }
    if (totalRecords > 1) {
      log.error("getInstanceIdByHrid:: Multiple instances found for HRID: {}", instanceHrid);
      throw new IllegalStateException(String.format("Multiple instances found for HRID: %s", instanceHrid));
    }
    return response.getInstances().getFirst().getId();
  }

  private void updateTitles(String id, InstanceRecord updatedInstance) {
    log.trace("updateTitles:: Updating preceding/succeeding titles for instance id: {}", id);
    var titles = PrecedingSucceedingTitlesHelper.collectPrecedingSucceedingTitles(updatedInstance);
    precedingSucceedingTitlesClient.updateTitles(id, titles);
    log.debug("Preceding/succeeding title records for instance id: {} have been updated successfully",
      updatedInstance.getId());
  }
}
