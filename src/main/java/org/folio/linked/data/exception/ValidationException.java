package org.folio.linked.data.exception;

import lombok.Getter;
import org.folio.linked.data.model.ErrorCode;

@Getter
public class ValidationException extends BaseLinkedDataException {

  private final String key;
  private final String value;

  /**
   * Creates {@link ValidationException} object for given message, key and value.
   *
   * @param message - validation error message
   * @param key     - validation key as field or parameter name
   * @param value   - invalid parameter value
   */
  public ValidationException(String message, String key, String value) {
    super(message, ErrorCode.VALIDATION_ERROR);

    this.key = key;
    this.value = value;
  }
}
