package org.folio.qm.domain.model;

import java.util.List;
import java.util.Objects;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ValidatableRecord;

public class ValidatableRecordDelegate implements BaseQuickMarcRecord {

  private final ValidatableRecord validatableRecord;

  public ValidatableRecordDelegate(ValidatableRecord validatableRecord) {
    this.validatableRecord = validatableRecord;
  }

  @Override
  public String getLeader() {
    return validatableRecord.getLeader();
  }

  @Override
  public void setLeader(String leader) {
    validatableRecord.setLeader(leader);
  }

  @Override
  public BaseQuickMarcRecord fields(List<FieldItem> fields) {
    return this;
  }

  @Override
  public BaseQuickMarcRecord addFieldsItem(FieldItem fieldsItem) {
    return this;
  }

  @Override
  public List<FieldItem> getFields() {
    return validatableRecord.getFields().stream()
      .map(field -> new FieldItem().tag(field.getTag()).content(field.getContent()).indicators(field.getIndicators()))
      .toList();
  }

  @Override
  public void setFields(List<FieldItem> fields) {
    // do nothing
  }

  @Override
  public BaseQuickMarcRecord suppressDiscovery(Boolean suppressDiscovery) {
    return this;
  }

  @Override
  public Boolean getSuppressDiscovery() {
    return false;
  }

  @Override
  public void setSuppressDiscovery(Boolean suppressDiscovery) {
    // do nothing
  }

  @Override
  public BaseQuickMarcRecord marcFormat(MarcFormat marcFormat) {
    return this;
  }

  @Override
  public MarcFormat getMarcFormat() {
    return validatableRecord.getMarcFormat();
  }

  @Override
  public void setMarcFormat(MarcFormat marcFormat) {
    // do nothing
  }

  @Override
  public BaseQuickMarcRecord sourceVersion(Integer sourceVersion) {
    return this;
  }

  @Override
  public Integer getSourceVersion() {
    return 0;
  }

  @Override
  public void setSourceVersion(Integer sourceVersion) {
    // do nothing
  }

  @Override
  public int hashCode() {
    return validatableRecord.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var vr = (ValidatableRecordDelegate) o;
    return Objects.equals(this.getLeader(), vr.getLeader())
           && Objects.equals(this.getFields(), vr.getFields())
           && Objects.equals(this.getMarcFormat(), vr.getMarcFormat());
  }
}
