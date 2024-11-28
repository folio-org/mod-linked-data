package org.folio.linked.data.controller.advice;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.linked.data.test.TestUtil.emptyRequestProcessingException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.folio.linked.data.domain.dto.ErrorResponse;
import org.folio.linked.data.mapper.error.ConstraintViolationExceptionMapper;
import org.folio.linked.data.mapper.error.EntityNotFoundExceptionMapper;
import org.folio.linked.data.mapper.error.GenericBadRequestMapper;
import org.folio.linked.data.mapper.error.GenericServerExceptionMapper;
import org.folio.linked.data.mapper.error.MethodArgumentNotValidExceptionMapper;
import org.folio.linked.data.mapper.error.RequestProcessingExceptionMapper;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ApiExceptionHandlerTest {

  @InjectMocks
  private ApiExceptionHandler apiExceptionHandler;

  @Mock
  private GenericBadRequestMapper genericBadRequestMapper;
  @Mock
  private GenericServerExceptionMapper genericServerExceptionMapper;
  @Mock
  private EntityNotFoundExceptionMapper entityNotFoundExceptionMapper;
  @Mock
  private RequestProcessingExceptionMapper requestProcessingExceptionMapper;
  @Mock
  private ConstraintViolationExceptionMapper constraintViolationExceptionMapper;
  @Mock
  private MethodArgumentNotValidExceptionMapper methodArgumentNotValidExceptionMapper;

  @Test
  void handleRequestProcessingException_shouldReturnCorrectResult() {
    // given
    var exception = emptyRequestProcessingException();
    var expectedResult = new ResponseEntity<ErrorResponse>(HttpStatusCode.valueOf(400));
    when(requestProcessingExceptionMapper.errorResponseEntity(exception)).thenReturn(expectedResult);

    // when
    var result = apiExceptionHandler.handleRequestProcessingException(exception);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void handleEntityNotFoundException_shouldReturnCorrectResult() {
    // given
    var exception = new EntityNotFoundException();
    var expectedResult = new ResponseEntity<ErrorResponse>(HttpStatusCode.valueOf(400));
    when(entityNotFoundExceptionMapper.errorResponseEntity(exception)).thenReturn(expectedResult);

    // when
    var result = apiExceptionHandler.handleEntityNotFoundException(exception);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void handleUnsupportedOperationException_shouldReturnCorrectResult() {
    // given
    var exception = new UnsupportedOperationException();
    var expectedResult = new ResponseEntity<ErrorResponse>(HttpStatusCode.valueOf(400));
    when(genericBadRequestMapper.errorResponseEntity(exception)).thenReturn(expectedResult);

    // when
    var result = apiExceptionHandler.handleUnsupportedOperationException(exception);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void handleMethodArgumentNotValidException_shouldReturnCorrectResult() {
    // given
    var exception = mock(MethodArgumentNotValidException.class);
    var expectedResult = new ResponseEntity<ErrorResponse>(HttpStatusCode.valueOf(400));
    when(methodArgumentNotValidExceptionMapper.errorResponseEntity(exception)).thenReturn(expectedResult);

    // when
    var result = apiExceptionHandler.handleMethodArgumentNotValidException(exception);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void handleConstraintViolation_shouldReturnCorrectResult() {
    // given
    var exception = mock(ConstraintViolationException.class);
    var expectedResult = new ResponseEntity<ErrorResponse>(HttpStatusCode.valueOf(400));
    when(constraintViolationExceptionMapper.errorResponseEntity(exception)).thenReturn(expectedResult);

    // when
    var result = apiExceptionHandler.handleConstraintViolation(exception);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void handleMethodArgumentTypeMismatch_shouldReturnCorrectResult() {
    // given
    var exception = mock(MethodArgumentTypeMismatchException.class);
    var expectedResult = new ResponseEntity<ErrorResponse>(HttpStatusCode.valueOf(400));
    when(genericBadRequestMapper.errorResponseEntity(exception)).thenReturn(expectedResult);

    // when
    var result = apiExceptionHandler.handleMethodArgumentTypeMismatch(exception);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void handleHttpMediaTypeNotSupportedException_shouldReturnCorrectResult() {
    // given
    var exception = mock(HttpMediaTypeNotSupportedException.class);
    var expectedResult = new ResponseEntity<ErrorResponse>(HttpStatusCode.valueOf(400));
    when(genericBadRequestMapper.errorResponseEntity(exception)).thenReturn(expectedResult);

    // when
    var result = apiExceptionHandler.handleHttpMediaTypeNotSupportedException(exception);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void handlerHttpMessageNotReadableException_shouldReturnCorrectResult() {
    // given
    var exception = mock(HttpMessageNotReadableException.class);
    var expectedResult = new ResponseEntity<ErrorResponse>(HttpStatusCode.valueOf(400));
    when(genericBadRequestMapper.errorResponseEntity(exception)).thenReturn(expectedResult);

    // when
    var result = apiExceptionHandler.handlerHttpMessageNotReadableException(exception);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void handleMissingServletRequestParameterException_shouldReturnCorrectResult() {
    // given
    var exception = mock(MissingServletRequestParameterException.class);
    var expectedResult = new ResponseEntity<ErrorResponse>(HttpStatusCode.valueOf(400));
    when(genericBadRequestMapper.errorResponseEntity(exception)).thenReturn(expectedResult);

    // when
    var result = apiExceptionHandler.handleMissingServletRequestParameterException(exception);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void handleAllOtherExceptions_shouldReturnCorrectResult() {
    // given
    var exception = new RuntimeException();
    var expectedResult = new ResponseEntity<ErrorResponse>(HttpStatusCode.valueOf(400));
    when(genericServerExceptionMapper.errorResponseEntity(exception)).thenReturn(expectedResult);

    // when
    var result = apiExceptionHandler.handleAllOtherExceptions(exception);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }
}
