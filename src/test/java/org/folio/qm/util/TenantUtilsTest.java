package org.folio.qm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.folio.qm.support.utils.APITestUtils.TENANT_ID;
import static org.folio.qm.util.TenantUtils.getFolioExecutionContextCopyForTenant;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import org.folio.qm.support.types.UnitTest;
import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.scope.EmptyFolioExecutionContextHolder;

@UnitTest
class TenantUtilsTest {

  @Test
  void shouldSetTenantIdIfHeadersPassed() {
    var context = new DefaultFolioExecutionContext(null, new HashMap<>());

    var result = getFolioExecutionContextCopyForTenant(context, TENANT_ID);

    assertEquals(TENANT_ID, result.getTenantId());
  }

  @Test
  void shouldSetTenantIdIfNoHeadersPassed() {
    var context = new EmptyFolioExecutionContextHolder(null).getEmptyFolioExecutionContext();

    var result = getFolioExecutionContextCopyForTenant(context, TENANT_ID);

    assertEquals(TENANT_ID, result.getTenantId());
  }
}
