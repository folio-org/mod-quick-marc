package org.folio.qm.client.model;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExternalIdsHolder {

  private UUID instanceId;
  private String instanceHrid;
  private UUID holdingsId;
  private String holdingsHrid;
  private UUID authorityId;
  private String authorityHrid;
}

