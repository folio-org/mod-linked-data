package org.folio.linked.data.mapper.error;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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
public abstract class ConstraintViolationExceptionMapper {

  @Autowired
  private ErrorResponseConfig errorResponseConfig;

  public ResponseEntity<ErrorResponse> errorResponseEntity(ConstraintViolationException exception) {
    return ResponseEntity.status(errorResponseConfig.getValidation().status()).body(errorResponse(exception));
  }

  @Mapping(target = "totalRecords", expression = "java(e.getConstraintViolations().size())")
  @Mapping(target = "errors", source = "constraintViolations")
  protected abstract ErrorResponse errorResponse(ConstraintViolationException e);

  @Mapping(target = "code", source = "message")
  @Mapping(target = "parameters", source = "constraintViolation")
  protected abstract Error error(ConstraintViolation<?> constraintViolation);

  protected List<Parameter> parameters(ConstraintViolation<?> constraintViolation) {
    var validation = errorResponseConfig.getValidation();
    return List.of(
      new Parameter().key(validation.parameters().get(0)).value(String.valueOf(constraintViolation.getPropertyPath())),
      new Parameter().key(validation.parameters().get(1)).value(String.valueOf(constraintViolation.getInvalidValue()))
    );
  }

}
