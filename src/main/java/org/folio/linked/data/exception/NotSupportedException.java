package org.folio.linked.data.exception;

import org.folio.linked.data.model.ErrorCode;

public class NotSupportedException extends BaseLinkedDataException {

  public NotSupportedException(String message) {
    super(message, ErrorCode.NOT_SUPPORTED);
  }

}
