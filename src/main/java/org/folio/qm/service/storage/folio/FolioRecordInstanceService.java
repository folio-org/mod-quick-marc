package org.folio.qm.service.storage.folio;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.folio.qm.client.InstanceStorageClient;
import org.folio.qm.client.PrecedingSucceedingTitlesClient;
import org.folio.qm.domain.model.InstanceRecord;
import org.folio.qm.service.mapping.InstanceToInstanceRecordMapper;
import org.folio.qm.service.storage.tenant.UserTenantsService;
import org.folio.qm.service.support.PrecedingSucceedingTitlesHelper;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.InstancePrecedingSucceedingTitles;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.folio.spring.scope.FolioExecutionContextService;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class FolioRecordInstanceService implements FolioRecordService<InstanceRecord> {

  private final InstanceStorageClient storageClient;
  private final PrecedingSucceedingTitlesClient precedingSucceedingTitlesClient;
  private final InstanceToInstanceRecordMapper instanceMapper;
  private final UserTenantsService userTenantsService;
  private final FolioExecutionContext folioExecutionContext;
  private final FolioExecutionContextService executionService;

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
    var response = storageClient.getInstances(instanceHrid);
    long totalRecords = response.getTotalRecords() != null ? response.getTotalRecords() : 0;
    if (totalRecords == 0 && isConsortiumMemberTenant()) {
      return createInstanceShadowCopy(instanceHrid);
    }
    validateTotalRecords(instanceHrid, totalRecords, folioExecutionContext.getTenantId());
    return response.getInstances().getFirst().getId();
  }

  private void updateTitles(String id, InstanceRecord updatedInstance) {
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

  private boolean isConsortiumMemberTenant() {
    var tenantId = folioExecutionContext.getTenantId();
    var consortiumCentralTenant = getConsortiumCentralTenant(tenantId);
    return consortiumCentralTenant != null && !consortiumCentralTenant.equals(tenantId);
  }

  private String createInstanceShadowCopy(String instanceHrid) {
    var sharedInstanceWithTitles = getSharedInstanceWithTitles(instanceHrid);
    var instance = sharedInstanceWithTitles.getLeft();
    var titles = sharedInstanceWithTitles.getRight();

    var instanceRecord = instanceMapper.toInstanceRecord(instance);
    var instanceShadowCopy = storageClient.createInstance(instanceRecord);
    if (titles != null && titles.getTotalRecords() != null && titles.getTotalRecords() > 0) {
      precedingSucceedingTitlesClient.updateTitles(instanceShadowCopy.getId(), titles);
    }
    return instanceShadowCopy.getId();
  }

  private Pair<Instance, InstancePrecedingSucceedingTitles> getSharedInstanceWithTitles(String instanceHrid) {
    var consortiumCentralTenant = getConsortiumCentralTenant(folioExecutionContext.getTenantId());
    return executionService.execute(consortiumCentralTenant, folioExecutionContext,
      () -> {
        var sharedInstances = storageClient.getInstances(instanceHrid);
        var totalRecords = sharedInstances.getTotalRecords() != null ? sharedInstances.getTotalRecords() : 0;

        validateTotalRecords(instanceHrid, totalRecords, consortiumCentralTenant);

        var sharedInstance = sharedInstances.getInstances().getFirst();
        var titles = precedingSucceedingTitlesClient.getTitles(sharedInstance.getId());
        return Pair.of(sharedInstance, titles);
      }
    );
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
