package org.folio.qm.service.storage.source;

import java.util.UUID;
import org.folio.Record;
import org.folio.qm.client.model.SourceRecord;

public interface SourceRecordService {

  Record get(UUID id);

  SourceRecord getByExternalId(UUID externalId);

  Record create(Record sourceRecord);

  void update(UUID id, Record sourceRecord);
}
