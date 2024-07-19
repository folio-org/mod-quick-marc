package org.folio.qm.service.impl;

import static org.folio.qm.config.CacheNames.SPECIFICATION_STORAGE_CACHE;

import lombok.RequiredArgsConstructor;
import org.folio.qm.client.SpecificationStorageClient;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.service.MarcSpecificationService;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarcSpecificationServiceImpl implements MarcSpecificationService {

  private final SpecificationStorageClient specificationStorageClient;

  @Override
  @Cacheable(cacheNames = SPECIFICATION_STORAGE_CACHE,
             unless = "#result == null",
             key = "@folioExecutionContext.tenantId + ':' + #marcFormat")
  public SpecificationDto getSpecification(MarcFormat marcFormat) {
    for (FamilyProfile profile : FamilyProfile.values()) {
      if (profile.name().equals(marcFormat.name())) {
        return specificationStorageClient.getSpecifications(profile.getValue()).getSpecifications().get(0);
      }
    }
    throw new IllegalArgumentException("Unknown format: " + marcFormat.name());
  }
}
