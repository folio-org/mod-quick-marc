package org.folio.qm.validation;

import lombok.Value;

@Value
public class ValidationError {

  String tag;
  String message;
}
