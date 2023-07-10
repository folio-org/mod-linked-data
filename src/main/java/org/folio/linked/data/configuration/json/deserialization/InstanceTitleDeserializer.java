package org.folio.linked.data.configuration.json.deserialization;

import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE_URL;
import static org.folio.linked.data.util.Constants.DTO_UNKNOWN_IMPLEMENTATION;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.folio.linked.data.domain.dto.InstanceTitleField;
import org.folio.linked.data.domain.dto.InstanceTitleInner;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.exception.JsonException;

public class InstanceTitleDeserializer extends JsonDeserializer<InstanceTitleInner> {

  @Override
  public InstanceTitleInner deserialize(JsonParser jp, DeserializationContext deserializationContext)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(INSTANCE_TITLE_URL)) {
      return jp.getCodec().treeToValue(node, InstanceTitleField.class);
    } else if (node.has(PARALLEL_TITLE_URL)) {
      return jp.getCodec().treeToValue(node, ParallelTitleField.class);
    } else if (node.has(VARIANT_TITLE_URL)) {
      return jp.getCodec().treeToValue(node, VariantTitleField.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(InstanceTitleInner.class.getSimpleName() + DTO_UNKNOWN_IMPLEMENTATION + field);
  }
}
