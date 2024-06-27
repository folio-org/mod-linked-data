package org.folio.linked.data.test.json;

import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LOCAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Map;
import org.folio.linked.data.domain.dto.EanFieldResponse;
import org.folio.linked.data.domain.dto.InstanceResponseAllOfMap;
import org.folio.linked.data.domain.dto.IsbnFieldResponse;
import org.folio.linked.data.domain.dto.LccnFieldResponse;
import org.folio.linked.data.domain.dto.LocalIdFieldResponse;
import org.folio.linked.data.domain.dto.OtherIdFieldResponse;
import org.folio.linked.data.util.DtoDeserializer;

public class InstanceResponseAllOfMapDeserializer extends JsonDeserializer<InstanceResponseAllOfMap> {

  private static final Map<String, Class<? extends InstanceResponseAllOfMap>> IDENDTITY_MAP = Map.of(
    ID_LCCN.getUri(), LccnFieldResponse.class,
    ID_ISBN.getUri(), IsbnFieldResponse.class,
    ID_EAN.getUri(), EanFieldResponse.class,
    ID_LOCAL.getUri(), LocalIdFieldResponse.class,
    ID_UNKNOWN.getUri(), OtherIdFieldResponse.class
  );
  private final DtoDeserializer<InstanceResponseAllOfMap> dtoDeserializer =
    new DtoDeserializer<>(IDENDTITY_MAP, InstanceResponseAllOfMap.class);

  @Override
  public InstanceResponseAllOfMap deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
    return dtoDeserializer.deserialize(jp);
  }

}
