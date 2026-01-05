package org.folio.qm.service.storage.user;

import java.util.Optional;
import java.util.UUID;
import org.folio.qm.domain.dto.UserInfo;
import org.springframework.lang.Nullable;

public interface UserService {

  Optional<UserInfo> fetchUser(@Nullable UUID userId);
}
