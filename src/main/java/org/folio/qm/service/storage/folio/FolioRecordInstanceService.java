package org.folio.qm.service.storage.folio;

import java.util.Optional;
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
        return new NotFoundException(String.format("Authority record with id: %s not found", id));
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
    return Optional.ofNullable(storageClient.getInstanceByHrid(instanceHrid).getInstanceId())
      .orElseThrow(() -> new IllegalStateException(
        String.format("Instance ID is missing or more than one instance found for HRID: %s", instanceHrid)));
  }

  private void updateTitles(String id, InstanceRecord updatedInstance) {
    log.trace("updateTitles:: Updating preceding/succeeding titles for instance id: {}", id);
    var titles = PrecedingSucceedingTitlesHelper.collectPrecedingSucceedingTitles(updatedInstance);
    precedingSucceedingTitlesClient.updateTitles(id, titles);
    log.debug("Preceding/succeeding title records for instance id: {} have been updated successfully",
      updatedInstance.getId());
  }
}
