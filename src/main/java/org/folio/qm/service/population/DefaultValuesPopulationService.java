package org.folio.qm.service.population;

import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

@Log4j2
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
    log.debug("populate:: Populating default values for MARC record with format: {}", quickMarc.getMarcFormat());
    findMatchingServices(quickMarc.getMarcFormat())
      .forEach(marcPopulationService -> marcPopulationService.populate(quickMarc));
    log.debug("populate:: Default values populated successfully");
  }

  public void populate(ValidatableRecord validatableRecord) {
    log.debug("populate:: Populating default values for validatable record with format: {}",
      validatableRecord.getMarcFormat());
    var baseMarcRecord = conversionService.convert(validatableRecord, BaseMarcRecord.class);

    findMatchingServices(validatableRecord.getMarcFormat())
      .forEach(marcPopulationService -> marcPopulationService.populate(baseMarcRecord));

    Optional.ofNullable(conversionService.convert(baseMarcRecord, ValidatableRecord.class))
      .ifPresent(populatedRecord -> {
        validatableRecord.setLeader(populatedRecord.getLeader());
        validatableRecord.setFields(populatedRecord.getFields());
        validatableRecord.setMarcFormat(populatedRecord.getMarcFormat());
      });
    log.debug("populate:: Default values populated successfully for validatable record");
  }

  private List<MarcPopulationService> findMatchingServices(MarcFormat marcFormat) {
    var applicableServices = marcPopulationServices.stream()
      .filter(marcPopulationService -> marcPopulationService.supportFormat(marcFormat))
      .toList();

    log.trace("populate:: Found {} applicable population services", applicableServices.size());
    return applicableServices;
  }
}
