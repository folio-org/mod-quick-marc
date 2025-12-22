package org.folio.qm.exception;

public class MappingMetadataException extends RuntimeException {

  public MappingMetadataException(String message) {
    super(message);
  }

  public MappingMetadataException(String message, Throwable cause) {
    super(message, cause);
  }

  public MappingMetadataException(Throwable cause) {
    super(cause);
  }
}
