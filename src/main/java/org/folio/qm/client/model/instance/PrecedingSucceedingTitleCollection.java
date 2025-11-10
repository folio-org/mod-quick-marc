package org.folio.qm.client.model.instance;

import java.util.List;
import lombok.Getter;

@Getter
public class PrecedingSucceedingTitleCollection {

  private final List<PrecedingSucceedingTitle> precedingSucceedingTitles;
  private final int totalRecords;

  public PrecedingSucceedingTitleCollection(List<PrecedingSucceedingTitle> precedingSucceedingTitles,
                                            int totalRecords) {
    this.precedingSucceedingTitles = precedingSucceedingTitles;
    this.totalRecords = totalRecords;
  }
}
