package org.folio.qm.client.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.folio.Holdings;

@Data
@EqualsAndHashCode(callSuper = true)
public class HoldingsRecord extends Holdings {
  private List<AdditionalCallNumber> additionalCallNumbers = new ArrayList<>();
}
