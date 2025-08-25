package org.folio.linked.data.configuration.json.deserialization.instance;

import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_IAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISSN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Map;
import org.folio.linked.data.domain.dto.IanField;
import org.folio.linked.data.domain.dto.IdentifierField;
import org.folio.linked.data.domain.dto.IsbnField;
import org.folio.linked.data.domain.dto.IssnField;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.domain.dto.OtherIdField;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.util.DtoDeserializer;

public class IdentifierFieldDeserializer extends JsonDeserializer<IdentifierField> {

  private static final Map<String, Class<? extends IdentifierField>> IDENTITY_MAP = Map.of(
    ID_LCCN.getUri(), LccnField.class,
    ID_ISSN.getUri(), IssnField.class,
    ID_ISBN.getUri(), IsbnField.class,
    ID_IAN.getUri(), IanField.class,
    ID_UNKNOWN.getUri(), OtherIdField.class
  );
  private final DtoDeserializer<IdentifierField> dtoDeserializer;

  public IdentifierFieldDeserializer(RequestProcessingExceptionBuilder exceptionBuilder) {
    dtoDeserializer = new DtoDeserializer<>(IdentifierField.class, IDENTITY_MAP, exceptionBuilder);
  }

  @Override
  public IdentifierField deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
    return dtoDeserializer.deserialize(jp);
  }
}
