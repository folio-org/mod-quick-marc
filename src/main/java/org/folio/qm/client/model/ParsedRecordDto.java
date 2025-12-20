package org.folio.qm.client.model;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.folio.ParsedRecord;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ParsedRecordDto {

  private UUID id;
  private ExternalIdsHolder externalIdsHolder;
  private RecordTypeEnum recordType;
  private ParsedRecord parsedRecord;
  private AdditionalInfo additionalInfo;
  private Metadata metadata;
}

