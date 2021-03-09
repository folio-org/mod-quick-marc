package org.folio.qm.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "job.execution.profile.default")
public class JobExecutionProfileProperties {
  private String id;
  private String name;
}
