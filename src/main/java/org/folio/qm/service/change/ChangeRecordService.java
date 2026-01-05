package org.folio.qm.service.change;

import java.util.UUID;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;

public interface ChangeRecordService {

  void update(UUID recordId, QuickMarcEdit qmRecord);

  QuickMarcView create(QuickMarcCreate qmRecord);

  MarcFormat supportedType();
}
