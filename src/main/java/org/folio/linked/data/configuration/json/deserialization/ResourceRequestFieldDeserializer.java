package org.folio.linked.data.configuration.json.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Map;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.ResourceRequestField;
import org.folio.linked.data.domain.dto.WorkField;
import org.folio.linked.data.util.DtoDeserializer;

public class ResourceRequestFieldDeserializer extends JsonDeserializer<ResourceRequestField> {
  private static final Map<String, Class<? extends ResourceRequestField>> IDENDTITY_MAP = Map.of(
    ResourceTypeDictionary.INSTANCE.getUri(), InstanceField.class,
    ResourceTypeDictionary.WORK.getUri(), WorkField.class
  );
  private final DtoDeserializer<ResourceRequestField> dtoDeserializer = new DtoDeserializer<>();

  @Override
  public ResourceRequestField deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
    return dtoDeserializer.deserialize(jp, IDENDTITY_MAP, ResourceRequestField.class);
  }
}
