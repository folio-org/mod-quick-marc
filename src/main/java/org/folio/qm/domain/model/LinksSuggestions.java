package org.folio.qm.domain.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class LinksSuggestions {

  private List<BaseSourceMarcRecord> records = new ArrayList<>();
}

