package org.folio.linked.data.test.json;

import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.linked.data.util.Constants.DTO_UNKNOWN_SUB_ELEMENT;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.folio.linked.data.domain.dto.ParallelTitleFieldResponse;
import org.folio.linked.data.domain.dto.PrimaryTitleFieldResponse;
import org.folio.linked.data.domain.dto.TitleFieldResponse;
import org.folio.linked.data.domain.dto.VariantTitleFieldResponse;
import org.folio.linked.data.exception.JsonException;

public class TitleFieldResponseDeserializer extends JsonDeserializer<TitleFieldResponse> {

  @Override
  public TitleFieldResponse deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
    throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    if (node.has(TITLE.getUri())) {
      return jsonParser.getCodec().treeToValue(node, PrimaryTitleFieldResponse.class);
    } else if (node.has(PARALLEL_TITLE.getUri())) {
      return jsonParser.getCodec().treeToValue(node, ParallelTitleFieldResponse.class);
    } else if (node.has(VARIANT_TITLE.getUri())) {
      return jsonParser.getCodec().treeToValue(node, VariantTitleFieldResponse.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(TitleFieldResponse.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }
}
