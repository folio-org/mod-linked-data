package org.folio.linked.data.mapper.error;


import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.genericError;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashSet;
import org.folio.linked.data.configuration.ErrorResponseConfig;
import org.folio.linked.data.domain.dto.Parameter;
import org.folio.spring.testing.type.UnitTest;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.util.ReflectionTestUtils;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ConstraintViolationExceptionMapperTest {

  private ConstraintViolationExceptionMapper mapper;
  @Mock
  private ErrorResponseConfig errorResponseConfig;

  @BeforeEach
  void setup() {
    if (isNull(mapper)) {
      mapper = new ConstraintViolationExceptionMapperImpl();
      ReflectionTestUtils.setField(mapper, "errorResponseConfig", errorResponseConfig);
    }
  }

  @Test
  void shouldReturnCorrectErrorResponse() {
    // given
    var violation1 = mock(ConstraintViolation.class);
    var violation1PropertyPath = "";
    when(violation1.getPropertyPath()).thenReturn(PathImpl.createPathFromString(violation1PropertyPath));
    var violation1Value = "violation1Value";
    when(violation1.getInvalidValue()).thenReturn(violation1Value);
    var violation1Message = "violation1Message";
    when(violation1.getMessage()).thenReturn(violation1Message);

    var violation2 = mock(ConstraintViolation.class);
    var violation2PropertyPath = "somePath";
    when(violation2.getPropertyPath()).thenReturn(PathImpl.createPathFromString(violation2PropertyPath));
    var violation2Value = "violation2Value";
    when(violation2.getInvalidValue()).thenReturn(violation2Value);
    var violation2Message = "violation2Message";
    when(violation2.getMessage()).thenReturn(violation2Message);
    var violations = new LinkedHashSet<ConstraintViolation<?>>();
    violations.add(violation1);
    violations.add(violation2);

    var exception = mock(ConstraintViolationException.class);
    when(exception.getConstraintViolations()).thenReturn(violations);
    var genericError = genericError(2);
    when(errorResponseConfig.getValidation()).thenReturn(genericError);

    // when
    var result = mapper.errorResponseEntity(exception);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(genericError.status()));
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().getTotalRecords()).isEqualTo(2);
    assertThat(result.getBody().getErrors()).hasSize(2);
    var error1 = result.getBody().getErrors().getFirst();
    assertThat(error1.getCode()).isEqualTo(violation1Message);
    assertThat(error1.getMessage()).isEqualTo(violation1Message);
    assertThat(error1.getParameters())
      .hasSize(2)
      .contains(new Parameter().key(genericError.parameters().getFirst()).value(violation1PropertyPath))
      .contains(new Parameter().key(genericError.parameters().get(1)).value(violation1Value));
    var error2 = result.getBody().getErrors().get(1);
    assertThat(error2.getCode()).isEqualTo(violation2Message);
    assertThat(error2.getMessage()).isEqualTo(violation2Message);
    assertThat(error2.getParameters())
      .hasSize(2)
      .contains(new Parameter().key(genericError.parameters().getFirst()).value(violation2PropertyPath))
      .contains(new Parameter().key(genericError.parameters().get(1)).value(violation2Value));
  }

}
