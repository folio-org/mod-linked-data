package org.folio.linked.data.configuration.json.deserialization;

import java.util.Map;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.HubField;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.ResourceRequestField;
import org.folio.linked.data.domain.dto.WorkField;
import org.folio.linked.data.util.DtoDeserializer;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class ResourceRequestFieldDeserializer extends ValueDeserializer<ResourceRequestField> {

  private static final Map<String, Class<? extends ResourceRequestField>> IDENDTITY_MAP = Map.of(
    ResourceTypeDictionary.INSTANCE.getUri(), InstanceField.class,
    ResourceTypeDictionary.WORK.getUri(), WorkField.class,
    ResourceTypeDictionary.HUB.getUri(), HubField.class
  );
  private final DtoDeserializer<ResourceRequestField> dtoDeserializer;

  public ResourceRequestFieldDeserializer() {
    dtoDeserializer = new DtoDeserializer<>(ResourceRequestField.class, IDENDTITY_MAP);
  }

  @Override
  public ResourceRequestField deserialize(JsonParser jp, DeserializationContext dc) {
    return dtoDeserializer.deserialize(jp, dc);
  }
}
