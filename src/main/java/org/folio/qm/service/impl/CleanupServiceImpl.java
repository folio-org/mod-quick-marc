package org.folio.qm.service.impl;

import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_DAY;
import static org.folio.qm.util.TenantContextUtils.getFolioExecutionContextCopyForTenant;
import static org.folio.qm.util.TenantContextUtils.runInFolioContext;

import java.sql.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.holder.TenantsHolder;
import org.folio.qm.service.CleanupService;
import org.folio.qm.service.StatusService;
import org.folio.spring.FolioExecutionContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class CleanupServiceImpl implements CleanupService {

  private final TenantsHolder tenantsHolder;
  private final FolioExecutionContext context;
  private final StatusService statusService;

  @Override
  @Scheduled(initialDelayString = "${folio.qm.creation-status.clear.initial-delay-ms}",
             fixedDelayString = "${folio.qm.creation-status.clear.fixed-delay-ms}")
  public void clearCreationStatusesForAllTenants() {
    log.trace("clearCreationStatusesForAllTenants:: trying to clean up jobs from DB");
    var yesterdayTimestamp = new Timestamp(System.currentTimeMillis() - MILLIS_PER_DAY);
    var tenants = tenantsHolder.getAll();
    log.info("clearCreationStatusesForAllTenants:: Cleaning up jobs for tenants: {} older than {}", tenants,
      yesterdayTimestamp);
    for (var tenant : tenants) {
      runInFolioContext(getFolioExecutionContextCopyForTenant(context, tenant),
        () -> statusService.removeOlderThan(yesterdayTimestamp));
    }
  }
}
