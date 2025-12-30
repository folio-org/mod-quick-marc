package org.folio.qm.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.folio.Record;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "source-storage")
public interface SourceStorageClient {

  @GetMapping(value = "/records/{id}/formatted", produces = MediaType.APPLICATION_JSON_VALUE)
  Optional<Record> getSourceRecord(@PathVariable UUID id, @RequestParam("idType") IdType idType);

  @GetMapping(value = "/records/{id}")
  Optional<Record> getSourceRecord(@PathVariable UUID id);

  @PostMapping(value = "/records", produces = MediaType.APPLICATION_JSON_VALUE)
  Record createSourceRecord(@RequestBody Record sourceRecord);

  @PutMapping(value = "/records/{id}/generation")
  void updateSourceRecord(@PathVariable UUID id, Record sourceRecord);

  @PostMapping(value = "/snapshots")
  SourceRecordSnapshot createSnapshot(SourceRecordSnapshot snapshot);

  enum IdType {
    EXTERNAL
  }

  record SourceRecordSnapshot(UUID jobExecutionId, Status status) {

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
        for (Status c : Status.values()) {
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
}
