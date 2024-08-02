package org.folio.qm.service;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;

public interface MarcSpecificationService {

  SpecificationDto getSpecification(MarcFormat marcFormat);

  void updateSpecificationCache(SpecificationUpdatedEvent specificationUpdate);
}
