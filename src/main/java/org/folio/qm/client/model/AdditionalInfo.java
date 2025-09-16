package org.folio.qm.client.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AdditionalInfo {

  private boolean suppressDiscovery = false;

  public AdditionalInfo(boolean suppressDiscovery) {
    this.suppressDiscovery = suppressDiscovery;
  }
}

