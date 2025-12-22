package org.folio.qm.client.model;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.folio.ParsedRecord;

@Data
@NoArgsConstructor
public class SourceRecord {

  private UUID recordId;
  private RecordTypeEnum recordType;
  private ParsedRecord parsedRecord;
  private Boolean deleted = false;
  private Integer generation;
  private ExternalIdsHolder externalIdsHolder;
  private AdditionalInfo additionalInfo;
  private Metadata metadata;
}

