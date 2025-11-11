package org.folio.qm.config;

import feign.codec.Encoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

  @Bean
  public Encoder feignEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
    return new SpringEncoder(messageConverters);
  }
}
