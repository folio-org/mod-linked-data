package org.folio.linked.data.configuration.json.deserialization.instance;

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
import org.folio.linked.data.domain.dto.EanField;
import org.folio.linked.data.domain.dto.InstanceAllOfMap;
import org.folio.linked.data.domain.dto.IsbnField;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.domain.dto.LocalIdField;
import org.folio.linked.data.domain.dto.OtherIdField;
import org.folio.linked.data.util.DtoDeserializer;

public class InstanceAllOfMapDeserializer extends JsonDeserializer<InstanceAllOfMap> {

  private static final Map<String, Class<? extends InstanceAllOfMap>> IDENDTITY_MAP = Map.of(
    ID_LCCN.getUri(), LccnField.class,
    ID_ISBN.getUri(), IsbnField.class,
    ID_EAN.getUri(), EanField.class,
    ID_LOCAL.getUri(), LocalIdField.class,
    ID_UNKNOWN.getUri(), OtherIdField.class
  );
  private final DtoDeserializer<InstanceAllOfMap> dtoDeserializer =
    new DtoDeserializer<>(IDENDTITY_MAP, InstanceAllOfMap.class);

  @Override
  public InstanceAllOfMap deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
    return dtoDeserializer.deserialize(jp);
  }
}
