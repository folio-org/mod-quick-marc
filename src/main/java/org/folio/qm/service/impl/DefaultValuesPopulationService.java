package org.folio.qm.service.impl;

import java.util.List;
import java.util.Optional;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.service.population.MarcPopulationService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

@Service
public class DefaultValuesPopulationService {

  private final List<MarcPopulationService> marcPopulationServices;
  private final ConversionService conversionService;

  public DefaultValuesPopulationService(List<MarcPopulationService> marcPopulationServices,
                                        ConversionService conversionService) {
    this.marcPopulationServices = marcPopulationServices;
    this.conversionService = conversionService;
  }

  public void populate(BaseMarcRecord quickMarc) {
    marcPopulationServices.stream()
      .filter(marcPopulationService -> marcPopulationService.supportFormat(quickMarc.getMarcFormat()))
      .forEach(marcPopulationService -> marcPopulationService.populate(quickMarc));
  }

  public void populate(ValidatableRecord validatableRecord) {
    var baseMarcRecord = conversionService.convert(validatableRecord, BaseMarcRecord.class);
    marcPopulationServices.stream()
      .filter(marcPopulationService -> marcPopulationService.supportFormat(validatableRecord.getMarcFormat()))
      .forEach(marcPopulationService -> marcPopulationService.populate(baseMarcRecord));

    Optional.ofNullable(conversionService.convert(baseMarcRecord, ValidatableRecord.class))
      .ifPresent(populatedRecord -> {
        validatableRecord.setLeader(populatedRecord.getLeader());
        validatableRecord.setFields(populatedRecord.getFields());
        validatableRecord.setMarcFormat(populatedRecord.getMarcFormat());
      });
  }


}
