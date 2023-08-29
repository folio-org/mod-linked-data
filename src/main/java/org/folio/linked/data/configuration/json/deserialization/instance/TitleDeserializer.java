package org.folio.linked.data.configuration.json.deserialization.instance;

import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;
import static org.folio.linked.data.util.Constants.DTO_UNKNOWN_SUB_ELEMENT;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.folio.linked.data.domain.dto.Instance2TitleInner;
import org.folio.linked.data.domain.dto.InstanceTitleField;
import org.folio.linked.data.domain.dto.InstanceTitleInner;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.exception.JsonException;

public class TitleDeserializer extends JsonDeserializer<InstanceTitleInner> {

  @Override
  public InstanceTitleInner deserialize(JsonParser jp, DeserializationContext deserializationContext)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(INSTANCE_TITLE)) {
      return jp.getCodec().treeToValue(node, InstanceTitleField.class);
    } else if (node.has(PARALLEL_TITLE)) {
      return jp.getCodec().treeToValue(node, ParallelTitleField.class);
    } else if (node.has(VARIANT_TITLE)) {
      return jp.getCodec().treeToValue(node, VariantTitleField.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(Instance2TitleInner.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }
}
