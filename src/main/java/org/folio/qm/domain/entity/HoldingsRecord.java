package org.folio.qm.domain.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.folio.Holdings;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class HoldingsRecord extends Holdings {
  private List<AdditionalCallNumber> additionalCallNumbers = new ArrayList<>();
}
