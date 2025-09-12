package org.folio.support;

import java.lang.reflect.Method;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Log4j2
public class DisplayNameLogger implements AfterEachCallback, BeforeEachCallback {

  @Override
  public void beforeEach(ExtensionContext context) {
    logStart(context);
  }

  @Override
  public void afterEach(ExtensionContext context) {
    logEnd(context);
  }

  private void logStart(ExtensionContext context) {
    String displayName = context.getTestMethod().map(Method::getName).orElse(context.getDisplayName());
    log.info("START TEST: {}", displayName);
  }

  private void logEnd(ExtensionContext context) {
    String displayName = context.getTestMethod().map(Method::getName).orElse(context.getDisplayName());
    log.info("END TEST: {}", displayName);
  }
}
