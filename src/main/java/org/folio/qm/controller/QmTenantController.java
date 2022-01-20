package org.folio.qm.controller;

import org.springframework.web.bind.annotation.RestController;

import org.folio.qm.messaging.topic.KafkaTopicsInitializer;
import org.folio.spring.controller.TenantController;
import org.folio.spring.service.TenantService;

@RestController("folioTenantController")
public class QmTenantController extends TenantController {

  private final KafkaTopicsInitializer kafkaTopicsInitializer;

  public QmTenantController(TenantService tenantService,
                            KafkaTopicsInitializer kafkaTopicsInitializer) {
    super(tenantService);
    this.kafkaTopicsInitializer = kafkaTopicsInitializer;
  }

  @Override
  protected void upgradeTenant() {
    super.upgradeTenant();
    kafkaTopicsInitializer.createTopics();
  }
}
