package org.folio.linked.data.test.json;

import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_IAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.linked.data.test.TestUtil.EMPTY_EXCEPTION_BUILDER;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Map;
import org.folio.linked.data.domain.dto.IanFieldResponse;
import org.folio.linked.data.domain.dto.IdentifierFieldResponse;
import org.folio.linked.data.domain.dto.IsbnFieldResponse;
import org.folio.linked.data.domain.dto.LccnFieldResponse;
import org.folio.linked.data.domain.dto.OtherIdFieldResponse;
import org.folio.linked.data.util.DtoDeserializer;

public class IdentifierFieldResponseDeserializer extends JsonDeserializer<IdentifierFieldResponse> {

  private static final Map<String, Class<? extends IdentifierFieldResponse>> IDENDTITY_MAP = Map.of(
    ID_LCCN.getUri(), LccnFieldResponse.class,
    ID_ISBN.getUri(), IsbnFieldResponse.class,
    ID_IAN.getUri(), IanFieldResponse.class,
    ID_UNKNOWN.getUri(), OtherIdFieldResponse.class
  );
  private final DtoDeserializer<IdentifierFieldResponse> dtoDeserializer =
    new DtoDeserializer<>(IdentifierFieldResponse.class, IDENDTITY_MAP, EMPTY_EXCEPTION_BUILDER);

  @Override
  public IdentifierFieldResponse deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
    return dtoDeserializer.deserialize(jp);
  }

}
