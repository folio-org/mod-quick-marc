package org.folio.qm.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataImportEventPayload {

  private String eventType;
  private List<String> currentNodePath = new ArrayList<>();
  private List<String> eventsChain = new ArrayList<>();
  private UUID jobExecutionId;
  private String tenant;
  private String token;
  private String okapiUrl;
  private Map<String, String> context = new HashMap<>();
}

