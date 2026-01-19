package org.folio.support;

import java.util.List;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.model.BaseQuickMarcRecord;

public class StubQuickMarcRecord implements BaseQuickMarcRecord {

  private String leader;
  private List<FieldItem> fields;
  private Boolean suppressDiscovery;
  private MarcFormat marcFormat;
  private Integer sourceVersion;

  public StubQuickMarcRecord() {
  }

  public StubQuickMarcRecord(MarcFormat marcFormat, String leader, List<FieldItem> fields) {
    this.marcFormat = marcFormat;
    this.leader = leader;
    this.fields = fields;
  }

  @Override
  public String getLeader() {
    return leader;
  }

  @Override
  public void setLeader(String leader) {
    this.leader = leader;
  }

  @Override
  public BaseQuickMarcRecord fields(List<FieldItem> fields) {
    this.fields = fields;
    return this;
  }

  @Override
  public BaseQuickMarcRecord addFieldsItem(FieldItem fieldsItem) {
    fields.add(fieldsItem);
    return this;
  }

  @Override
  public List<FieldItem> getFields() {
    return fields;
  }

  @Override
  public void setFields(List<FieldItem> fields) {
    this.fields = fields;
  }

  @Override
  public BaseQuickMarcRecord suppressDiscovery(Boolean suppressDiscovery) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Boolean getSuppressDiscovery() {
    return suppressDiscovery;
  }

  @Override
  public void setSuppressDiscovery(Boolean suppressDiscovery) {
    this.suppressDiscovery = suppressDiscovery;
  }

  @Override
  public BaseQuickMarcRecord marcFormat(MarcFormat marcFormat) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MarcFormat getMarcFormat() {
    return marcFormat;
  }

  @Override
  public void setMarcFormat(MarcFormat marcFormat) {
    this.marcFormat = marcFormat;
  }

  @Override
  public BaseQuickMarcRecord sourceVersion(Integer sourceVersion) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Integer getSourceVersion() {
    return sourceVersion;
  }

  @Override
  public void setSourceVersion(Integer sourceVersion) {
    this.sourceVersion = sourceVersion;
  }
}
