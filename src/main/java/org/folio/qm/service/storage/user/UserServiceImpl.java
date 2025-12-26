package org.folio.qm.service.storage.user;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.qm.client.UsersClient;
import org.folio.qm.converter.UserMapper;
import org.folio.qm.domain.dto.UserInfo;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UsersClient usersClient;
  private final UserMapper userMapper;

  @Override
  public Optional<UserInfo> fetchUser(@Nullable UUID userId) {
    if (userId == null) {
      return Optional.empty();
    }
    return usersClient.fetchUserById(userId)
      .map(userMapper::fromDto);
  }
}
