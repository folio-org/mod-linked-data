package org.folio.linked.data.exception;

import org.folio.linked.data.model.ErrorCode;

public class AlreadyExistsException extends BaseLinkedDataException {

  public AlreadyExistsException(String message) {
    super(message, ErrorCode.ALREADY_EXISTS_ERROR);
  }

}
