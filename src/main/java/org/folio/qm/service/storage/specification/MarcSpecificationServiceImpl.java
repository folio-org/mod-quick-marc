package org.folio.qm.service.storage.specification;

import static org.folio.qm.config.CacheConfig.SPECIFICATION_STORAGE_CACHE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.client.SpecificationStorageClient;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class MarcSpecificationServiceImpl implements MarcSpecificationService {

  private final SpecificationStorageClient specificationStorageClient;
  private final CacheManager cacheManager;

  @Override
  @Cacheable(cacheNames = SPECIFICATION_STORAGE_CACHE,
             unless = "#result == null",
             key = "@folioExecutionContext.tenantId + ':' + #marcFormat")
  public SpecificationDto getSpecification(MarcFormat marcFormat) {
    for (FamilyProfile profile : FamilyProfile.values()) {
      if (profile.name().equals(marcFormat.name())) {
        return specificationStorageClient.getSpecifications(profile.getValue()).getSpecifications().getFirst();
      }
    }
    throw new IllegalArgumentException("Unknown format: " + marcFormat.name());
  }

  @Override
  public void updateSpecificationCache(SpecificationUpdatedEvent specificationUpdate) {
    var specification = specificationStorageClient.getSpecification(specificationUpdate.specificationId());
    var cache = cacheManager.getCache(SPECIFICATION_STORAGE_CACHE);

    if (cache == null) {
      log.warn("updateSpecificationCache:: no cache found for " + SPECIFICATION_STORAGE_CACHE);
      return;
    }

    cache.put(specificationUpdate.tenantId() + ":" + specification.getProfile().name(), specification);
    log.debug("updateSpecificationCache:: updated for tenant {}, profile {}",
      specificationUpdate.tenantId(), specification.getProfile().name());
  }
}
