package org.folio.qm.convertion.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import org.folio.qm.client.UsersClient;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class UserDtoToUserInfoConverterTest {

  private final UserDtoToUserInfoConverter converter = new UserDtoToUserInfoConverter();

  @Test
  void shouldConvertUserDtoWithAllFields() {
    var userId = UUID.randomUUID();
    var username = "testuser";
    var personal = new UsersClient.UserPersonal("John", "Doe", "M");
    var userDto = new UsersClient.UserDto(userId.toString(), username, personal);

    var result = converter.convert(userDto);

    assertNotNull(result);
    assertEquals(userId, result.getUserId());
    assertEquals(username, result.getUsername());
    assertEquals("John", result.getFirstName());
    assertEquals("Doe", result.getLastName());
    assertEquals("M", result.getMiddleName());
  }

  @Test
  void shouldConvertUserDtoWithoutPersonalInfo() {
    var userId = UUID.randomUUID();
    var username = "testuser";
    var userDto = new UsersClient.UserDto(userId.toString(), username, null);

    var result = converter.convert(userDto);

    assertNotNull(result);
    assertEquals(userId, result.getUserId());
    assertEquals(username, result.getUsername());
    assertNull(result.getFirstName());
    assertNull(result.getLastName());
    assertNull(result.getMiddleName());
  }

  @Test
  void shouldConvertUserDtoWithPartialPersonalInfo() {
    var userId = UUID.randomUUID();
    var username = "testuser";
    var personal = new UsersClient.UserPersonal("Jane", null, null);
    var userDto = new UsersClient.UserDto(userId.toString(), username, personal);

    var result = converter.convert(userDto);

    assertNotNull(result);
    assertEquals(userId, result.getUserId());
    assertEquals(username, result.getUsername());
    assertEquals("Jane", result.getFirstName());
    assertNull(result.getLastName());
    assertNull(result.getMiddleName());
  }
}
