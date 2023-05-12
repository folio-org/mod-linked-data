package org.folio.linked.data.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  UNKNOWN_ERROR("unknown_error"),
  SERVICE_ERROR("service_error"),
  VALIDATION_ERROR("validation_error"),
  NOT_FOUND_ERROR("not_found_error");

  @JsonValue
  private final String value;
}
