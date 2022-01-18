package org.folio.qm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.folio.qm.util.TenantUtils.getFolioExecutionContextCopyForTenant;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.scope.EmptyFolioExecutionContextHolder;

class TenantUtilsTest {

  private static final String TEST_TENANT = "test";

  @Test
  void shouldSetTenantIdIfHeadersPassed() {
    var context = new DefaultFolioExecutionContext(null, new HashMap<>());

    var result = getFolioExecutionContextCopyForTenant(context, TEST_TENANT);

    assertEquals(TEST_TENANT, result.getTenantId());
  }

  @Test
  void shouldSetTenantIdIfNoHeadersPassed() {
    var context = new EmptyFolioExecutionContextHolder(null).getEmptyFolioExecutionContext();

    var result = getFolioExecutionContextCopyForTenant(context, TEST_TENANT);

    assertEquals(TEST_TENANT, result.getTenantId());
  }
}
