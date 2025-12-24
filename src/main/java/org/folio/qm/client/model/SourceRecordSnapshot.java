package org.folio.qm.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record SourceRecordSnapshot(UUID jobExecutionId, Status status) {

  public static SourceRecordSnapshot snapshot() {
    return new SourceRecordSnapshot(UUID.randomUUID(), Status.PARSING_IN_PROGRESS);
  }

  public enum Status {
    PARENT("PARENT"),
    NEW("NEW"),
    FILE_UPLOADED("FILE_UPLOADED"),
    PARSING_IN_PROGRESS("PARSING_IN_PROGRESS"),
    PARSING_FINISHED("PARSING_FINISHED"),
    PROCESSING_IN_PROGRESS("PROCESSING_IN_PROGRESS"),
    PROCESSING_FINISHED("PROCESSING_FINISHED"),
    COMMIT_IN_PROGRESS("COMMIT_IN_PROGRESS"),
    COMMITTED("COMMITTED"),
    ERROR("ERROR"),
    DISCARDED("DISCARDED"),
    CANCELLED("CANCELLED");

    private static final Map<String, Status> CONSTANTS = new HashMap<>();

    static {
      for (Status c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    private final String value;

    Status(String value) {
      this.value = value;
    }

    @JsonCreator
    public static Status fromValue(String value) {
      Status constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }

    @JsonValue
    public String value() {
      return this.value;
    }
  }
}
