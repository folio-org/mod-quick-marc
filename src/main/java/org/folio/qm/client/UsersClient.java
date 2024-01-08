package org.folio.qm.client;

import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "users", dismiss404 = true)
public interface UsersClient {

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  Optional<UserDto> fetchUserById(@PathVariable("id") String id);

  record UserDto(String id, String username, UserPersonal personal) {

  }

  record UserPersonal(String firstName, String lastName, String middleName) {

  }
}
