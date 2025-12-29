package org.folio.qm.service.storage.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.folio.qm.client.UsersClient;
import org.folio.qm.convertion.RecordConversionService;
import org.folio.qm.domain.dto.UserInfo;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  private @Mock UsersClient usersClient;
  private @Mock RecordConversionService conversionService;
  private @InjectMocks UserServiceImpl userService;

  @Test
  void shouldReturnEmptyWhenUserIdIsNull() {
    var result = userService.fetchUser(null);

    assertFalse(result.isPresent());
    verify(usersClient, never()).fetchUserById(any());
    verify(conversionService, never()).convert(any(), any());
  }

  @Test
  void shouldFetchAndConvertUser() {
    var userId = UUID.randomUUID();
    var userDto = new UsersClient.UserDto(userId.toString(), "username", null);
    var userInfo = new UserInfo();
    userInfo.setUserId(userId);
    userInfo.setUsername("username");

    when(usersClient.fetchUserById(userId)).thenReturn(Optional.of(userDto));
    when(conversionService.convert(userDto, UserInfo.class)).thenReturn(userInfo);

    var result = userService.fetchUser(userId);

    assertTrue(result.isPresent());
    assertEquals(userInfo, result.get());
    verify(usersClient).fetchUserById(userId);
    verify(conversionService).convert(userDto, UserInfo.class);
  }

  @Test
  void shouldReturnEmptyWhenUserNotFound() {
    var userId = UUID.randomUUID();
    when(usersClient.fetchUserById(userId)).thenReturn(Optional.empty());

    var result = userService.fetchUser(userId);

    assertFalse(result.isPresent());
    verify(usersClient).fetchUserById(userId);
    verify(conversionService, never()).convert(any(), eq(UserInfo.class));
  }
}
