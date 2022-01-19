package org.folio.qm.holder.impl;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.springframework.stereotype.Component;

import org.folio.qm.holder.TenantsHolder;

@Component
public class TenantsHolderImpl implements TenantsHolder {

  private final Set<String> tenants = new ConcurrentSkipListSet<>();

  @Override
  public void add(String tenant) {
    tenants.add(tenant);
  }

  @Override
  public void remove(String tenant) {
    tenants.remove(tenant);
  }

  @Override
  public Set<String> getAll() {
    return Set.copyOf(tenants);
  }

  @Override
  public int count() {
    return tenants.size();
  }
}
