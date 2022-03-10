package org.folio.qm.controller.filter;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.filter.GenericFilterBean;

import org.folio.spring.integration.XOkapiHeaders;

@Component
public class UserIdOkapiHeaderValidationFilter extends GenericFilterBean implements OrderedFilter {

  private static final String ERROR_MSG = "x-okapi-user-id header must be provided";
  private int order = 2;

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    String requestURI = req.getRequestURI();
    if (!requestURI.startsWith("/_/") && isBlank(req.getHeader(XOkapiHeaders.USER_ID))) {
      HttpServletResponse res = (HttpServletResponse) response;
      res.setContentType(MimeTypeUtils.TEXT_PLAIN_VALUE);
      res.setStatus(400);
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
}
