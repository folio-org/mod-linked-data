package org.folio.linked.data.configuration.json.deserialization.instance;

import static org.folio.linked.data.util.BibframeConstants.LCCN;
import static org.folio.linked.data.util.Constants.DTO_UNKNOWN_SUB_ELEMENT;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.folio.linked.data.domain.dto.Instance2TitleInner;
import org.folio.linked.data.domain.dto.InstanceMapInner;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.exception.JsonException;

public class MapDeserializer extends JsonDeserializer<InstanceMapInner> {

  @Override
  public InstanceMapInner deserialize(JsonParser jp, DeserializationContext deserializationContext)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(LCCN)) {
      return jp.getCodec().treeToValue(node, LccnField.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(Instance2TitleInner.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }
}
