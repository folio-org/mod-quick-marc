package org.folio.qm.exception;

import org.springframework.core.convert.ConversionException;

public class NullConvertionException extends ConversionException {

  private static final String MESSAGE = "Conversion resulted in null for target type: %s";

  public NullConvertionException(Class<?> targetType) {
    super(MESSAGE.formatted(targetType.getName()));
  }
}
