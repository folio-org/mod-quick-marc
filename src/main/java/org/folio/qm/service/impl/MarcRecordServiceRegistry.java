package org.folio.qm.service.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.folio.qm.client.model.RecordTypeEnum;
import org.folio.qm.service.RecordService;
import org.springframework.stereotype.Component;

@Component
public class MarcRecordServiceRegistry {
  private final Map<RecordTypeEnum, RecordService<?>> services;

  public MarcRecordServiceRegistry(List<RecordService<?>> serviceList) {
    this.services = serviceList.stream()
      .collect(Collectors.toUnmodifiableMap(
        RecordService::supportedType,
        Function.identity()
      ));
  }

  @SuppressWarnings("java:S1452")
  public RecordService<?> get(RecordTypeEnum type) {
    RecordService<?> service = services.get(type);
    if (service == null) {
      throw new IllegalArgumentException(
        "No MARC RecordService found for record type: " + type);
    }
    return service;
  }
}
