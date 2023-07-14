package org.folio.linked.data.exception;

import lombok.Getter;
import org.folio.linked.data.model.ErrorCode;

@Getter
public class ValidationException extends BaseLinkedDataException {

  private final String key;
  private final String value;

  public ValidationException(String message, String key, String value) {
    super(message, ErrorCode.VALIDATION_ERROR);
    this.key = key;
    this.value = value;
  }

  public ValidationException(String key, String value) {
    super(null, ErrorCode.VALIDATION_ERROR);
    this.key = key;
    this.value = value;
  }
}
