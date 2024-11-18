package org.folio.linked.data.controller.advice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.folio.linked.data.domain.dto.ErrorResponse;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.mapper.error.ConstraintViolationExceptionMapper;
import org.folio.linked.data.mapper.error.EntityNotFoundExceptionMapper;
import org.folio.linked.data.mapper.error.GenericBadRequestMapper;
import org.folio.linked.data.mapper.error.GenericServerExceptionMapper;
import org.folio.linked.data.mapper.error.MethodArgumentNotValidExceptionMapper;
import org.folio.linked.data.mapper.error.RequestProcessingExceptionMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Log4j2
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

  private final GenericBadRequestMapper genericBadRequestMapper;
  private final GenericServerExceptionMapper genericServerExceptionMapper;
  private final EntityNotFoundExceptionMapper entityNotFoundExceptionMapper;
  private final RequestProcessingExceptionMapper requestProcessingExceptionMapper;
  private final ConstraintViolationExceptionMapper constraintViolationExceptionMapper;
  private final MethodArgumentNotValidExceptionMapper methodArgumentNotValidExceptionMapper;

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException exception) {
    logException(exception);
    return entityNotFoundExceptionMapper.errorResponseEntity(exception);
  }

  @ExceptionHandler(UnsupportedOperationException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedOperationException(UnsupportedOperationException exception) {
    logException(exception);
    return genericBadRequestMapper.errorResponseEntity(exception);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    logException(e);
    return methodArgumentNotValidExceptionMapper.errorResponseEntity(e);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
    logException(exception);
    return constraintViolationExceptionMapper.errorResponseEntity(exception);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
    logException(e);
    return genericBadRequestMapper.errorResponseEntity(e);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
    logException(exception);
    return genericBadRequestMapper.errorResponseEntity(exception);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
    logException(e);
    return genericBadRequestMapper.errorResponseEntity(e);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handlerHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    logException(e);
    return genericBadRequestMapper.errorResponseEntity(e);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
    MissingServletRequestParameterException e) {
    logException(e);
    return genericBadRequestMapper.errorResponseEntity(e);
  }

  @ExceptionHandler(RequestProcessingException.class)
  public ResponseEntity<ErrorResponse> handleRequestProcessingException(RequestProcessingException exception) {
    return requestProcessingExceptionMapper.errorResponseEntity(exception);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllOtherExceptions(Exception exception) {
    logException(exception);
    return genericServerExceptionMapper.errorResponseEntity(exception);
  }

  private static ResponseEntity<ErrorResponse> buildResponseEntity(ErrorResponse errorResponse, HttpStatus status) {
    return ResponseEntity.status(status).body(errorResponse);
  }

  private static void logException(Exception exception) {
    log.log(Level.WARN, "Handling exception", exception);
  }
}
