package org.folio.qm.holder;

import java.util.Set;

public interface TenantsHolder {
  void add(String tenant);

  void remove(String tenant);

  Set<String> getAll();

  int count();
}
