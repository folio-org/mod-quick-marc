package org.folio.qm.client.model;

import lombok.Data;
import org.folio.ErrorRecord;
import org.folio.Metadata;
import org.folio.RawRecord;

@Data
public class Record {

  private String id;
  private String snapshotId;
  private String matchedId;
  private Integer generation;
  private RecordType recordType;
  private RawRecord rawRecord;
  private ParsedRecord parsedRecord;
  private ErrorRecord errorRecord;
  private Boolean deleted = false;
  private Integer order;
  private ExternalIdsHolder externalIdsHolder;
  private AdditionalInfo additionalInfo;
  private State state = State.ACTUAL;
  private String leaderRecordStatus;
  private Metadata metadata;

  public enum RecordType {
    MARC_BIB, MARC_AUTHORITY, MARC_HOLDING, EDIFACT
  }

  public enum State {
    ACTUAL, OLD, DRAFT, DELETED
  }
}
