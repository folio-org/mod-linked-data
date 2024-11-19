package org.folio.linked.data.mapper.error;


import static java.lang.String.*;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.genericError;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Constants.LINKED_DATA_STORAGE;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
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
public class EntityNotFoundExceptionMapperTest {

  private EntityNotFoundExceptionMapper mapper;
  @Mock
  private ErrorResponseConfig errorResponseConfig;

  @BeforeEach
  void setup() {
    if (isNull(mapper)) {
      mapper = new EntityNotFoundExceptionMapperImpl();
      ReflectionTestUtils.setField(mapper, "errorResponseConfig", errorResponseConfig);
    }
  }

  @Test
  void shouldReturnCorrectErrorResponse() {
    // given
    var id = randomLong();
    var message = "Unable to find org.folio.linked.data.model.entity.Resource with id " + id;
    var exception = new EntityNotFoundException(message);
    var genericError = genericError(4);
    when(errorResponseConfig.getNotFound()).thenReturn(genericError);

    // when
    var result = mapper.errorResponseEntity(exception);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(genericError.status()));
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().getTotalRecords()).isEqualTo(1);
    assertThat(result.getBody().getErrors()).hasSize(1);
    var error = result.getBody().getErrors().get(0);
    assertThat(error.getCode()).isEqualTo(genericError.code());
    assertThat(error.getMessage()).isEqualTo(format(genericError.message(), "Entity", "id", id, LINKED_DATA_STORAGE));
    assertThat(error.getParameters())
      .hasSize(4)
      .contains(new Parameter().key(genericError.parameters().get(0)).value("Entity"))
      .contains(new Parameter().key(genericError.parameters().get(1)).value("id"))
      .contains(new Parameter().key(genericError.parameters().get(2)).value(id.toString()))
      .contains(new Parameter().key(genericError.parameters().get(3)).value(LINKED_DATA_STORAGE));
  }

}
