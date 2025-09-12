package org.folio.qm.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class BaseSrsMarcRecord {

  private String leader;
  private List<Map<String, SrsFieldItem>> fields = new ArrayList<>();
}

