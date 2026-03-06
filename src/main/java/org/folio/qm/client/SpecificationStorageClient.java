package org.folio.qm.client;

import java.util.UUID;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "specification-storage", contentType = MediaType.APPLICATION_JSON_VALUE)
public interface SpecificationStorageClient {

  @GetExchange(value = "/specifications?family=MARC&include=all&limit=1")
  SpecificationDtoCollection getSpecifications(@RequestParam("profile") String profile);

  @GetExchange(value = "/specifications/{specificationId}?include=all")
  SpecificationDto getSpecification(@PathVariable("specificationId") UUID specificationId);
}
