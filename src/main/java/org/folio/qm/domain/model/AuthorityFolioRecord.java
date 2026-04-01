package org.folio.qm.domain.model;

import org.folio.Authority;

public class AuthorityFolioRecord extends Authority implements FolioRecord {
  @Override
  public String getHrid() {
    return getNaturalId();
  }
}
