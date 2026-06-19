package org.folio.linked.data.test.json;

import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_IAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;

import java.util.Map;
import org.folio.linked.data.domain.dto.IanFieldResponse;
import org.folio.linked.data.domain.dto.IsbnFieldResponse;
import org.folio.linked.data.domain.dto.LccnFieldResponse;
import org.folio.linked.data.domain.dto.MapResponse;
import org.folio.linked.data.domain.dto.OtherIdFieldResponse;
import org.folio.linked.data.util.DtoDeserializer;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class MapResponseDeserializer extends ValueDeserializer<MapResponse> {

  private static final Map<String, Class<? extends MapResponse>> IDENDTITY_MAP = Map.of(
    ID_LCCN.getUri(), LccnFieldResponse.class,
    ID_ISBN.getUri(), IsbnFieldResponse.class,
    ID_IAN.getUri(), IanFieldResponse.class,
    ID_UNKNOWN.getUri(), OtherIdFieldResponse.class
  );
  private final DtoDeserializer<MapResponse> dtoDeserializer =
    new DtoDeserializer<>(MapResponse.class, IDENDTITY_MAP);

  @Override
  public MapResponse deserialize(JsonParser jp, DeserializationContext dc) {
    return dtoDeserializer.deserialize(jp, dc);
  }

}
