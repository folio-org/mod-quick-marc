package org.folio.qm.controller.interceptor;

import java.time.Instant;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import org.folio.spring.FolioExecutionContext;
import org.folio.spring.integration.XOkapiHeaders;

@Log4j2
@Component
@ConditionalOnProperty(
  prefix = "folio.logging.request",
  name = "enabled"
)
@RequiredArgsConstructor
public class LoggingRequestInterceptor extends HandlerInterceptorAdapter {

  private static final String START_TIME_ATTR = "startTime";
  private static final String REQ_ID_ATTR = "startTime";

  private final FolioExecutionContext folioExecutionContext;

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                           @NonNull Object handler) {
    var startTime = Instant.now().toEpochMilli();
    var requestId = getRequestId();
    request.setAttribute(START_TIME_ATTR, startTime);
    request.setAttribute(REQ_ID_ATTR, requestId);
    log.info("---> [{}] {} {} {}",
      requestId,
      request.getMethod(),
      request.getRequestURI(),
      request.getQueryString()
    );
    return true;
  }

  @Override
  public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                              @NonNull Object handler, @Nullable Exception ex) {
    var startTime = (Long) request.getAttribute(START_TIME_ATTR);
    var requestId = (String) request.getAttribute(REQ_ID_ATTR);
    if (ex != null) {
      log.error("[{}] Exception: {}", requestId, ex.getMessage());
    }
    log.info("<--- [{}] {} in {}ms",
      requestId,
      response.getStatus(),
      (Instant.now().toEpochMilli() - startTime)
    );
  }

  private String getRequestId() {
    var c = folioExecutionContext.getOkapiHeaders().get(XOkapiHeaders.REQUEST_ID);
    return c != null && !c.isEmpty() ? c.iterator().next() : null;
  }

}
