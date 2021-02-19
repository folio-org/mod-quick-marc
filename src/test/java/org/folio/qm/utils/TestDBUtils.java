package org.folio.qm.utils;

import static org.folio.qm.utils.TestUtils.TENANT_ID;

import java.util.UUID;

import lombok.experimental.UtilityClass;
import org.springframework.jdbc.core.JdbcTemplate;

import org.folio.qm.domain.entity.RecordCreationStatus;
import org.folio.qm.domain.entity.RecordCreationStatusEnum;
import org.folio.spring.FolioModuleMetadata;

@UtilityClass
public class TestDBUtils {

  public static final String RECORD_CREATION_STATUS_TABLE_NAME = "record_creation_status";

  public static void saveCreationStatus(UUID id, UUID jobExecutionId, FolioModuleMetadata metadata,
                                        JdbcTemplate jdbcTemplate) {
    var sql = "INSERT INTO " + creationStatusTable(TENANT_ID, metadata) + " (id, job_execution_id) VALUES (?, ?)";
    jdbcTemplate.update(sql, id, jobExecutionId);
  }
  
  public static RecordCreationStatus getCreationStatusById(UUID id, FolioModuleMetadata metadata,
                                                                       JdbcTemplate jdbcTemplate) {
    var sql = "SELECT * FROM " + creationStatusTable(TENANT_ID, metadata) + " WHERE id = ?";
    return jdbcTemplate.query(sql, new Object[] {id}, rs -> {
      rs.next();
      var recordCreationStatus = new RecordCreationStatus();
      recordCreationStatus.setId(UUID.fromString(rs.getString("id")));
      recordCreationStatus.setJobExecutionId(UUID.fromString(rs.getString("job_execution_id")));
      var instanceId = rs.getString("instance_id");
      recordCreationStatus.setInstanceId(instanceId == null ? null : UUID.fromString(instanceId));
      recordCreationStatus.setStatus(RecordCreationStatusEnum.valueOf(rs.getString("status")));
      recordCreationStatus.setErrorMessage(rs.getString("error_message"));
      recordCreationStatus.setCreatedAt(rs.getTimestamp("created_at"));
      recordCreationStatus.setUpdatedAt(rs.getTimestamp("updated_at"));
      return recordCreationStatus;
    });
  }

  public static String creationStatusTable(String tenantId, FolioModuleMetadata metadata) {
    return getTableName(RECORD_CREATION_STATUS_TABLE_NAME, tenantId, metadata);
  }

  public static String getTableName(String tableName, String tenantId, FolioModuleMetadata metadata) {
    return metadata.getDBSchemaName(tenantId) + "." + tableName;
  }
}
