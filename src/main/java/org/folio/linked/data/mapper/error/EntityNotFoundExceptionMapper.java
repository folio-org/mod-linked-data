package org.folio.linked.data.mapper.error;

import static org.folio.linked.data.util.Constants.LINKED_DATA_STORAGE;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.regex.Pattern;
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
public abstract class EntityNotFoundExceptionMapper {

  private static final Pattern ID_PATTERN = Pattern.compile("id (\\S+)");
  @Autowired
  private ErrorResponseConfig errorResponseConfig;

  public ResponseEntity<ErrorResponse> errorResponseEntity(EntityNotFoundException exception) {
    return ResponseEntity.status(errorResponseConfig.getNotFound().status()).body(errorResponse(exception));
  }

  @Mapping(target = "totalRecords", constant = "1")
  @Mapping(target = "errors", source = "exception")
  protected abstract ErrorResponse errorResponse(EntityNotFoundException exception);

  protected List<Error> mapErrors(EntityNotFoundException exception) {
    return List.of(mapErrorFromEntityNotFoundException(exception));
  }

  protected Error mapErrorFromEntityNotFoundException(EntityNotFoundException exception) {
    var notFound = errorResponseConfig.getNotFound();
    var id = parseId(exception.getMessage());
    return new Error()
      .code(notFound.code())
      .message(notFound.message().formatted("Entity", "id", id, LINKED_DATA_STORAGE))
      .parameters(List.of(
        new Parameter().key(notFound.parameters().getFirst()).value("Entity"),
        new Parameter().key(notFound.parameters().get(1)).value("id"),
        new Parameter().key(notFound.parameters().get(2)).value(id),
        new Parameter().key(notFound.parameters().get(3)).value(LINKED_DATA_STORAGE)
      ));
  }

  private String parseId(String message) {
    var matcher = ID_PATTERN.matcher(message);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return "?";
  }
}
