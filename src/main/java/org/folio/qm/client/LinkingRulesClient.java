package org.folio.qm.client;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "linking-rules", accept = MediaType.APPLICATION_JSON_VALUE)
public interface LinkingRulesClient {

  @GetExchange(value = "instance-authority")
  List<LinkingRuleDto> fetchLinkingRules();

  @Data
  @Accessors(chain = true)
  class LinkingRuleDto {
    private Integer id;
    private String bibField;
  }
}
