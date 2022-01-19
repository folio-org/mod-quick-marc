package org.folio.qm.service.impl;

import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_DAY;

import static org.folio.qm.util.TenantUtils.getFolioExecutionContextCopyForTenant;
import static org.folio.spring.scope.FolioExecutionScopeExecutionContextManager.beginFolioExecutionContext;
import static org.folio.spring.scope.FolioExecutionScopeExecutionContextManager.endFolioExecutionContext;

import java.sql.Timestamp;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.folio.qm.holder.TenantsHolder;
import org.folio.qm.service.CleanupService;
import org.folio.qm.service.CreationStatusService;
import org.folio.spring.FolioExecutionContext;

@Service
@RequiredArgsConstructor
public class CleanupServiceImpl implements CleanupService {

  private final TenantsHolder tenantsHolder;
  private final FolioExecutionContext context;
  private final CreationStatusService creationStatusService;

  @Override
  @Scheduled(initialDelayString = "${folio.qm.creation-status.clear.initial-delay-ms}",
    fixedDelayString = "${folio.qm.creation-status.clear.fixed-delay-ms}")
  public void clearCreationStatusesForAllTenants() {
    var yesterdayTimestamp = new Timestamp(System.currentTimeMillis() - MILLIS_PER_DAY);
    endFolioExecutionContext();

    for (var tenant : tenantsHolder.getAll()) {
      beginFolioExecutionContext(getFolioExecutionContextCopyForTenant(context, tenant));
      try {
        creationStatusService.removeOlderThan(yesterdayTimestamp);
      } finally {
        endFolioExecutionContext();
      }
    }
  }
}
