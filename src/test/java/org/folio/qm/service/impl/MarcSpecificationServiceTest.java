package org.folio.qm.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.qm.config.CacheNames.SPECIFICATION_STORAGE_CACHE;
import static org.folio.support.utils.ApiTestUtils.TENANT_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.folio.qm.client.SpecificationStorageClient;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.service.storage.specification.MarcSpecificationServiceImpl;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.spring.testing.extension.Random;
import org.folio.spring.testing.extension.impl.RandomParametersExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith({MockitoExtension.class, RandomParametersExtension.class})
class MarcSpecificationServiceTest {

  @Mock
  private SpecificationStorageClient client;
  @Mock
  private CacheManager cacheManager;

  @InjectMocks
  private MarcSpecificationServiceImpl service;

  @Test
  void getSpecification_invalidFormat() {
    var ex = assertThrows(IllegalArgumentException.class, () -> service.getSpecification(MarcFormat.HOLDINGS));

    assertThat(ex.getMessage()).isEqualTo("Unknown format: " + MarcFormat.HOLDINGS.name());
    verifyNoInteractions(client);
  }

  @Test
  void updateSpecificationCache(@Random SpecificationDto specificationDto) {
    var event = event();
    var cache = mock(Cache.class);

    when(client.getSpecification(event.specificationId())).thenReturn(specificationDto);
    when(cacheManager.getCache(SPECIFICATION_STORAGE_CACHE)).thenReturn(cache);

    service.updateSpecificationCache(event);

    verify(cache).put(event.tenantId() + ":" + specificationDto.getProfile().name(), specificationDto);
  }

  @Test
  void updateSpecificationCache_noCache(@Random SpecificationDto specificationDto) {
    var event = event();

    when(client.getSpecification(event.specificationId())).thenReturn(specificationDto);
    when(cacheManager.getCache(SPECIFICATION_STORAGE_CACHE)).thenReturn(null);

    service.updateSpecificationCache(event);

    verify(cacheManager).getCache(SPECIFICATION_STORAGE_CACHE);
  }

  private SpecificationUpdatedEvent event() {
    return new SpecificationUpdatedEvent(UUID.randomUUID(), TENANT_ID, Family.MARC, FamilyProfile.AUTHORITY,
      SpecificationUpdatedEvent.UpdateExtent.PARTIAL);
  }
}
