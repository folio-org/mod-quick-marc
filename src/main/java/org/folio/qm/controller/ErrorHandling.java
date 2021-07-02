package org.folio.qm.controller;

import static feign.Util.UTF_8;
import static org.folio.qm.util.ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED;
import static org.folio.qm.util.ErrorUtils.ErrorType.INTERNAL;
import static org.folio.qm.util.ErrorUtils.ErrorType.UNKNOWN;
import static org.folio.qm.util.ErrorUtils.buildError;

import feign.FeignException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import org.apache.commons.lang.StringUtils;
import org.folio.qm.exception.QuickMarcException;
import org.folio.spring.exception.NotFoundException;
import org.folio.tenant.domain.dto.Error;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ErrorHandling {

  @ExceptionHandler(FeignException.class)
  public Error handleFeignStatusException(FeignException e, HttpServletResponse response) {
    var status = e.status();
    if (status != -1) {
      var message = e.responseBody()
        .map(byteBuffer -> new String(byteBuffer.array(), UTF_8))
        .orElse(StringUtils.EMPTY);
      response.setStatus(status);
      return buildError(status, FOLIO_EXTERNAL_OR_UNDEFINED, message);
    } else {
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      return buildError(HttpStatus.BAD_REQUEST, FOLIO_EXTERNAL_OR_UNDEFINED, e.getMessage());
    }
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public Error handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    FieldError fieldError = e.getBindingResult().getFieldError();
    if (fieldError != null) {
      String message = "Parameter '" + fieldError.getField() + "' " + fieldError.getDefaultMessage();
      return buildError(HttpStatus.BAD_REQUEST, INTERNAL, message);
    } else {
      return buildError(HttpStatus.BAD_REQUEST, INTERNAL, e.getMessage());
    }
  }

  @ExceptionHandler(QuickMarcException.class)
  public Error handleConverterException(QuickMarcException e, HttpServletResponse response) {
    var code = e.getStatus();
    response.setStatus(code);
    return e.getError();
  }

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  public Error handleNotFoundException(NotFoundException e) {
    return buildError(HttpStatus.NOT_FOUND, INTERNAL, e.getMessage());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Error handleMissingParameterException(MissingServletRequestParameterException e, HttpServletResponse response) {
    var message = "Parameter '" + e.getParameterName() + "' is required";
    return buildBadRequestResponse(message);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Error handleGlobalException(MethodArgumentTypeMismatchException e) {
    var message = "Parameter '" + e.getParameter().getParameterName() + "' is invalid";
    return buildBadRequestResponse(message);
  }

  private Error buildBadRequestResponse(String message) {
    return buildError(HttpStatus.BAD_REQUEST, INTERNAL, message);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Error handleConstraintViolationException(Exception e) {
    return buildError(HttpStatus.BAD_REQUEST, INTERNAL, "Parameter " + e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Error handleGlobalException(Exception e) {
    return buildError(HttpStatus.INTERNAL_SERVER_ERROR, UNKNOWN, e.getMessage());
  }
}
