package org.folio.qm.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class FolioEnvironment {

  @Value("${ENV:folio}")
  private String envId;
}
