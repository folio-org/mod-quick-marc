package org.folio.qm.support.utils;

import static org.folio.qm.support.utils.APITestUtils.TENANT_ID;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import lombok.experimental.UtilityClass;
import org.springframework.jdbc.core.JdbcTemplate;

import org.folio.qm.domain.entity.ActionStatus;
import org.folio.qm.domain.entity.ActionStatusEnum;
import org.folio.spring.FolioModuleMetadata;

@UtilityClass
public class DBTestUtils {

  public static final String RECORD_CREATION_STATUS_TABLE_NAME = "action_status";
  public static final String JOB_PROFILE_TABLE_NAME = "job_profile";

  public static void saveCreationStatus(UUID id, UUID jobExecutionId, FolioModuleMetadata metadata,
                                        JdbcTemplate jdbcTemplate) {
    var jobProfileId = jdbcTemplate.query("SELECT id FROM " + jobProfileTable(TENANT_ID, metadata) + " LIMIT 1", rs -> {
      rs.next();
      return getUuid("id", rs);
    });
    var sql = "INSERT INTO " + creationStatusTable(TENANT_ID, metadata) + " (id, job_execution_id, job_profile_id) " +
      "VALUES (?, ?, ?)";
    jdbcTemplate.update(sql, id, jobExecutionId, jobProfileId);
  }

  public static ActionStatus getCreationStatusById(UUID id, FolioModuleMetadata metadata,
                                                   JdbcTemplate jdbcTemplate) {
    var sql = "SELECT * FROM " + creationStatusTable(TENANT_ID, metadata) + " WHERE id = ?";
    return jdbcTemplate.query(sql, ps -> ps.setObject(1, id), rs -> {
      rs.next();
      var recordCreationStatus = new ActionStatus();
      recordCreationStatus.setId(getUuid("id", rs));
      recordCreationStatus.setJobExecutionId(UUID.fromString(rs.getString("job_execution_id")));
      recordCreationStatus.setExternalId(getUuid("external_id", rs));
      recordCreationStatus.setMarcId(getUuid("marc_id", rs));
      recordCreationStatus.setStatus(ActionStatusEnum.valueOf(rs.getString("status")));
      recordCreationStatus.setErrorMessage(rs.getString("error_message"));
      recordCreationStatus.setCreatedAt(rs.getTimestamp("created_at"));
      recordCreationStatus.setUpdatedAt(rs.getTimestamp("updated_at"));
      return recordCreationStatus;
    });
  }

  public static String creationStatusTable(String tenantId, FolioModuleMetadata metadata) {
    return getTableName(RECORD_CREATION_STATUS_TABLE_NAME, tenantId, metadata);
  }

  public static String jobProfileTable(String tenantId, FolioModuleMetadata metadata) {
    return getTableName(JOB_PROFILE_TABLE_NAME, tenantId, metadata);
  }

  public static String getTableName(String tableName, String tenantId, FolioModuleMetadata metadata) {
    return metadata.getDBSchemaName(tenantId) + "." + tableName;
  }

  private static UUID getUuid(String columnLabel, ResultSet rs) throws SQLException {
    var string = rs.getString(columnLabel);
    return string == null ? null : UUID.fromString(string);
  }
}
