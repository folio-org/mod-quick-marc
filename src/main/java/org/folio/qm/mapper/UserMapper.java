package org.folio.qm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

import org.folio.qm.client.UsersClient;
import org.folio.qm.domain.dto.UserInfo;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface UserMapper {

  @Mapping(target = "userId", expression = "java(java.util.UUID.fromString(userDto.getId()))")
  @Mapping(target = "username", source = "username")
  @Mapping(target = "firstName", source = "personal.firstName")
  @Mapping(target = "lastName", source = "personal.lastName")
  @Mapping(target = "middleName", source = "personal.middleName")
  UserInfo fromDto(UsersClient.UserDto userDto);
}

