package org.folio.qm.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeignInfoLogger extends  feign.Logger {

  private final Logger logger;

  public FeignInfoLogger(Class<?> clazz) {
    this(LoggerFactory.getLogger(clazz));
  }

  FeignInfoLogger(Logger logger) {
    this.logger = logger;
  }

  @Override
  protected void log(String configKey, String format, Object... args) {
    logger.info(String.format(methodTag(configKey) + format, args));
  }
}
