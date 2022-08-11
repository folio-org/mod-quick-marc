package org.folio.qm.support.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.folio.qm.domain.entity.RecordCreationStatus;
import org.folio.qm.domain.entity.RecordCreationStatusEnum;
import org.folio.spring.FolioModuleMetadata;
import org.springframework.jdbc.core.JdbcTemplate;

@UtilityClass
public class DataBaseTestUtils {

  public static final String RECORD_CREATION_STATUS_TABLE_NAME = "record_creation_status";

  public static void saveCreationStatus(UUID id, UUID jobExecutionId, FolioModuleMetadata metadata,
                                        JdbcTemplate jdbcTemplate) {
    var sql =
      "INSERT INTO " + creationStatusTable(ApiTestUtils.TENANT_ID, metadata) + " (id, job_execution_id) VALUES (?, ?)";
    jdbcTemplate.update(sql, id, jobExecutionId);
  }

  public static RecordCreationStatus getCreationStatusById(UUID id, FolioModuleMetadata metadata,
                                                           JdbcTemplate jdbcTemplate) {
    var sql = "SELECT * FROM " + creationStatusTable(ApiTestUtils.TENANT_ID, metadata) + " WHERE id = ?";
    return jdbcTemplate.query(sql, ps -> ps.setObject(1, id), rs -> {
      rs.next();
      var recordCreationStatus = new RecordCreationStatus();
      recordCreationStatus.setId(getUuid("id", rs));
      recordCreationStatus.setJobExecutionId(UUID.fromString(rs.getString("job_execution_id")));
      recordCreationStatus.setExternalId(getUuid("external_id", rs));
      recordCreationStatus.setMarcId(getUuid("marc_id", rs));
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

  private static UUID getUuid(String columnLabel, ResultSet rs) throws SQLException {
    var string = rs.getString(columnLabel);
    return string == null ? null : UUID.fromString(string);
  }
}
