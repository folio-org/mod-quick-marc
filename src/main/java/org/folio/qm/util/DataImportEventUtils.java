package org.folio.qm.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.domain.dto.DataImportEventPayload;

@Log4j2
@UtilityClass
public class DataImportEventUtils {

  public static Optional<UUID> extractExternalId(DataImportEventPayload data, ObjectMapper mapper) {
    return extractRecordIdFromRecord(data, FolioRecord.INSTANCE, mapper)
      .or(() -> extractRecordIdFromRecord(data, FolioRecord.HOLDINGS, mapper))
      .or(() -> extractRecordIdFromRecord(data, FolioRecord.AUTHORITY, mapper))
      .or(() -> extractRecordIdFromField(data, FolioRecord.AUTHORITY));
  }

  private static Optional<UUID> extractRecordIdFromField(DataImportEventPayload data, FolioRecord folioRecord) {
    return Optional.ofNullable(data.getContext().get(folioRecord.getValue() + "_RECORD_ID"))
      .map(UUID::fromString);
  }

  public static Optional<UUID> extractMarcId(DataImportEventPayload data, ObjectMapper mapper) {
    return extractRecordIdFromRecord(data, FolioRecord.MARC_BIBLIOGRAPHIC, mapper)
      .or(() -> extractRecordIdFromRecord(data, FolioRecord.MARC_HOLDINGS, mapper))
      .or(() -> extractRecordIdFromRecord(data, FolioRecord.MARC_AUTHORITY, mapper));
  }

  public static Optional<String> extractErrorMessage(DataImportEventPayload data) {
    return Optional.ofNullable(data.getContext().get("ERROR"));
  }

  public static Optional<UUID> extractRecordIdFromRecord(DataImportEventPayload data, FolioRecord folioRecord,
                                                         ObjectMapper mapper) {
    return Optional.ofNullable(data.getContext().get(folioRecord.getValue()))
      .map(recordInJson -> {
        try {
          var idNode = mapper.readTree(recordInJson).get("id");
          return idNode != null ? UUID.fromString(idNode.asText()) : null;
        } catch (JsonProcessingException e) {
          log.info("Failed to process json", e);
          throw new IllegalStateException("Failed to process json with message: " + e.getMessage());
        }
      });
  }

  public enum FolioRecord {

    INSTANCE("INSTANCE"),
    HOLDINGS("HOLDINGS"),
    AUTHORITY("AUTHORITY"),
    MARC_BIBLIOGRAPHIC("MARC_BIBLIOGRAPHIC"),
    MARC_HOLDINGS("MARC_HOLDINGS"),
    MARC_AUTHORITY("MARC_AUTHORITY");

    private final String value;

    FolioRecord(String value) {
      this.value = value;
    }

    private String getValue() {
      return value;
    }
  }
}
