package org.folio.linked.data.test.json;

import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LOCAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.linked.data.util.Constants.DTO_UNKNOWN_SUB_ELEMENT;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.folio.linked.data.domain.dto.EanFieldResponse;
import org.folio.linked.data.domain.dto.InstanceResponseAllOfMap;
import org.folio.linked.data.domain.dto.IsbnFieldResponse;
import org.folio.linked.data.domain.dto.LccnFieldResponse;
import org.folio.linked.data.domain.dto.LocalIdFieldResponse;
import org.folio.linked.data.domain.dto.OtherIdFieldResponse;
import org.folio.linked.data.exception.JsonException;

public class InstanceResponseAllOfMapDeserializer extends JsonDeserializer<InstanceResponseAllOfMap> {

  @Override
  public InstanceResponseAllOfMap deserialize(JsonParser jp, DeserializationContext deserializationContext)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(ID_LCCN.getUri())) {
      return jp.getCodec().treeToValue(node, LccnFieldResponse.class);
    } else if (node.has(ID_ISBN.getUri())) {
      return jp.getCodec().treeToValue(node, IsbnFieldResponse.class);
    } else if (node.has(ID_EAN.getUri())) {
      return jp.getCodec().treeToValue(node, EanFieldResponse.class);
    } else if (node.has(ID_LOCAL.getUri())) {
      return jp.getCodec().treeToValue(node, LocalIdFieldResponse.class);
    } else if (node.has(ID_UNKNOWN.getUri())) {
      return jp.getCodec().treeToValue(node, OtherIdFieldResponse.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(InstanceResponseAllOfMap.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }
}
