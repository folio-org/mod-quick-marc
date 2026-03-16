package org.folio.qm.service.storage.source;

import java.util.UUID;
import org.folio.Record;

public interface SourceRecordService {

  Record get(UUID id);

  Record getByExternalId(UUID externalId);

  Record create(Record sourceRecord);

  void update(UUID id, Record sourceRecord);
}
