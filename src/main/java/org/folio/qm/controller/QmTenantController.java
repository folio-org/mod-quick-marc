package org.folio.qm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import org.folio.qm.holder.TenantsHolder;
import org.folio.qm.messaging.topic.KafkaTopicsInitializer;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.controller.TenantController;
import org.folio.spring.service.TenantService;

@RestController("folioTenantController")
public class QmTenantController extends TenantController {

  private final KafkaTopicsInitializer kafkaTopicsInitializer;
  private final TenantsHolder tenantsHolder;
  private final FolioExecutionContext context;

  public QmTenantController(TenantService tenantService,
                            KafkaTopicsInitializer kafkaTopicsInitializer,
                            TenantsHolder tenantsHolder,
                            FolioExecutionContext context) {
    super(tenantService);
    this.kafkaTopicsInitializer = kafkaTopicsInitializer;
    this.tenantsHolder = tenantsHolder;
    this.context = context;
  }

  @Override
  protected void upgradeTenant() {
    super.upgradeTenant();
    kafkaTopicsInitializer.createTopics();
    tenantsHolder.add(context.getTenantId());
  }

  @Override
  public ResponseEntity<Void> deleteTenant(String operationId) {
    ResponseEntity<Void> voidResponseEntity = super.deleteTenant(operationId);
    tenantsHolder.remove(context.getTenantId());
    return voidResponseEntity;
  }
}
