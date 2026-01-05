package org.folio.qm.service.change;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.domain.dto.MarcFormat;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class ChangeRecordServiceRegistry {

  private final Map<MarcFormat, ChangeRecordService> services;

  public ChangeRecordServiceRegistry(List<ChangeRecordService> serviceList) {
    this.services = serviceList.stream()
      .collect(Collectors.toUnmodifiableMap(
        ChangeRecordService::supportedType,
        Function.identity()
      ));
    log.info("ChangeRecordServiceRegistry:: Initialized with {} record services", services.size());
  }

  public ChangeRecordService get(MarcFormat marcFormat) {
    log.debug("get:: Retrieving service for format: {}", marcFormat);
    var service = services.get(marcFormat);
    if (service == null) {
      log.error("get:: No service found for format: {}", marcFormat);
      throw new IllegalArgumentException("No record service found for record type: " + marcFormat);
    }
    return service;
  }
}
