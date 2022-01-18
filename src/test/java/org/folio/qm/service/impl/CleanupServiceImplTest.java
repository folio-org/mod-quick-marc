package org.folio.qm.service.impl;

import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_DAY;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Timestamp;
import java.util.Set;

import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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
  private Set<String> tenants = Set.of("tenant1", "tenant2");

  @Mock
  private CreationStatusServiceImpl creationStatusService;

  @InjectMocks
  private CleanupServiceImpl cleanupService;

  @Captor
  private ArgumentCaptor<Timestamp> timestampCaptor;

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
        times(tenants.size()));
      contextManager.verify(() -> FolioExecutionScopeExecutionContextManager.beginFolioExecutionContext(context),
        times(tenants.size()));
      contextManager.verify(FolioExecutionScopeExecutionContextManager::endFolioExecutionContext,
        times(tenants.size() + 1));
    }

    verify(creationStatusService, times(tenants.size()))
      .removeOlderThan(timestampCaptor.capture());

    var yesterdayTime = System.currentTimeMillis() - MILLIS_PER_DAY;
    var allCallsWereForYesterday = timestampCaptor.getAllValues().stream()
      .map(Timestamp::getTime)
      .allMatch(actualTimestamp -> actualTimestamp <= yesterdayTime);
    assertTrue(allCallsWereForYesterday);
  }

}