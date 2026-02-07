package org.folio.linked.data.exception;

import static org.folio.linked.data.util.Constants.LINKED_DATA_STORAGE;

import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.configuration.ErrorResponseConfig;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestProcessingExceptionBuilder {

  private final ErrorResponseConfig errorResponseConfig;

  public RequestProcessingException alreadyExistsException(String idType, String idValue) {
    var alreadyExistsError = errorResponseConfig.getAlreadyExists();
    return requestProcessingException(alreadyExistsError, idType, idValue);
  }

  public RequestProcessingException mappingException(String dtoClass, String dtoValue) {
    var mappingError = errorResponseConfig.getMapping();
    return requestProcessingException(mappingError, dtoClass, dtoValue);
  }

  public RequestProcessingException notSupportedException(String type, String process) {
    var notSupportedResourceTypeError = errorResponseConfig.getNotSupported();
    return requestProcessingException(notSupportedResourceTypeError, type, process);
  }

  public RequestProcessingException requiredException(String object) {
    var nullIdError = errorResponseConfig.getRequired();
    return requestProcessingException(nullIdError, object);
  }

  public RequestProcessingException notFoundLdResourceByInventoryIdException(String id) {
    var notFoundError = errorResponseConfig.getNotFound();
    return requestProcessingException(notFoundError, "Resource", "inventoryId", id, LINKED_DATA_STORAGE);
  }

  public RequestProcessingException notFoundLdResourceByIdException(String resourceType, String id) {
    var notFoundError = errorResponseConfig.getNotFound();
    return requestProcessingException(notFoundError, resourceType, "id", id, LINKED_DATA_STORAGE);
  }

  public RequestProcessingException notFoundSourceRecordException(String idType, String idValue) {
    var notFoundError = errorResponseConfig.getNotFound();
    return requestProcessingException(notFoundError, "Source Record", idType, idValue, "Source Record storage");
  }

  public RequestProcessingException notFoundResourceByUriException(String rdfUri) {
    var notFoundError = errorResponseConfig.getNotFound();
    return requestProcessingException(notFoundError, "Resource", "URI", rdfUri, "remote source");
  }

  public RequestProcessingException failedDependencyException(String message, String reason) {
    return requestProcessingException(errorResponseConfig.getFailedDependency(), message, reason);
  }

  public RequestProcessingException badRequestException(String message, String reason) {
    return requestProcessingException(errorResponseConfig.getGenericBadRequest(), message, reason);
  }

  private RequestProcessingException requestProcessingException(ErrorResponseConfig.Error error, String... arguments) {
    var parameters = new HashMap<String, String>();
    for (int i = 0; i < arguments.length; i++) {
      parameters.put(error.parameters().get(i), arguments[i]);
    }
    return new RequestProcessingException(error.status(),
      error.code(),
      parameters,
      error.message().formatted((Object[]) arguments)
    );
  }
}
