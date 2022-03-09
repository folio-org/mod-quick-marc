package org.folio.qm.domain.entity;

import org.folio.qm.domain.dto.MarcFormat;

public enum RecordType {

  MARC_BIBLIOGRAPHIC,
  MARC_HOLDINGS,
  MARC_AUTHORITY;

  public static RecordType from(MarcFormat marcFormat) {
    switch (marcFormat) {
      case BIBLIOGRAPHIC:
        return MARC_BIBLIOGRAPHIC;
      case HOLDINGS:
        return MARC_HOLDINGS;
      case AUTHORITY:
        return MARC_AUTHORITY;
      default:
        throw new IllegalArgumentException(String.format("MARC format [%s] is not supported", marcFormat));
    }
  }
}
