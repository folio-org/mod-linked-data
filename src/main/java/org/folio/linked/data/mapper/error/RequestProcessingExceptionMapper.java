package org.folio.linked.data.mapper.error;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.List;
import java.util.Map;
import org.folio.linked.data.domain.dto.Error;
import org.folio.linked.data.domain.dto.ErrorResponse;
import org.folio.linked.data.domain.dto.Parameter;
import org.folio.linked.data.exception.RequestProcessingException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.http.ResponseEntity;

@Mapper(componentModel = SPRING)
public abstract class RequestProcessingExceptionMapper {

  public ResponseEntity<ErrorResponse> errorResponseEntity(RequestProcessingException exception) {
    return ResponseEntity.status(exception.getStatus())
      .body(errorResponse(exception));
  }

  @Mapping(target = "totalRecords", constant = "1")
  @Mapping(target = "errors", source = "exception")
  protected abstract ErrorResponse errorResponse(RequestProcessingException exception);

  protected List<Error> mapErrors(RequestProcessingException exception) {
    return List.of(mapErrorFromRequestProcessingException(exception));
  }

  protected abstract Error mapErrorFromRequestProcessingException(RequestProcessingException exception);

  protected List<Parameter> mapParameters(Map<String, String> parameters) {
    return parameters.entrySet()
      .stream()
      .map(entry -> new Parameter().key(entry.getKey()).value(entry.getValue()))
      .toList();
  }

}
