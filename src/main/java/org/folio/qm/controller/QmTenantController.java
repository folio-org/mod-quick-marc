package org.folio.qm.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.folio.qm.messaging.topic.KafkaTopicsInitializer;
import org.folio.spring.controller.TenantController;
import org.folio.spring.service.TenantService;
import org.folio.tenant.domain.dto.TenantAttributes;

@RestController("folioTenantController")
@RequestMapping(value = "/_/")
public class QmTenantController extends TenantController {

  private final KafkaTopicsInitializer kafkaTopicsInitializer;

  public QmTenantController(TenantService tenantService,
                            KafkaTopicsInitializer kafkaTopicsInitializer) {
    super(tenantService);
    this.kafkaTopicsInitializer = kafkaTopicsInitializer;
  }

  @Override
  public ResponseEntity<String> postTenant(@Valid TenantAttributes tenantAttributes) {
    var responseEntity = super.postTenant(tenantAttributes);
    if (responseEntity.getStatusCode() == HttpStatus.OK) {
      kafkaTopicsInitializer.createTopics();
    }
    return responseEntity;
  }
}
