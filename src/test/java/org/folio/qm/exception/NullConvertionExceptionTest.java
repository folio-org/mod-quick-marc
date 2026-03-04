package org.folio.qm.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class NullConvertionExceptionTest {

  @Test
  void shouldCreateExceptionWithFormattedMessage() {
    var exception = new NullConvertionException(String.class);

    assertNotNull(exception);
    assertEquals("Conversion resulted in null for target type: java.lang.String", exception.getMessage());
  }

  @Test
  void shouldContainCorrectClassNameInMessage() {
    var exception = new NullConvertionException(Integer.class);

    assertNotNull(exception);
    assertEquals("Conversion resulted in null for target type: java.lang.Integer", exception.getMessage());
  }
}
