package org.folio.qm.controller;

import static feign.Util.UTF_8;

import static org.folio.qm.util.ErrorUtils.ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED;
import static org.folio.qm.util.ErrorUtils.ErrorType.INTERNAL;
import static org.folio.qm.util.ErrorUtils.ErrorType.UNKNOWN;
import static org.folio.qm.util.ErrorUtils.buildError;
import static org.folio.qm.util.ErrorUtils.buildErrors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import feign.FeignException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.folio.qm.exception.FieldsValidationException;
import org.folio.qm.exception.JobProfileNotFoundException;
import org.folio.qm.exception.QuickMarcException;
import org.folio.spring.exception.NotFoundException;
import org.folio.tenant.domain.dto.Error;

@Log4j2
@RestControllerAdvice
public class ErrorHandling {

  private static final String TYPE_MISMATCH_MSG_PATTERN = "Parameter '%s' is invalid";
  private static final String MISSING_PARAMETER_MSG_PATTERN = "Parameter '%s' is required";
  private static final String ARGUMENT_NOT_VALID_MSG_PATTERN = "Parameter '%s' %s";
  private static final String CONSTRAINT_VIOLATION_MSG_PATTERN = "Parameter %s";

  @ExceptionHandler(FeignException.class)
  public Error handleFeignStatusException(FeignException e, HttpServletResponse response) {
    var status = e.status();
    if (status != -1) {
      var message = e.responseBody()
        .map(byteBuffer -> new String(byteBuffer.array(), UTF_8))
        .orElse(StringUtils.EMPTY);
      response.setStatus(status);
      log.warn(message);
      return buildErrors(status, FOLIO_EXTERNAL_OR_UNDEFINED, message);
    } else {
      log.warn(e.getMessage());
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      return buildError(HttpStatus.BAD_REQUEST, FOLIO_EXTERNAL_OR_UNDEFINED, e.getMessage());
    }
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public Error handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    log.warn(e.getMessage());
    FieldError fieldError = e.getBindingResult().getFieldError();
    if (fieldError != null) {
      var message = String.format(ARGUMENT_NOT_VALID_MSG_PATTERN,
        fieldError.getField(), fieldError.getDefaultMessage());
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

  @ExceptionHandler(FieldsValidationException.class)
  @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
  public Object handleFieldsValidationException(FieldsValidationException e) {
    var errors = e.getValidationResult().getErrors();
    return errors.size() == 1 ? buildError(errors.get(0)) : buildErrors(errors);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Error handleMissingParameterException(MissingServletRequestParameterException e) {
    log.warn(e.getMessage());
    var message = String.format(MISSING_PARAMETER_MSG_PATTERN, e.getParameterName());
    return buildBadRequestResponse(message);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Error handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    log.warn(e.getMessage());
    return buildBadRequestResponse(e.getMessage());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Error handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
    log.warn(e.getMessage());
    var message = String.format(TYPE_MISMATCH_MSG_PATTERN, e.getParameter().getParameterName());
    return buildBadRequestResponse(message);
  }

  @ExceptionHandler(JobProfileNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Error handleMethodArgumentTypeMismatchException(JobProfileNotFoundException e) {
    return buildBadRequestResponse(e.getMessage());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Error handleConstraintViolationException(Exception e) {
    log.warn(e.getMessage());
    var message = String.format(CONSTRAINT_VIOLATION_MSG_PATTERN, e.getMessage());
    return buildError(HttpStatus.BAD_REQUEST, INTERNAL, message);
  }

  @ExceptionHandler(AsyncRequestTimeoutException.class)
  @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
  public Error handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e) {
    return buildError(HttpStatus.REQUEST_TIMEOUT, INTERNAL, "Request timeout occurred");
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Error handleGlobalException(Exception e) {
    log.error("Unexpected error occurred: ", e);
    return buildError(HttpStatus.INTERNAL_SERVER_ERROR, UNKNOWN, e.getMessage());
  }

  private Error buildBadRequestResponse(String message) {
    return buildError(HttpStatus.BAD_REQUEST, INTERNAL, message);
  }
}
