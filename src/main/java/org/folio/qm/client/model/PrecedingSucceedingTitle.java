package org.folio.qm.client.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.folio.Identifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrecedingSucceedingTitle {

  private String id;
  private String precedingInstanceId;
  private String succeedingInstanceId;
  private String title;
  private String hrid;
  private List<Identifier> identifiers;
}
