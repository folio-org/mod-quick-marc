package org.folio.qm.client.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MarcFieldProtectionSettingsCollection {

  private List<MarcFieldProtectionSetting> marcFieldProtectionSettings = new ArrayList<>();
  private Integer totalRecords;
}

