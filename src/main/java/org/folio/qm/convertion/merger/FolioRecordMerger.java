package org.folio.qm.convertion.merger;

import org.folio.qm.domain.model.FolioRecord;
import org.mapstruct.MappingTarget;

public interface FolioRecordMerger<F extends FolioRecord, E> {

  void merge(E source, @MappingTarget F target);
}
