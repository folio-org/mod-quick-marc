package org.folio.qm.service.impl;

import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_DAY;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Timestamp;
import org.folio.qm.holder.TenantsHolder;
import org.folio.qm.holder.impl.TenantsHolderImpl;
import org.folio.qm.util.TenantContextUtils;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.scope.EmptyFolioExecutionContextHolder;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class CleanupServiceImplTest {

  private static final FolioExecutionContext CONTEXT = new EmptyFolioExecutionContextHolder(null)
    .getEmptyFolioExecutionContext();

  @Spy
  private TenantsHolder tenantsHolder = new TenantsHolderImpl();

  @Mock
  private StatusServiceImpl statusService;

  @InjectMocks
  private CleanupServiceImpl cleanupService;

  @Captor
  private ArgumentCaptor<Timestamp> timestampCaptor;

  @BeforeEach
  void setUpTenants() {
    tenantsHolder.add("tenant1");
    tenantsHolder.add("tenant2");
  }

  @Test
  void shouldClearDataForAllTenants() {
    try (
      var tenantUtils = mockStatic(TenantContextUtils.class)
    ) {
      tenantUtils.when(() -> TenantContextUtils.getFolioExecutionContextCopyForTenant(any(), any()))
        .thenReturn(CONTEXT);
      tenantUtils.when(() -> TenantContextUtils.runInFolioContext(any(), any())).thenCallRealMethod();

      cleanupService.clearCreationStatusesForAllTenants();

      tenantUtils.verify(() -> TenantContextUtils.getFolioExecutionContextCopyForTenant(any(), any()),
        times(tenantsHolder.count()));
    }

    verify(statusService, times(tenantsHolder.count()))
      .removeOlderThan(timestampCaptor.capture());

    var yesterdayTime = System.currentTimeMillis() - MILLIS_PER_DAY;
    var allCallsWereForYesterday = timestampCaptor.getAllValues().stream()
      .map(Timestamp::getTime)
      .allMatch(actualTimestamp -> actualTimestamp <= yesterdayTime);
    assertTrue(allCallsWereForYesterday);
  }
}
