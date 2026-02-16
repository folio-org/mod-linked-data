package org.folio.linked.data.test.json;

import java.util.Map;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.InstanceResponseField;
import org.folio.linked.data.domain.dto.ResourceResponseField;
import org.folio.linked.data.domain.dto.WorkResponseField;
import org.folio.linked.data.util.DtoDeserializer;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class ResourceResponseFieldDeserializer extends ValueDeserializer<ResourceResponseField> {

  private static final Map<String, Class<? extends ResourceResponseField>> IDENDTITY_MAP = Map.of(
    ResourceTypeDictionary.INSTANCE.getUri(), InstanceResponseField.class,
    ResourceTypeDictionary.WORK.getUri(), WorkResponseField.class
  );
  private final DtoDeserializer<ResourceResponseField> dtoDeserializer =
    new DtoDeserializer<>(ResourceResponseField.class, IDENDTITY_MAP);

  @Override
  public ResourceResponseField deserialize(JsonParser jp, DeserializationContext dc) {
    return dtoDeserializer.deserialize(jp, dc);
  }
}
