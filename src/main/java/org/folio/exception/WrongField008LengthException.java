package org.folio.exception;

public class WrongField008LengthException extends RuntimeException {
  public WrongField008LengthException() {
    super("Filed 008 must be 40 characters length");
  }
}
