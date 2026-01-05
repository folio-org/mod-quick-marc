package org.folio.qm.service.population;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.model.BaseQuickMarcRecord;

public interface MarcPopulationService {
  void populate(BaseQuickMarcRecord quickMarc);

  boolean supportFormat(MarcFormat marcFormat);
}
