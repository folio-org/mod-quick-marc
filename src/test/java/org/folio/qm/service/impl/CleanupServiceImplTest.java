package org.folio.qm.service.impl;

import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_DAY;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Timestamp;

import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.folio.qm.holder.TenantsHolder;
import org.folio.qm.holder.impl.TenantsHolderImpl;
import org.folio.qm.util.TenantUtils;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.scope.EmptyFolioExecutionContextHolder;
import org.folio.spring.scope.FolioExecutionScopeExecutionContextManager;

@ExtendWith({
  MockitoExtension.class,
  RandomBeansExtension.class
})
class CleanupServiceImplTest {

  private static final FolioExecutionContext context = new EmptyFolioExecutionContextHolder(null)
    .getEmptyFolioExecutionContext();

  @Spy
  private TenantsHolder tenantsHolder = new TenantsHolderImpl();

  @Mock
  private CreationStatusServiceImpl creationStatusService;

  @InjectMocks
  private CleanupServiceImpl cleanupService;

  @Captor
  private ArgumentCaptor<Timestamp> timestampCaptor;

  @BeforeEach
  public void setUpTenants() {
    tenantsHolder.add("tenant1");
    tenantsHolder.add("tenant2");
  }

  @Test
  void shouldClearDataForAllTenants() {
    try (
      var contextManager = mockStatic(FolioExecutionScopeExecutionContextManager.class);
      var tenantUtils = mockStatic(TenantUtils.class)
    ) {
      tenantUtils.when(() -> TenantUtils.getFolioExecutionContextCopyForTenant(any(), any()))
        .thenReturn(context);

      cleanupService.clearCreationStatusesForAllTenants();

      tenantUtils.verify(() -> TenantUtils.getFolioExecutionContextCopyForTenant(any(), any()),
        times(tenantsHolder.count()));
      contextManager.verify(() -> FolioExecutionScopeExecutionContextManager.beginFolioExecutionContext(context),
        times(tenantsHolder.count()));
      contextManager.verify(FolioExecutionScopeExecutionContextManager::endFolioExecutionContext,
        times(tenantsHolder.count() + 1));
    }

    verify(creationStatusService, times(tenantsHolder.count()))
      .removeOlderThan(timestampCaptor.capture());

    var yesterdayTime = System.currentTimeMillis() - MILLIS_PER_DAY;
    var allCallsWereForYesterday = timestampCaptor.getAllValues().stream()
      .map(Timestamp::getTime)
      .allMatch(actualTimestamp -> actualTimestamp <= yesterdayTime);
    assertTrue(allCallsWereForYesterday);
  }

}