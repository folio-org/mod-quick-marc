package org.folio.qm.support.extension.impl;

import static org.folio.qm.support.utils.APITestUtils.TENANT_ID;
import static org.folio.qm.support.utils.DBTestUtils.getTableName;

import java.util.Optional;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

import org.folio.qm.support.extension.ClearTable;
import org.folio.spring.FolioModuleMetadata;

public class DatabaseCleanupExtension implements AfterEachCallback {

  @Override
  public void afterEach(ExtensionContext context) {
    Optional.ofNullable(context.getRequiredTestMethod().getAnnotation(ClearTable.class))
      .map(ClearTable::value)
      .ifPresent(tableNames -> clearTables(tableNames, context));
  }

  private void clearTables(String[] tableNames, ExtensionContext context) {
    var applicationContext = SpringExtension.getApplicationContext(context);
    JdbcTemplate jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
    FolioModuleMetadata folioModuleMetadata = applicationContext.getBean(FolioModuleMetadata.class);
    for (String tableName : tableNames) {
      JdbcTestUtils.deleteFromTables(jdbcTemplate, getTableName(tableName, TENANT_ID, folioModuleMetadata));
    }
  }
}
