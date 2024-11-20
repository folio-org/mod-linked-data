package org.folio.linked.data.exception;

import java.util.Map;
import lombok.Getter;

@Getter
public class RequestProcessingException extends RuntimeException {

  private final int status;
  private final String code;
  private final Map<String, String> parameters;
  private final String message;

  public RequestProcessingException(int status,
                                    String code,
                                    Map<String, String> parameters,
                                    String message) {
    super();
    this.status = status;
    this.code = code;
    this.parameters = parameters;
    this.message = message;
  }

}
