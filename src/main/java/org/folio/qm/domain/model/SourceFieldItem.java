package org.folio.qm.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.folio.qm.domain.dto.LinkDetails;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SourceFieldItem {

  private String ind1;
  private String ind2;
  private List<Map<String, String>> subfields = new ArrayList<>();
  private LinkDetails linkDetails;
}

