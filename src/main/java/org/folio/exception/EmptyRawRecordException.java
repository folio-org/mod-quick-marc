package org.folio.exception;

public class EmptyRawRecordException extends RuntimeException {
  public EmptyRawRecordException() {
    super("RawRecord content should not be empty");
  }
}
