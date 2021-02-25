package org.folio.qm.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.folio.qm.util.JsonUtils.OBJECT_DESERIALIZATION_FAILED;
import static org.folio.qm.util.JsonUtils.OBJECT_SERIALIZATION_FAILED;

import org.junit.jupiter.api.Test;

import org.folio.qm.domain.entity.UserInfo;


class JsonUtilsTest {

  @Test
  void shouldThrowExceptionWhenInvalidJsonString() {
    Exception exception = assertThrows(IllegalStateException.class,
      () -> JsonUtils.jsonToObject("abc",  UserInfo.class));
    assertTrue(exception.getMessage().contains(OBJECT_DESERIALIZATION_FAILED));
  }

  @Test
  void shouldDeserializeObjectFromValidJsonString() {
    final var jsonString = "{\n" +
      "  \"sub\": \"john_doe\",\n" +
      "  \"user_id\": \"00000000-0000-0000-0000-000000000000\",\n" +
      "  \"iat\": 1614252390,\n" +
      "  \"tenant\": \"test\"\n" +
      "}";
    assertDoesNotThrow(() -> {
      final var userInfo = JsonUtils.jsonToObject(jsonString, UserInfo.class);
      assertEquals("00000000-0000-0000-0000-000000000000", userInfo.getUserId());
      assertEquals("john_doe", userInfo.getUserName());
    });
  }

  @Test
  void shouldThrowExceptionWhenInvalidObject(){
    Exception exception = assertThrows(IllegalStateException.class,
      () -> JsonUtils.objectToJsonString(new Object()));
    assertTrue(exception.getMessage().contains(OBJECT_SERIALIZATION_FAILED));
  }
}
