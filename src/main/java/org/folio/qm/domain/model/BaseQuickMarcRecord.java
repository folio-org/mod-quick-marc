package org.folio.qm.domain.model;

import jakarta.validation.Valid;
import java.util.List;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

public interface BaseQuickMarcRecord {

  String getLeader();

  void setLeader(String leader);

  BaseQuickMarcRecord fields(List<@Valid FieldItem> fields);

  BaseQuickMarcRecord addFieldsItem(FieldItem fieldsItem);

  List<FieldItem> getFields();

  void setFields(List<@Valid FieldItem> fields);

  BaseQuickMarcRecord suppressDiscovery(Boolean suppressDiscovery);

  Boolean getSuppressDiscovery();

  void setSuppressDiscovery(Boolean suppressDiscovery);

  BaseQuickMarcRecord marcFormat(MarcFormat marcFormat);

  MarcFormat getMarcFormat();

  void setMarcFormat(MarcFormat marcFormat);

  BaseQuickMarcRecord sourceVersion(Integer sourceVersion);

  Integer getSourceVersion();

  void setSourceVersion(Integer sourceVersion);
}

