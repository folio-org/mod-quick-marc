package org.folio.qm.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MappingRecordType {

  MARC_BIB("marc-bib"),
  MARC_HOLDINGS("marc-holdings"),
  MARC_AUTHORITY("marc-authority");

  private final String value;

  MappingRecordType(String value) {
    this.value = value;
  }

  @JsonCreator
  public static MappingRecordType fromValue(String value) {
    for (var b : MappingRecordType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }

  @JsonValue
  public String value() {
    return this.value;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
