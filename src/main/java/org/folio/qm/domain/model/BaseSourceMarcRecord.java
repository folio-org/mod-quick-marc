package org.folio.qm.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class BaseSourceMarcRecord {

  private String leader;
  private List<Map<String, SourceFieldItem>> fields = new ArrayList<>();
}

