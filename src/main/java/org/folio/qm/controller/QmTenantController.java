package org.folio.qm.controller;

import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.folio.qm.messaging.topic.KafkaTopicsInitializer;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.controller.TenantController;
import org.folio.spring.service.TenantService;
import org.folio.tenant.domain.dto.TenantAttributes;

@RestController("folioTenantController")
@RequestMapping(value = "/_/")
public class QmTenantController extends TenantController {

  private final KafkaTopicsInitializer kafkaTopicsInitializer;
  private final Set<String> tenants;
  private final FolioExecutionContext context;

  public QmTenantController(TenantService tenantService,
                            KafkaTopicsInitializer kafkaTopicsInitializer,
                            @Qualifier("tenants") Set<String> tenants,
                            FolioExecutionContext context) {
    super(tenantService);
    this.kafkaTopicsInitializer = kafkaTopicsInitializer;
    this.tenants = tenants;
    this.context = context;
  }

  @Override
  public ResponseEntity<String> postTenant(@Valid TenantAttributes tenantAttributes) {
    var responseEntity = super.postTenant(tenantAttributes);
    if (responseEntity.getStatusCode() == HttpStatus.OK) {
      kafkaTopicsInitializer.createTopics();
      tenants.add(context.getTenantId());
    }
    return responseEntity;
  }
}
