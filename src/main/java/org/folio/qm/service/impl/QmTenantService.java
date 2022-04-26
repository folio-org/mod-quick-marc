package org.folio.qm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import org.folio.qm.holder.TenantsHolder;
import org.folio.qm.messaging.topic.KafkaTopicsInitializer;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.service.TenantService;
import org.folio.tenant.domain.dto.TenantAttributes;

@Primary
@Service
public class QmTenantService extends TenantService {

  private final KafkaTopicsInitializer kafkaTopicsInitializer;
  private final TenantsHolder tenantsHolder;

  @Autowired
  public QmTenantService(JdbcTemplate jdbcTemplate,
                         FolioExecutionContext context,
                         FolioSpringLiquibase folioSpringLiquibase,
                         KafkaTopicsInitializer kafkaTopicsInitializer,
                         TenantsHolder tenantsHolder) {
    super(jdbcTemplate, context, folioSpringLiquibase);
    this.kafkaTopicsInitializer = kafkaTopicsInitializer;
    this.tenantsHolder = tenantsHolder;
  }

  @Override
  protected void afterTenantUpdate(TenantAttributes tenantAttributes) {
    super.afterTenantUpdate(tenantAttributes);
    kafkaTopicsInitializer.createTopics();
    kafkaTopicsInitializer.restartEventListeners();
    tenantsHolder.add(context.getTenantId());
  }

  @Override
  protected void afterTenantDeletion(TenantAttributes tenantAttributes) {
    super.afterTenantDeletion(tenantAttributes);
    tenantsHolder.remove(context.getTenantId());
  }
}
