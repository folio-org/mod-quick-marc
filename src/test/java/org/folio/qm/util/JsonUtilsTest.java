package org.folio.qm.util;

import static org.folio.qm.util.JsonUtils.OBJECT_DESERIALIZATION_FAILED;
import static org.folio.qm.util.JsonUtils.OBJECT_SERIALIZATION_FAILED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class JsonUtilsTest {

  @Test
  void shouldThrowExceptionWhenInvalidJsonString() {
    Exception exception = assertThrows(IllegalStateException.class,
      () -> JsonUtils.jsonToObject("abc", UserInfo.class));
    assertTrue(exception.getMessage().contains(OBJECT_DESERIALIZATION_FAILED));
  }

  @Test
  void shouldDeserializeObjectFromValidJsonString() {
    final var jsonString = """
      {
        "sub": "john_doe",
        "user_id": "00000000-0000-0000-0000-000000000000",
        "iat": 1614252390,
        "tenant": "test"
      }
      """;
    assertDoesNotThrow(() -> {
      final var userInfo = JsonUtils.jsonToObject(jsonString, UserInfo.class);
      assertEquals("00000000-0000-0000-0000-000000000000", userInfo.getUserId());
      assertEquals("john_doe", userInfo.getUserName());
    });
  }

  @Test
  void shouldThrowExceptionWhenInvalidObject() {
    var arg = new Object();
    Exception exception = assertThrows(IllegalStateException.class,
      () -> JsonUtils.objectToJsonString(arg));
    assertTrue(exception.getMessage().contains(OBJECT_SERIALIZATION_FAILED));
  }

  @NoArgsConstructor
  @Getter
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class UserInfo {

    @JsonProperty("user_id")
    String userId;
    @JsonProperty("sub")
    String userName;
  }
}
