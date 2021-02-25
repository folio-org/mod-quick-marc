package org.folio.qm.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {

  @JsonProperty("user_id")
  String userId;
  @JsonProperty("sub")
  String userName;
}
