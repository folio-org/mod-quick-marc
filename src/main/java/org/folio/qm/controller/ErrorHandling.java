package org.folio.qm.controller;

import static feign.Util.UTF_8;

import static org.folio.qm.util.ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED;
import static org.folio.qm.util.ErrorUtils.ErrorType.INTERNAL;
import static org.folio.qm.util.ErrorUtils.ErrorType.UNKNOWN;
import static org.folio.qm.util.ErrorUtils.buildError;

import javax.servlet.http.HttpServletResponse;

import feign.FeignException;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.folio.qm.exception.QuickMarcException;
import org.folio.spring.exception.NotFoundException;
import org.folio.tenant.domain.dto.Error;

@RestControllerAdvice
public class ErrorHandling {

  @ExceptionHandler(FeignException.class)
  public Error handleFeignStatusException(FeignException e, HttpServletResponse response) {
    var status = e.status();
    var message = e.responseBody()
      .map(byteBuffer -> new String(byteBuffer.array(), UTF_8))
      .orElse(StringUtils.EMPTY);
    response.setStatus(status);
    return buildError(status, FOLIO_EXTERNAL_OR_UNDEFINED, message);
  }

  @ExceptionHandler(QuickMarcException.class)
  public Error handleConverterException(QuickMarcException e, HttpServletResponse response) {
    var code = e.getStatus();
    response.setStatus(code);
    return e.getError();
  }

  @ExceptionHandler(NotFoundException.class)
  public Error handleNotFoundException(NotFoundException e, HttpServletResponse response) {
    var status = HttpStatus.NOT_FOUND.value();
    response.setStatus(status);
    return buildError(status, INTERNAL, e.getMessage());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public Error handleMissingParameterException(MissingServletRequestParameterException e, HttpServletResponse response) {
    var message = "Parameter '" + e.getParameterName() + "' is required";
    return buildBadRequestResponse(response, message);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public Error handleGlobalException(MethodArgumentTypeMismatchException e, HttpServletResponse response) {
    var message = "Parameter '" + e.getParameter().getParameterName() + "' is invalid";
    return buildBadRequestResponse(response, message);
  }

  private Error buildBadRequestResponse(HttpServletResponse response, String message) {
    var status = HttpStatus.BAD_REQUEST.value();
    response.setStatus(status);
    return buildError(status, INTERNAL, message);
  }

  @ExceptionHandler(Exception.class)
  public Error handleGlobalException(Exception e, HttpServletResponse response) {
    var status = HttpStatus.INTERNAL_SERVER_ERROR.value();
    response.setStatus(status);
    return buildError(status, UNKNOWN, e.getMessage());
  }
}
