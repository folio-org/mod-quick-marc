package org.folio.qm.client.model;

import lombok.Getter;

@Getter
public enum MappingRecordTypeEnum {

  MARC_BIB("marc-bib"),
  MARC_HOLDINGS("marc-holdings"),
  MARC_AUTHORITY("marc-authority");

  private final String value;

  MappingRecordTypeEnum(String value) {
    this.value = value;
  }
}
