package org.folio.linked.data.mapper.error;


import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.genericError;
import static org.mockito.Mockito.when;

import org.folio.linked.data.configuration.ErrorResponseConfig;
import org.folio.linked.data.domain.dto.Parameter;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.util.ReflectionTestUtils;

@UnitTest
@ExtendWith(MockitoExtension.class)
class GenericServerExceptionMapperTest {

  private GenericServerExceptionMapper mapper;
  @Mock
  private ErrorResponseConfig errorResponseConfig;

  @BeforeEach
  void setup() {
    if (isNull(mapper)) {
      mapper = new GenericServerExceptionMapperImpl();
      ReflectionTestUtils.setField(mapper, "errorResponseConfig", errorResponseConfig);
    }
  }

  @Test
  void shouldReturnCorrectErrorResponse() {
    // given
    var message = "Something is null...";
    var exception = new NullPointerException(message);
    var genericError = genericError(2);
    when(errorResponseConfig.getGenericServer()).thenReturn(genericError);

    // when
    var result = mapper.errorResponseEntity(exception);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(genericError.status()));
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().getTotalRecords()).isEqualTo(1);
    assertThat(result.getBody().getErrors()).hasSize(1);
    var error = result.getBody().getErrors().get(0);
    assertThat(error.getCode()).isEqualTo(genericError.code());
    assertThat(error.getMessage())
      .isEqualTo(format(genericError.message(), exception.getClass().getSimpleName(), message));
    assertThat(error.getParameters())
      .hasSize(2)
      .contains(new Parameter().key(genericError.parameters().get(0)).value(exception.getClass().getSimpleName()))
      .contains(new Parameter().key(genericError.parameters().get(1)).value(message));
  }

}
