package org.folio.qm.service.impl;

import org.folio.qm.holder.TenantsHolder;
import org.folio.qm.messaging.topic.KafkaTopicsInitializer;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.service.TenantService;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Primary
public class QmTenantService extends TenantService {

  private final KafkaTopicsInitializer kafkaTopicsInitializer;
  private final TenantsHolder tenantsHolder;
  private final KafkaAdminService kafkaAdminService;

  public QmTenantService(JdbcTemplate jdbcTemplate,
                         TenantsHolder tenantsHolder,
                         KafkaTopicsInitializer kafkaTopicsInitializer,
                         FolioExecutionContext context,
                         FolioSpringLiquibase folioSpringLiquibase, KafkaAdminService kafkaAdminService) {
    super(jdbcTemplate, context, folioSpringLiquibase);
    this.kafkaTopicsInitializer = kafkaTopicsInitializer;
    this.tenantsHolder = tenantsHolder;
    this.kafkaAdminService = kafkaAdminService;
  }

  @Override
  protected void afterTenantUpdate(TenantAttributes tenantAttributes) {
    kafkaTopicsInitializer.createTopics();
    tenantsHolder.add(context.getTenantId());
  }

  @Override
  protected void afterTenantDeletion(TenantAttributes tenantAttributes) {
    String tenantId = context.getTenantId();
    tenantsHolder.remove(tenantId);
    kafkaAdminService.deleteTopics(tenantId);
  }
}
