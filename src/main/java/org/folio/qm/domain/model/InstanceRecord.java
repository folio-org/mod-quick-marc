package org.folio.qm.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.InstancePrecedingSucceedingTitle;

@Getter
public class InstanceRecord extends Instance implements FolioRecord {

  @JsonIgnore
  private List<InstancePrecedingSucceedingTitle> precedingTitles;
  @JsonIgnore
  private List<InstancePrecedingSucceedingTitle> succeedingTitles;
}
