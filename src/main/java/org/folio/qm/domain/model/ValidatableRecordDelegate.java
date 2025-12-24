package org.folio.qm.domain.model;

import java.util.List;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ValidatableRecord;

public class ValidatableRecordDelegate extends BaseMarcRecord {

  private final ValidatableRecord validatableRecord;

  public ValidatableRecordDelegate(ValidatableRecord validatableRecord) {
    this.validatableRecord = validatableRecord;
  }

  @Override
  public String getLeader() {
    return validatableRecord.getLeader();
  }

  @Override
  public List<FieldItem> getFields() {
    return validatableRecord.getFields().stream()
      .map(field -> new FieldItem().tag(field.getTag()).content(field.getContent()).indicators(field.getIndicators()))
      .toList();
  }

  @Override
  public MarcFormat getMarcFormat() {
    return validatableRecord.getMarcFormat();
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return validatableRecord.hashCode();
  }
}
