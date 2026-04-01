package org.folio.qm.service.storage.folio;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.client.InstanceStorageClient;
import org.folio.qm.client.PrecedingSucceedingTitlesClient;
import org.folio.qm.domain.model.InstanceFolioRecord;
import org.folio.qm.service.storage.tenant.UserTenantsService;
import org.folio.qm.service.support.PrecedingSucceedingTitlesHelper;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.folio.spring.scope.FolioExecutionContextService;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class FolioRecordInstanceService implements FolioRecordService<InstanceFolioRecord> {

  private final InstanceStorageClient storageClient;
  private final PrecedingSucceedingTitlesClient precedingSucceedingTitlesClient;
  private final UserTenantsService userTenantsService;
  private final FolioExecutionContext context;
  private final FolioExecutionContextService executionService;

  @Override
  public InstanceFolioRecord get(UUID id) {
    log.debug("get:: Retrieving instance record by id: {}", id);
    return storageClient.getInstanceById(id)
      .orElseThrow(() -> {
        log.error("get:: Instance record not found with id: {}", id);
        return new NotFoundException(String.format("Instance record with id: %s not found", id));
      });
  }

  @Override
  public InstanceFolioRecord create(InstanceFolioRecord folioRecord) {
    log.debug("create:: Creating instance record");
    var instance = storageClient.createInstance(folioRecord);
    log.info("create:: Instance record created with id: {}", instance.getId());
    folioRecord.setId(instance.getId());
    updateTitles(instance.getId(), folioRecord);
    return instance;
  }

  @Override
  public void update(UUID id, InstanceFolioRecord folioRecord) {
    log.debug("update:: Updating instance record with id: {}", id);
    storageClient.updateInstance(id, folioRecord);
    log.info("update:: Instance record updated successfully with id: {}", id);
    updateTitles(id.toString(), folioRecord);
  }

  public String getInstanceIdByHrid(String instanceHrid) {
    var tenantId = context.getTenantId();
    var instances = storageClient.getInstances(instanceHrid);
    long totalRecords = instances.getTotalRecords() != null ? instances.getTotalRecords() : 0;
    var consortiumCentralTenant = getConsortiumCentralTenant(tenantId);
    var isConsortiumMemberTenant = consortiumCentralTenant != null && !consortiumCentralTenant.equals(tenantId);
    if (totalRecords == 0 && isConsortiumMemberTenant) {
      tenantId = consortiumCentralTenant;
      instances = executionService.execute(tenantId, context, () -> storageClient.getInstances(instanceHrid));
      totalRecords = instances.getTotalRecords() != null ? instances.getTotalRecords() : 0;
    }
    validateTotalRecords(instanceHrid, totalRecords, tenantId);
    return instances.getInstances().getFirst().getId();
  }

  private void updateTitles(String id, InstanceFolioRecord updatedInstance) {
    log.trace("updateTitles:: Updating preceding/succeeding titles for instance id: {}", id);
    var titles = PrecedingSucceedingTitlesHelper.collectPrecedingSucceedingTitles(updatedInstance);
    precedingSucceedingTitlesClient.updateTitles(id, titles);
    log.debug("Preceding/succeeding title records for instance id: {} have been updated successfully",
      updatedInstance.getId());
  }

  private String getConsortiumCentralTenant(String tenantId) {
    var centralTenant = userTenantsService.getCentralTenant(tenantId);
    return centralTenant.orElse(null);
  }

  private void validateTotalRecords(String instanceHrid, long totalRecords, String tenantId) {
    if (totalRecords == 0) {
      var message = "No instance found for HRID: %s in tenant: %s".formatted(instanceHrid, tenantId);
      log.error("getInstanceIdByHrid:: {}", message);
      throw new NotFoundException(message);
    }
    if (totalRecords > 1) {
      var message = "Multiple instances found for HRID: %s in tenant: %s".formatted(instanceHrid, tenantId);
      log.error("getInstanceIdByHrid:: {}", message);
      throw new IllegalStateException(message);
    }
  }
}
