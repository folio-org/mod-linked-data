package org.folio.linked.data.mapper.error;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.List;
import org.folio.linked.data.configuration.ErrorResponseConfig;
import org.folio.linked.data.domain.dto.Error;
import org.folio.linked.data.domain.dto.ErrorResponse;
import org.folio.linked.data.domain.dto.Parameter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@SuppressWarnings("java:S6813")
@Mapper(componentModel = SPRING)
public abstract class GenericBadRequestMapper {

  @Autowired
  private ErrorResponseConfig errorResponseConfig;

  public ResponseEntity<ErrorResponse> errorResponseEntity(Exception exception) {
    return ResponseEntity.status(errorResponseConfig.getGenericBadRequest().status()).body(errorResponse(exception));
  }

  @Mapping(target = "totalRecords", constant = "1")
  @Mapping(target = "errors", source = "exception")
  protected abstract ErrorResponse errorResponse(Exception exception);

  protected List<Error> mapErrors(Exception exception) {
    return List.of(mapErrorFromException(exception));
  }

  protected Error mapErrorFromException(Exception exception) {
    var badRequest = errorResponseConfig.getGenericBadRequest();
    var exceptionName = exception.getClass().getSimpleName();
    var exceptionMessage = exception.getMessage();
    return new Error()
      .code(badRequest.code())
      .message(badRequest.message().formatted(exceptionName, exceptionMessage))
      .parameters(List.of(
        new Parameter().key(badRequest.parameters().getFirst()).value(exceptionName),
        new Parameter().key(badRequest.parameters().get(1)).value(exceptionMessage)
      ));
  }

}
