package org.folio.linked.data.mapper.error;

import static java.util.Collections.emptyList;
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
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@SuppressWarnings("java:S6813")
@Mapper(componentModel = SPRING)
public abstract class MethodArgumentNotValidExceptionMapper {

  @Autowired
  private ErrorResponseConfig errorResponseConfig;

  public ResponseEntity<ErrorResponse> errorResponseEntity(MethodArgumentNotValidException exception) {
    return ResponseEntity.status(errorResponseConfig.getValidation().status()).body(errorResponse(exception));
  }

  @Mapping(target = "totalRecords", source = "errorCount")
  @Mapping(target = "errors", source = "allErrors")
  protected abstract ErrorResponse errorResponse(MethodArgumentNotValidException e);

  @Mapping(target = "code", source = "defaultMessage")
  @Mapping(target = "parameters", source = "objectError")
  protected abstract Error error(ObjectError objectError);

  protected List<Parameter> parameters(ObjectError error) {
    var validation = errorResponseConfig.getValidation();
    if (error instanceof FieldError fieldError) {
      return List.of(
        new Parameter().key(validation.parameters().getFirst()).value(fieldError.getField()),
        new Parameter().key(validation.parameters().get(1)).value(String.valueOf(fieldError.getRejectedValue()))
      );
    } else {
      return emptyList();
    }
  }

}
