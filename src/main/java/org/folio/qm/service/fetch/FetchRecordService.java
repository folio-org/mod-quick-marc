package org.folio.qm.service.fetch;

import java.util.UUID;
import org.folio.qm.domain.dto.QuickMarcView;

public interface FetchRecordService {

  /**
   * Fetches and converts record; applies protection; sets links.
   *
   * @param externalId the external ID of the record to fetch
   * @return QuickMarcView
   */
  QuickMarcView fetchByExternalId(UUID externalId);
}
