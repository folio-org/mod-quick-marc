package org.folio.qm.controller.filter;

import static org.apache.commons.lang3.StringUtils.isBlank;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.folio.spring.integration.XOkapiHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.filter.GenericFilterBean;

@Component
public class UserIdOkapiHeaderValidationFilter extends GenericFilterBean implements OrderedFilter {

  private static final String ERROR_MSG = "x-okapi-user-id header must be provided";
  private int order = 2;

  @Value("${management.endpoints.web.base-path}")
  private String managementBasePath;

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    String requestUri = req.getRequestURI();
    if (!requestUri.startsWith("/_/")
      && !requestUri.startsWith(managementBasePath)
      && isBlank(req.getHeader(XOkapiHeaders.USER_ID))) {
      HttpServletResponse res = (HttpServletResponse) response;
      res.setContentType(MimeTypeUtils.TEXT_PLAIN_VALUE);
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().println(ERROR_MSG);
      return;
    }
    chain.doFilter(request, response);
  }

  public int getOrder() {
    return this.order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public void setManagementBasePath(String managementBasePath) {
    this.managementBasePath = managementBasePath;
  }
}
