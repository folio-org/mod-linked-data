package org.folio.linked.data.configuration.json.deserialization.instance;

import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE_URL;
import static org.folio.linked.data.util.Constants.DTO_UNKNOWN_SUB_ELEMENT;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.folio.linked.data.domain.dto.Instance2TitleInner;
import org.folio.linked.data.domain.dto.InstanceTitleField2;
import org.folio.linked.data.domain.dto.ParallelTitleField2;
import org.folio.linked.data.domain.dto.VariantTitleField2;
import org.folio.linked.data.exception.JsonException;

public class Title2Deserializer extends JsonDeserializer<Instance2TitleInner> {

  @Override
  public Instance2TitleInner deserialize(JsonParser jp, DeserializationContext deserializationContext)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(INSTANCE_TITLE_URL)) {
      return jp.getCodec().treeToValue(node, InstanceTitleField2.class);
    } else if (node.has(PARALLEL_TITLE_URL)) {
      return jp.getCodec().treeToValue(node, ParallelTitleField2.class);
    } else if (node.has(VARIANT_TITLE_URL)) {
      return jp.getCodec().treeToValue(node, VariantTitleField2.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(Instance2TitleInner.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }
}
