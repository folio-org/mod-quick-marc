package org.folio.qm.service.storage.folio;

import java.util.UUID;
import org.folio.qm.domain.FolioRecord;

public interface FolioRecordService<T extends FolioRecord> {

  T get(UUID id);

  T create(T folioRecord);

  void update(UUID id, T folioRecord);
}
