package org.folio.qm.service.impl;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.folio.qm.support.utils.APITestUtils.TENANT_ID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.folio.qm.holder.TenantsHolder;
import org.folio.qm.messaging.topic.KafkaTopicsInitializer;
import org.folio.qm.support.types.UnitTest;
import org.folio.spring.FolioExecutionContext;
import org.folio.tenant.domain.dto.Parameter;
import org.folio.tenant.domain.dto.TenantAttributes;

@UnitTest
@ExtendWith(MockitoExtension.class)
class QmTenantServiceTest {

  @InjectMocks
  private QmTenantService qmTenantService;
  @Mock
  private TenantsHolder tenantsHolder;
  @Mock
  private FolioExecutionContext context;
  @Mock
  private KafkaTopicsInitializer kafkaTopicsInitializer;

  @Test
  void initializeTenant_positive() {
    when(context.getTenantId()).thenReturn(TENANT_ID);
    doNothing().when(kafkaTopicsInitializer).createTopics();

    var attributes = new TenantAttributes().moduleTo("mod-quick-marc");
    qmTenantService.afterTenantUpdate(attributes);

    verify(kafkaTopicsInitializer).createTopics();
    verify(tenantsHolder).add(TENANT_ID);
  }

  @Test
  void disableTenant_positive() {
    when(context.getTenantId()).thenReturn(TENANT_ID);

    var attributes = new TenantAttributes().purge(true);
    qmTenantService.afterTenantDeletion(attributes);

    verify(tenantsHolder).remove(TENANT_ID);
  }
}
