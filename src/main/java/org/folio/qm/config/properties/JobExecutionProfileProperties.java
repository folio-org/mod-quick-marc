package org.folio.qm.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "folio.job.execution.profile")
public class JobExecutionProfileProperties {

  private ProfileOptions marcBib;
  private ProfileOptions marcHoldings;

  @Data
  public static class ProfileOptions {

    private String id;
    private String name;
  }
}
