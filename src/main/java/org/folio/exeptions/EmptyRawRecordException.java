package org.folio.exeptions;

public class EmptyRawRecordException extends RuntimeException {
  public EmptyRawRecordException(){
    super("RawRecord content should not be empty");
  }
}
