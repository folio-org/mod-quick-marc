package org.folio.qm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.folio.qm.util.TenantUtils.getFolioExecutionContextCopyForTenant;
import static org.folio.qm.utils.APITestUtils.TENANT_ID;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.scope.EmptyFolioExecutionContextHolder;

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
