package org.folio.qm.util;

import java.util.UUID;
import org.folio.qm.support.types.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@UnitTest
class MarcUtilsTest {

  @Test
  void whenUuidIsValid_thenValidationSucceeds() {
    Assertions.assertTrue(MarcUtils.isValidUuid(UUID.randomUUID().toString()));
  }

  @Test
  void whenUuidIsInvalid_thenValidationFails() {
    Assertions.assertFalse(MarcUtils.isValidUuid("invalid-uuid"));
  }
}
