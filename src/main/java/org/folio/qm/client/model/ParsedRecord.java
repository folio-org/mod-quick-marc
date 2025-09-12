package org.folio.qm.client.model;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ParsedRecord {

  private UUID id;
  private Object content;

  public ParsedRecord(Object content) {
    this.content = content;
  }
}

