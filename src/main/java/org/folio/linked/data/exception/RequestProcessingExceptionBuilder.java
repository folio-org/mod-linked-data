package org.folio.linked.data.exception;

import static java.lang.String.format;
import static org.folio.linked.data.util.Constants.LINKED_DATA_STORAGE;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.configuration.ErrorResponseConfig;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestProcessingExceptionBuilder {

  private final ErrorResponseConfig errorResponseConfig;

  public RequestProcessingException alreadyExistsException(String idType, String idValue) {
    var alreadyExistsException = errorResponseConfig.getAlreadyExistsException();
    return new RequestProcessingException(alreadyExistsException.status(),
      alreadyExistsException.code(),
      Map.of(
        alreadyExistsException.parameters().get(0), idType,
        alreadyExistsException.parameters().get(1), idValue
      ),
      format(alreadyExistsException.message(), idType, idValue)
    );
  }

  public RequestProcessingException notFoundLdResourceByInventoryIdException(String id) {
    return notFoundException("Resource", "inventoryId", id, LINKED_DATA_STORAGE);
  }

  public RequestProcessingException notFoundLdResourceByIdException(String resourceType, String id) {
    return notFoundException(resourceType, "id", id, LINKED_DATA_STORAGE);
  }

  public RequestProcessingException notFoundSourceRecordException(String idType, String idValue) {
    return notFoundException("Source Record", idType, idValue, "Source Record storage");
  }

  private RequestProcessingException notFoundException(String entityType,
                                                      String idType,
                                                      String idValue,
                                                      String storage) {
    var notFoundException = errorResponseConfig.getNotFoundException();
    return new RequestProcessingException(notFoundException.status(),
      notFoundException.code(),
      Map.of(
        notFoundException.parameters().get(0), entityType,
        notFoundException.parameters().get(1), idType,
        notFoundException.parameters().get(2), idValue,
        notFoundException.parameters().get(3), storage
      ),
      format(notFoundException.message(), entityType, idType, idValue, storage)
    );
  }
}
