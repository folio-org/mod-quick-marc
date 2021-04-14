package org.folio.qm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.folio.qm.controller.interceptor.LoggingRequestInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Autowired(required = false)
  private LoggingRequestInterceptor loggingRequestInterceptor;

  @Override
  public void addInterceptors(@NonNull InterceptorRegistry registry) {
    if (loggingRequestInterceptor != null) {
      registry.addInterceptor(loggingRequestInterceptor);
    }
  }

}
