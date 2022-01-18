package org.folio.qm.service.impl;

import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_DAY;

import static org.folio.qm.util.TenantUtils.getFolioExecutionContextCopyForTenant;
import static org.folio.spring.scope.FolioExecutionScopeExecutionContextManager.beginFolioExecutionContext;
import static org.folio.spring.scope.FolioExecutionScopeExecutionContextManager.endFolioExecutionContext;

import java.sql.Timestamp;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.folio.qm.service.CleanupService;
import org.folio.qm.service.CreationStatusService;
import org.folio.spring.FolioExecutionContext;

@Service
@RequiredArgsConstructor
public class CleanupServiceImpl implements CleanupService {

  @Qualifier("tenants") private final Set<String> tenants;
  private final FolioExecutionContext context;
  private final CreationStatusService creationStatusService;

  @Override
  @Scheduled(initialDelayString = "${folio.qm.creation-status.clear.initial-delay-ms}",
    fixedDelayString = "${folio.qm.creation-status.clear.fixed-delay-ms}")
  public void clearCreationStatusesForAllTenants() {
    var yesterdayTimestamp = new Timestamp(System.currentTimeMillis() - MILLIS_PER_DAY);
    endFolioExecutionContext();

    for (var tenant : tenants) {
      beginFolioExecutionContext(getFolioExecutionContextCopyForTenant(context, tenant));
      try {
        creationStatusService.removeOlderThan(yesterdayTimestamp);
      } finally {
        endFolioExecutionContext();
      }
    }
  }
}
