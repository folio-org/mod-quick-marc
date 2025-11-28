package org.folio.qm.client;

import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "users", accept = MediaType.APPLICATION_JSON_VALUE)
public interface UsersClient {

  @GetExchange(value = "/{id}")
  Optional<UserDto> fetchUserById(@PathVariable("id") String id);

  record UserDto(String id, String username, UserPersonal personal) {

  }

  record UserPersonal(String firstName, String lastName, String middleName) {

  }
}
