package org.folio.qm.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.qm.util.TenantContextUtils.getFolioExecutionContextCopyForTenant;
import static org.folio.qm.util.TenantContextUtils.getFolioExecutionContextFromSpecification;
import static org.folio.support.utils.ApiTestUtils.TENANT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.scope.EmptyFolioExecutionContextHolder;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessageHeaders;

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

  @Test
  void shouldReturnContextWithTenant() {
    var metadata = mock(FolioModuleMetadata.class);
    var tenant = TENANT_ID;
    var url = "url";
    var token = "token";
    var user = UUID.randomUUID().toString();
    var expectedHeaders = Map.of(XOkapiHeaders.TENANT, List.of(tenant), XOkapiHeaders.URL, List.of(url),
      XOkapiHeaders.TOKEN, List.of(token), XOkapiHeaders.USER_ID, List.of(user));
    var headers = Map.<String, Object>of(XOkapiHeaders.URL, headerValue(url), XOkapiHeaders.TOKEN, headerValue(token),
      XOkapiHeaders.USER_ID, headerValue(user));

    var actual = getFolioExecutionContextFromSpecification(new MessageHeaders(headers), tenant, metadata);

    assertThat(actual.getTenantId()).isEqualTo(tenant);
    assertThat(actual.getAllHeaders()).isEqualTo(expectedHeaders);
  }

  private byte[] headerValue(String header) {
    return header.getBytes(StandardCharsets.UTF_8);
  }
}
