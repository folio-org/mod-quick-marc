package org.folio.qm.service.impl;

import java.util.List;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.service.population.MarcPopulationService;
import org.springframework.stereotype.Service;

@Service
public class DefaultValuesPopulationService {

  private final List<MarcPopulationService> marcPopulationServices;

  public DefaultValuesPopulationService(List<MarcPopulationService> marcPopulationServices) {
    this.marcPopulationServices = marcPopulationServices;
  }

  public void populate(QuickMarc quickMarc) {
    marcPopulationServices.stream()
      .filter(marcPopulationService -> marcPopulationService.supportFormat(quickMarc.getMarcFormat()))
      .forEach(marcPopulationService -> marcPopulationService.populate(quickMarc));
  }
}
