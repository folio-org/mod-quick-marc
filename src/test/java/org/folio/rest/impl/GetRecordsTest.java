package org.folio.rest.impl;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetRecordsTest extends TestBase  {

  private static final Logger logger = LoggerFactory.getLogger(GetRecordsTest.class);

  @Test
  public void testSuccessfulGet() {
    logger.info("===== Verify GET record:  =====");
    verifyGetRequest("/records-editor/records/" + UUID.randomUUID().toString(), 200);
  }

}
