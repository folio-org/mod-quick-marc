package org.folio.qm.service.storage.tenant;

import static org.folio.qm.config.CacheConfig.CONSORTIUM_CENTRAL_TENANT_CACHE;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.qm.client.UserTenantsClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserTenantsServiceImpl implements UserTenantsService {

  private final UserTenantsClient userTenantsClient;

  @Cacheable(cacheNames = CONSORTIUM_CENTRAL_TENANT_CACHE, key = "@folioExecutionContext.tenantId + ':' + #tenantId")
  public Optional<String> getCentralTenant(String tenantId) {
    if (StringUtils.isBlank(tenantId)) {
      return Optional.empty();
    }

    var userTenants = userTenantsClient.getUserTenants(tenantId);
    log.debug("getCentralTenant: tenantId: {}, response: {}", tenantId, userTenants);

    return Optional.ofNullable(userTenants)
      .map(UserTenantsClient.UserTenants::userTenants)
      .orElse(List.of())
      .stream()
      .findFirst()
      .map(UserTenantsClient.UserTenant::centralTenantId);
  }
}
