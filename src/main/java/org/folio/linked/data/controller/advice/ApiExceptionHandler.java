package org.folio.linked.data.controller.advice;

import static java.util.Collections.emptyList;
import static org.folio.linked.data.model.ErrorCode.ALREADY_EXISTS_ERROR;
import static org.folio.linked.data.model.ErrorCode.NOT_FOUND_ERROR;
import static org.folio.linked.data.model.ErrorCode.SERVICE_ERROR;
import static org.folio.linked.data.model.ErrorCode.UNKNOWN_ERROR;
import static org.folio.linked.data.model.ErrorCode.VALIDATION_ERROR;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.folio.linked.data.domain.dto.Error;
import org.folio.linked.data.domain.dto.ErrorResponse;
import org.folio.linked.data.domain.dto.Parameter;
import org.folio.linked.data.exception.AlreadyExistsException;
import org.folio.linked.data.exception.LinkedDataServiceException;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.model.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Log4j2
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(LinkedDataServiceException.class)
  public ResponseEntity<ErrorResponse> handleLinkedDataServiceException(LinkedDataServiceException exception) {
    logException(exception);
    return buildResponseEntity(exception, BAD_REQUEST, exception.getErrorCode());
  }

  @ExceptionHandler(UnsupportedOperationException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedOperationException(UnsupportedOperationException exception) {
    logException(exception);
    return buildResponseEntity(exception, BAD_REQUEST, SERVICE_ERROR);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    var validationErrors = Optional.of(e.getBindingResult()).map(Errors::getAllErrors).orElse(emptyList());
    var errorResponse = new ErrorResponse();
    validationErrors.forEach(error ->
      errorResponse.addErrorsItem(new Error()
        .message(error.getDefaultMessage())
        .code(VALIDATION_ERROR.getValue())
        .type(MethodArgumentNotValidException.class.getSimpleName())
        .addParametersItem(new Parameter()
          .key(((FieldError) error).getField())
          .value(String.valueOf(((FieldError) error).getRejectedValue())))));
    errorResponse.totalRecords(errorResponse.getErrors().size());

    return buildResponseEntity(errorResponse, BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
    logException(exception);
    var errorResponse = new ErrorResponse();
    exception.getConstraintViolations().forEach(constraintViolation ->
      errorResponse.addErrorsItem(new Error()
        .message(String.format("%s %s", constraintViolation.getPropertyPath(), constraintViolation.getMessage()))
        .code(VALIDATION_ERROR.getValue())
        .type(ConstraintViolationException.class.getSimpleName())));
    errorResponse.totalRecords(errorResponse.getErrors().size());

    return buildResponseEntity(errorResponse, BAD_REQUEST);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(ValidationException exception) {
    var errorResponse = buildValidationError(exception, exception.getKey(), exception.getValue());
    return buildResponseEntity(errorResponse, UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
    MethodArgumentTypeMismatchException exception) {
    return buildResponseEntity(exception, BAD_REQUEST, VALIDATION_ERROR);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
    logException(exception);
    return buildResponseEntity(exception, BAD_REQUEST, VALIDATION_ERROR);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException exception) {
    logException(exception);
    return buildResponseEntity(exception, NOT_FOUND, NOT_FOUND_ERROR);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleEntityNotFoundException(NotFoundException exception) {
    log.info("Not found: {}", exception.getMessage());
    return buildResponseEntity(exception, NOT_FOUND, NOT_FOUND_ERROR);
  }

  @ExceptionHandler(AlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleAlreadyExistsException(AlreadyExistsException exception) {
    logException(exception);
    return buildResponseEntity(exception, BAD_REQUEST, ALREADY_EXISTS_ERROR);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
    logException(e);
    return buildResponseEntity(e, BAD_REQUEST, VALIDATION_ERROR);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handlerHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    return Optional.ofNullable(e.getCause())
      .map(Throwable::getCause)
      .filter(IllegalArgumentException.class::isInstance)
      .map(IllegalArgumentException.class::cast)
      .map(this::handleIllegalArgumentException)
      .orElseGet(() -> {
        logException(e);
        return buildResponseEntity(e, UNPROCESSABLE_ENTITY, VALIDATION_ERROR);
      });
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
    MissingServletRequestParameterException exception) {
    logException(exception);
    return buildResponseEntity(exception, BAD_REQUEST, VALIDATION_ERROR);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllOtherExceptions(Exception exception) {
    logException(exception);
    return buildResponseEntity(exception, INTERNAL_SERVER_ERROR, UNKNOWN_ERROR);
  }

  private static ErrorResponse buildValidationError(Exception exception, String key, String value) {
    var error = new Error()
      .type(exception.getClass().getSimpleName())
      .code(VALIDATION_ERROR.getValue())
      .message(exception.getMessage())
      .parameters(List.of(new Parameter().key(key).value(value)));
    return new ErrorResponse().errors(List.of(error)).totalRecords(1);
  }

  private static ResponseEntity<ErrorResponse> buildResponseEntity(Exception e, HttpStatus status, ErrorCode code) {
    var errorResponse = new ErrorResponse()
      .errors(List.of(new Error()
        .message(e.getMessage())
        .type(e.getClass().getSimpleName())
        .code(code.getValue())))
      .totalRecords(1);
    return buildResponseEntity(errorResponse, status);
  }

  private static ResponseEntity<ErrorResponse> buildResponseEntity(ErrorResponse errorResponse, HttpStatus status) {
    return ResponseEntity.status(status).body(errorResponse);
  }

  private static void logException(Exception exception) {
    log.log(Level.WARN, "Handling exception", exception);
  }
}
