package org.folio.qm.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.folio.Record;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(url = "source-storage", accept = MediaType.APPLICATION_JSON_VALUE)
public interface SourceStorageClient {

  @GetExchange(value = "/records/{id}/formatted")
  Optional<Record> getSourceRecord(@PathVariable("id") UUID id, @RequestParam("idType") IdType idType);

  @GetExchange(value = "/records/{id}")
  Optional<Record> getSourceRecord(@PathVariable("id") UUID id);

  @PostExchange(value = "/records")
  Record createSourceRecord(@RequestBody Record sourceRecord);

  @PutExchange(value = "/records/{id}/generation")
  void updateSourceRecord(@PathVariable("id") UUID id, @RequestBody Record sourceRecord);

  @PostExchange(value = "/snapshots")
  SourceRecordSnapshot createSnapshot(@RequestBody SourceRecordSnapshot snapshot);

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
