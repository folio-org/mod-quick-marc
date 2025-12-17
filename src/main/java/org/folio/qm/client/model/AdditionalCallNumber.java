package org.folio.qm.client.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdditionalCallNumber {
  private String typeId;
  private String prefix;
  private String callNumber;
  private String suffix;
  private Map<String, Object> additionalProperties = new HashMap<>();
}
