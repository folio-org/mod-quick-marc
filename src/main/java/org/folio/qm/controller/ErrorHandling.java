package org.folio.qm.controller;

import static feign.Util.UTF_8;

import javax.servlet.http.HttpServletResponse;

import feign.FeignException;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.folio.qm.domain.dto.Error;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.util.ErrorUtils;

@RestControllerAdvice
public class ErrorHandling {

  @ExceptionHandler(FeignException.class)
  public Error handleFeignStatusException(FeignException e, HttpServletResponse response) {
    var status = e.status();
    var message = e.responseBody()
      .map(byteBuffer -> new String(byteBuffer.array(), UTF_8))
      .orElse(StringUtils.EMPTY);
    response.setStatus(status);
    return ErrorUtils.buildError(status, ErrorUtils.ErrorType.EXTERNAL_OR_UNDEFINED, message);
  }

  @ExceptionHandler(ConverterException.class)
  public Error handleConverterException(ConverterException e, HttpServletResponse response) {
    var code = e.getStatus();
    response.setStatus(code);
    return e.getError();
  }
}
