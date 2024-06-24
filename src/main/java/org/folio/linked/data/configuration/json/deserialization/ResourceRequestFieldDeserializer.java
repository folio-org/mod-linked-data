package org.folio.linked.data.configuration.json.deserialization;

import static org.folio.linked.data.util.Constants.DTO_UNKNOWN_SUB_ELEMENT;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.InstanceRequestField;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceRequestField;
import org.folio.linked.data.domain.dto.WorkRequestField;
import org.folio.linked.data.exception.JsonException;

public class ResourceRequestFieldDeserializer extends JsonDeserializer<ResourceRequestField> {

  @Override
  public ResourceRequestField deserialize(JsonParser jp, DeserializationContext deserializationContext)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(ResourceTypeDictionary.INSTANCE.getUri())) {
      return jp.getCodec().treeToValue(node, InstanceRequestField.class);
    }
    if (node.has(ResourceTypeDictionary.WORK.getUri())) {
      return jp.getCodec().treeToValue(node, WorkRequestField.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(ResourceRequestDto.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }
}
