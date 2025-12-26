package org.folio.qm.service;

import java.util.UUID;
import org.folio.qm.domain.dto.QuickMarcView;

public interface FetchRecordService {

  QuickMarcView fetchByExternalId(UUID externalId);
}
