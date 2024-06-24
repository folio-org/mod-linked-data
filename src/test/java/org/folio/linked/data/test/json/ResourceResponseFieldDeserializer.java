package org.folio.linked.data.test.json;

import static org.folio.linked.data.util.Constants.DTO_UNKNOWN_SUB_ELEMENT;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.InstanceResponseField;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseField;
import org.folio.linked.data.domain.dto.WorkResponseField;
import org.folio.linked.data.exception.JsonException;

public class ResourceResponseFieldDeserializer extends JsonDeserializer<ResourceResponseField> {

  @Override
  public ResourceResponseField deserialize(JsonParser jp, DeserializationContext deserializationContext)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(ResourceTypeDictionary.INSTANCE.getUri())) {
      return jp.getCodec().treeToValue(node, InstanceResponseField.class);
    }
    if (node.has(ResourceTypeDictionary.WORK.getUri())) {
      return jp.getCodec().treeToValue(node, WorkResponseField.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(ResourceRequestDto.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }
}
