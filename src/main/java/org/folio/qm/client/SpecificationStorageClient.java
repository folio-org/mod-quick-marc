package org.folio.qm.client;

import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "specification-storage")
public interface SpecificationStorageClient {

  @GetMapping(value = "/specifications?family=MARC&include=all&limit=1",
              produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  SpecificationDtoCollection getSpecifications(@RequestParam("profile") String profile);
}
