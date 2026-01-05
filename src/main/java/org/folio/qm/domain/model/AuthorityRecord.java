package org.folio.qm.domain.model;

import org.folio.Authority;

public class AuthorityRecord extends Authority implements FolioRecord {
  @Override
  public String getHrid() {
    return getNaturalId();
  }
}
