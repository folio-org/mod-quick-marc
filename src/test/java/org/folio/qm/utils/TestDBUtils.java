package org.folio.qm.utils;

import static org.folio.qm.utils.TestUtils.TENANT_ID;

import java.util.UUID;

import lombok.experimental.UtilityClass;
import org.springframework.jdbc.core.JdbcTemplate;

import org.folio.spring.FolioModuleMetadata;

@UtilityClass
public class TestDBUtils {

  public static final String RECORD_CREATION_STATUS_TABLE_NAME = "record_creation_status";

  public static void saveCreationStatus(UUID id, UUID jobExecutionId, FolioModuleMetadata metadata,
                                        JdbcTemplate jdbcTemplate) {
    var sql = "INSERT INTO " + creationStatusTable(TENANT_ID, metadata) + " (id, job_execution_id) VALUES (?, ?)";
    jdbcTemplate.update(sql, id, jobExecutionId);
  }

  public static String creationStatusTable(String tenantId, FolioModuleMetadata metadata) {
    return getTableName(RECORD_CREATION_STATUS_TABLE_NAME, tenantId, metadata);
  }

  public static String getTableName(String tableName, String tenantId, FolioModuleMetadata metadata) {
    return metadata.getDBSchemaName(tenantId) + "." + tableName;
  }
}
