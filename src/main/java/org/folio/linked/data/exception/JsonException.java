package org.folio.linked.data.exception;

import static org.folio.linked.data.model.ErrorCode.JSON_EXCEPTION;

public class JsonException extends BaseLinkedDataException {

  public JsonException(String message) {
    super(message, JSON_EXCEPTION);
  }

  public JsonException(String message, Throwable cause) {
    super(message, cause, JSON_EXCEPTION);
  }
}
