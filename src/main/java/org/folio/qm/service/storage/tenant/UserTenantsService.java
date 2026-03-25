package org.folio.qm.service.storage.tenant;

import java.util.Optional;

public interface UserTenantsService {

  Optional<String> getCentralTenant(String tenantId);
}
