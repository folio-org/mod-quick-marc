package org.folio.qm.service.population;

import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.MarcFormat;

public interface MarcPopulationService {
  void populate(BaseMarcRecord quickMarc);

  boolean supportFormat(MarcFormat marcFormat);
}
