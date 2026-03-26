package org.folio.qm.service.storage.user;

import java.util.Optional;
import org.folio.qm.domain.dto.UserInfo;

public interface UserService {

  Optional<UserInfo> fetchUser(String userId);
}
