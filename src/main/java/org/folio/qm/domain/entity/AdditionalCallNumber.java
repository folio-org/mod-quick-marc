package org.folio.qm.domain.entity;

import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class AdditionalCallNumber {
  private String typeId;
  private String prefix;
  private String callNumber;
  private String suffix;
  private Map<String, Object> additionalProperties = new HashMap<>();
}
