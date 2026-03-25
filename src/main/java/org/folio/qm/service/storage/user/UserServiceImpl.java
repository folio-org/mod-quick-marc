package org.folio.qm.service.storage.user;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.client.UsersClient;
import org.folio.qm.convertion.RecordConversionService;
import org.folio.qm.domain.dto.UserInfo;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UsersClient usersClient;
  private final RecordConversionService conversionService;

  @Override
  public Optional<UserInfo> fetchUser(@Nullable UUID userId) {
    if (userId == null) {
      log.trace("fetchUser:: UserId is null, returning empty");
      return Optional.empty();
    }
    log.debug("fetchUser:: Fetching user info for userId: {}", userId);
    var userInfo = usersClient.fetchUserById(userId)
      .map(userDto -> conversionService.convert(userDto, UserInfo.class));
    if (userInfo.isPresent()) {
      log.debug("fetchUser:: User info retrieved successfully for userId: {}", userId);
    } else {
      log.warn("fetchUser:: User info not found for userId: {}", userId);
    }
    return userInfo;
  }
}
