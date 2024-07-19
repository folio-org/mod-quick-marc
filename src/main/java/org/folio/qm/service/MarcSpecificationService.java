package org.folio.qm.service;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.rspec.domain.dto.SpecificationDto;

public interface MarcSpecificationService {

  SpecificationDto getSpecification(MarcFormat marcFormat);
}
