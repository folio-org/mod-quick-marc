package org.folio.qm.client.model;

import java.util.Date;
import lombok.Data;
import org.folio.Metadata;

@Data
public class Snapshot {
  private String jobExecutionId;
  private Status status;
  private Date processingStartedDate;
  private Metadata metadata;

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

    private final String value;

    Status(String value) {
      this.value = value;
    }

    public String value() {
      return this.value;
    }
  }
}
