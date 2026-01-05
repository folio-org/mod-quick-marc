package org.folio.qm.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.InstancePrecedingSucceedingTitle;

@Getter
public class InstanceRecord extends Instance implements FolioRecord {

  @Setter
  @JsonIgnore
  private List<InstancePrecedingSucceedingTitle> precedingTitles;
  @Setter
  @JsonIgnore
  private List<InstancePrecedingSucceedingTitle> succeedingTitles;

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    return super.equals(other);
  }
}
