package org.folio.qm.service.population;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;

public interface MarcPopulationService {
  void populate(QuickMarc quickMarc);
  boolean supportFormat(MarcFormat marcFormat);
}
