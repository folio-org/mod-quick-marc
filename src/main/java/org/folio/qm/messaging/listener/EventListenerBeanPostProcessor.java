package org.folio.qm.messaging.listener;

import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.TOKEN;
import static org.folio.spring.integration.XOkapiHeaders.URL;
import static org.folio.spring.scope.FolioExecutionScopeExecutionContextManager.beginFolioExecutionContext;
import static org.folio.spring.scope.FolioExecutionScopeExecutionContextManager.endFolioExecutionContext;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;

@Component
@AllArgsConstructor
public class EventListenerBeanPostProcessor implements BeanPostProcessor {

  private final FolioModuleMetadata folioModuleMetadata;

  @Override
  public Object postProcessBeforeInitialization(@Nullable Object bean, @Nullable String beanName) throws BeansException {
    if (bean instanceof DataImportEventListener) {
      ProxyFactoryBean pfb = new ProxyFactoryBean();
      pfb.setTarget(bean);
      pfb.addAdvice((MethodInterceptor) invocation -> {
        try {
          var arguments = invocation.getArguments();
          var headers = (MessageHeaders) arguments[1];
          var folioExecutionContext = getFolioExecutionContext(headers);
          beginFolioExecutionContext(folioExecutionContext);
          return invocation.proceed();
        } finally {
          endFolioExecutionContext();
        }
      });
      return pfb.getObject();
    }
    return bean;
  }

  private FolioExecutionContext getFolioExecutionContext(MessageHeaders headers) {
    var tenantId = getHeaderValue(headers, TENANT);
    var okapiUrl = getHeaderValue(headers, URL);
    var token = getHeaderValue(headers, TOKEN);
    return new FolioExecutionContext() {

      @Override public String getTenantId() {
        return tenantId;
      }

      @Override public String getOkapiUrl() {
        return okapiUrl;
      }

      @Override public String getToken() {
        return token;
      }

      @Override public Map<String, Collection<String>> getAllHeaders() {
        return null;
      }

      @Override public Map<String, Collection<String>> getOkapiHeaders() {
        return null;
      }

      @Override public FolioModuleMetadata getFolioModuleMetadata() {
        return folioModuleMetadata;
      }
    };
  }

  private String getHeaderValue(MessageHeaders headers, String headerName) {
    var headerValue = headers.get(headerName);
    return headerValue == null
      ? null
      : new String((byte[]) headerValue, StandardCharsets.UTF_8);
  }
}
