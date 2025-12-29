package org.folio.qm.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class MappingMetadataExceptionTest {

  @Test
  void shouldCreateExceptionWithMessage() {
    var message = "Test message";
    var exception = new MappingMetadataException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  void shouldCreateExceptionWithMessageAndCause() {
    var message = "Test message";
    var cause = new RuntimeException("Cause");
    var exception = new MappingMetadataException(message, cause);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertSame(cause, exception.getCause());
  }

  @Test
  void shouldCreateExceptionWithCause() {
    var cause = new RuntimeException("Cause message");
    var exception = new MappingMetadataException(cause);

    assertNotNull(exception);
    assertSame(cause, exception.getCause());
  }
}
