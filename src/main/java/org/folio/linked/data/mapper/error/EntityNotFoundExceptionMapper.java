package org.folio.linked.data.mapper.error;

import static org.folio.linked.data.util.Constants.LINKED_DATA_STORAGE;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.regex.Pattern;
import org.folio.linked.data.configuration.ErrorResponseConfig;
import org.folio.linked.data.domain.dto.Error;
import org.folio.linked.data.domain.dto.ErrorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@SuppressWarnings("java:S6813")
@Mapper(componentModel = SPRING)
public abstract class EntityNotFoundExceptionMapper {

  @Autowired
  private ErrorResponseConfig errorResponseConfig;

  public ResponseEntity<ErrorResponse> errorResponseEntity(EntityNotFoundException exception) {
    return ResponseEntity.status(errorResponseConfig.getNotFoundException().status())
      .body(errorResponse(exception));
  }

  @Mapping(target = "totalRecords", constant = "1")
  @Mapping(target = "errors", source = "exception")
  protected abstract ErrorResponse errorResponse(EntityNotFoundException exception);

  protected List<Error> mapErrors(EntityNotFoundException exception) {
    return List.of(mapErrorFromEntityNotFoundException(exception));
  }

  protected Error mapErrorFromEntityNotFoundException(EntityNotFoundException exception) {
    var notFoundExceptionConfig = errorResponseConfig.getNotFoundException();
    var id = parseId(exception.getMessage());
    return new Error()
      .code(notFoundExceptionConfig.code())
      .message(String.format(notFoundExceptionConfig.message(), "Entity", "id", id, LINKED_DATA_STORAGE));
  }

  private String parseId(String message) {
    var pattern = Pattern.compile("id (\\S+)");
    var matcher = pattern.matcher(message);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return "?";
  }
}
