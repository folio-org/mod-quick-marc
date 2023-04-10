package org.folio.qm.client;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "linking-rules", dismiss404 = true)
public interface LinkingRulesClient {

  @GetMapping(value = "instance-authority", produces = MediaType.APPLICATION_JSON_VALUE)
  List<LinkingRuleDto> fetchLinkingRules();

  @Data
  @Accessors(chain = true)
  class LinkingRuleDto {
    private Integer id;
    private String bibField;
  }
}
