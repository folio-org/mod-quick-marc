package org.folio.qm.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InitJobExecutionsRsDto {

  private UUID parentJobExecutionId;
  private List<JobExecution> jobExecutions = new ArrayList<>();
}

