package org.folio.linked.data.exception;

import org.folio.linked.data.model.ErrorCode;
/**
 * Thrown to indicate a service error that was occurred during operation in LinkedDataService.
 */

public class LinkedDataServiceException extends BaseLinkedDataException {

  /**
   * Creates exception instance from given message.
   *
   * @param message exception message as {@link String} object
   */
  public LinkedDataServiceException(String message) {
    super(message, ErrorCode.SERVICE_ERROR);
  }

  /**
   * Creates exception instance from given message and Throwable.
   *
   * @param message   exception message as{@link String} object
   * @param throwable exception cause as {@link Throwable} object
   */
  public LinkedDataServiceException(String message, Throwable throwable) {
    super(message, throwable, ErrorCode.SERVICE_ERROR);
  }
}
