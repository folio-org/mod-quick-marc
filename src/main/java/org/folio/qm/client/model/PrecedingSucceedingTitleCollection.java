package org.folio.qm.client.model;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PrecedingSucceedingTitleCollection {

  private List<PrecedingSucceedingTitle> precedingSucceedingTitles;
  private int totalRecords;

  public PrecedingSucceedingTitleCollection(List<PrecedingSucceedingTitle> precedingSucceedingTitles,
                                            int totalRecords) {
    this.precedingSucceedingTitles = precedingSucceedingTitles;
    this.totalRecords = totalRecords;
  }
}
