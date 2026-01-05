package org.folio.qm.convertion.converter;

import java.util.UUID;
import org.folio.qm.client.UsersClient;
import org.folio.qm.domain.dto.UserInfo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserDtoToUserInfoConverter implements Converter<UsersClient.UserDto, UserInfo> {

  @Override
  public UserInfo convert(UsersClient.UserDto source) {
    var userInfo = new UserInfo();
    userInfo.setUserId(UUID.fromString(source.id()));
    userInfo.setUsername(source.username());
    
    if (source.personal() != null) {
      userInfo.setFirstName(source.personal().firstName());
      userInfo.setLastName(source.personal().lastName());
      userInfo.setMiddleName(source.personal().middleName());
    }
    
    return userInfo;
  }
}
