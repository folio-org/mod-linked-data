package org.folio.linked.data.test.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Map;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.InstanceResponseField;
import org.folio.linked.data.domain.dto.ResourceResponseField;
import org.folio.linked.data.domain.dto.WorkResponseField;
import org.folio.linked.data.util.DtoDeserializer;

public class ResourceResponseFieldDeserializer extends JsonDeserializer<ResourceResponseField> {

  private static final Map<String, Class<? extends ResourceResponseField>> IDENDTITY_MAP = Map.of(
    ResourceTypeDictionary.INSTANCE.getUri(), InstanceResponseField.class,
    ResourceTypeDictionary.WORK.getUri(), WorkResponseField.class
  );
  private final DtoDeserializer<ResourceResponseField> dtoDeserializer = new DtoDeserializer<>();

  @Override
  public ResourceResponseField deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
    return dtoDeserializer.deserialize(jp, IDENDTITY_MAP, ResourceResponseField.class);
  }
}
