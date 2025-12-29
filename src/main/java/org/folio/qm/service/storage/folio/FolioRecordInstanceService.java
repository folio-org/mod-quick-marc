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
    return storageClient.getInstanceById(id)
      .orElseThrow(() -> new NotFoundException(String.format("Authority record with id: %s not found", id)));
  }

  @Override
  public InstanceRecord create(InstanceRecord folioRecord) {
    var instance = storageClient.createInstance(folioRecord);
    updateTitles(instance.getId(), folioRecord);
    return instance;
  }

  @Override
  public void update(UUID id, InstanceRecord folioRecord) {
    storageClient.updateInstance(id, folioRecord);
    updateTitles(id.toString(), folioRecord);
  }

  private void updateTitles(String id, InstanceRecord updatedInstance) {
    var titles = PrecedingSucceedingTitlesHelper.updatePrecedingSucceedingTitles(updatedInstance);
    precedingSucceedingTitlesClient.updateTitles(id, titles);
    log.debug("Preceding/succeeding title records for instance id: {} have been updated successfully",
      updatedInstance.getId());
  }
}
