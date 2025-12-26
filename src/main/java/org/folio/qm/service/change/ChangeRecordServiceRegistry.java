package org.folio.qm.service.change;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.folio.qm.domain.dto.MarcFormat;
import org.springframework.stereotype.Component;

@Component
public class ChangeRecordServiceRegistry {

  private final Map<MarcFormat, ChangeRecordService> services;

  public ChangeRecordServiceRegistry(List<ChangeRecordService> serviceList) {
    this.services = serviceList.stream()
      .collect(Collectors.toUnmodifiableMap(
        ChangeRecordService::supportedType,
        Function.identity()
      ));
  }

  @SuppressWarnings("java:S1452")
  public ChangeRecordService get(MarcFormat marcFormat) {
    var service = services.get(marcFormat);
    if (service == null) {
      throw new IllegalArgumentException("No MARC RecordService found for record type: " + marcFormat);
    }
    return service;
  }
}
