package org.folio.linked.data.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class JsonMappingException extends RuntimeException {
  private final String dtoClass;
  private final String fieldName;

}
