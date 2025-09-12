package org.folio.qm.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class RawRecordsDto {

  private UUID id;
  private List<RawRecordDto> initialRecords = new ArrayList<>();
  private RawRecordsMetadata recordsMetadata;
}

