package org.folio.qm.util;

import static org.folio.qm.support.utils.ApiTestUtils.TENANT_ID;
import static org.folio.qm.util.TenantContextUtils.getFolioExecutionContextCopyForTenant;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.scope.EmptyFolioExecutionContextHolder;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class TenantContextUtilsTest {

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
