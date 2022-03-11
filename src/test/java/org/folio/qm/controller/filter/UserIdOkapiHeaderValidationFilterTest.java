package org.folio.qm.controller.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.folio.qm.support.types.UnitTest;
import org.folio.spring.integration.XOkapiHeaders;

@UnitTest
@ExtendWith(MockitoExtension.class)
class UserIdOkapiHeaderValidationFilterTest {

  @Spy
  private HttpServletRequest request;
  @Spy
  private HttpServletResponse response;
  @Spy
  private FilterChain chain;

  @SneakyThrows
  @Test
  void testStopFilterChainWhenXOkapiUserIdHeaderIsMissingInCommonRequest() {
    var filter = new UserIdOkapiHeaderValidationFilter();
    var contentStream = new ByteArrayOutputStream();
    filter.setManagementBasePath("/admin");
    when(request.getRequestURI()).thenReturn("/test");

    try (var writer = new PrintWriter(contentStream)) {
      when(response.getWriter()).thenReturn(writer);
      filter.doFilter(request, response, chain);
    }

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    assertThat(contentStream.toString(), containsString("x-okapi-user-id header must be provided"));
  }

  @SneakyThrows
  @Test
  void testContinueFilterChainWhenXOkapiUserIdHeaderIsExistInCommonRequest() {
    var filter = new UserIdOkapiHeaderValidationFilter();
    filter.setManagementBasePath("/admin");
    when(request.getRequestURI()).thenReturn("/test");
    when(request.getHeader(XOkapiHeaders.USER_ID)).thenReturn("user-id-header");

    filter.doFilter(request, response, chain);

    verify(response, never()).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(chain).doFilter(request, response);
  }

  @SneakyThrows
  @Test
  void testContinueFilterChainWhenXOkapiUserIdHeaderIsMissingInAdminRequest() {
    var filter = new UserIdOkapiHeaderValidationFilter();
    filter.setManagementBasePath("/admin");
    when(request.getRequestURI()).thenReturn("/admin/test");

    filter.doFilter(request, response, chain);

    verify(response, never()).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(chain).doFilter(request, response);
  }

  @SneakyThrows
  @Test
  void testContinueFilterChainWhenXOkapiUserIdHeaderIsMissingInTenantRequest() {
    var filter = new UserIdOkapiHeaderValidationFilter();
    filter.setManagementBasePath("/admin");
    when(request.getRequestURI()).thenReturn("/_/tenant");

    filter.doFilter(request, response, chain);

    verify(response, never()).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(chain).doFilter(request, response);
  }
}
