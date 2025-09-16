package org.folio.qm.client.model;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JobExecution {

  private UUID id;
  private ProfileInfo jobProfileInfo;
}

