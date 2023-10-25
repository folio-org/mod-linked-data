package org.folio.linked.data.configuration.json.deserialization.instance;

import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.linked.data.util.Constants.DTO_UNKNOWN_SUB_ELEMENT;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.folio.linked.data.domain.dto.InstanceAllOfTitleInner;
import org.folio.linked.data.domain.dto.InstanceTitleField;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.exception.JsonException;

public class TitleDeserializer extends JsonDeserializer<InstanceAllOfTitleInner> {

  @Override
  public InstanceAllOfTitleInner deserialize(JsonParser jp, DeserializationContext deserializationContext)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(TITLE.getUri())) {
      return jp.getCodec().treeToValue(node, InstanceTitleField.class);
    } else if (node.has(PARALLEL_TITLE.getUri())) {
      return jp.getCodec().treeToValue(node, ParallelTitleField.class);
    } else if (node.has(VARIANT_TITLE.getUri())) {
      return jp.getCodec().treeToValue(node, VariantTitleField.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(InstanceAllOfTitleInner.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }
}
