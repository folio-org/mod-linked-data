package org.folio.linked.data.configuration.json.deserialization.instance;

import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_EAN_URL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_ISBN_URL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LCCN_URL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LOCAL_URL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_OTHER_URL;
import static org.folio.linked.data.util.Constants.DTO_UNKNOWN_SUB_ELEMENT;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.folio.linked.data.domain.dto.EanField2;
import org.folio.linked.data.domain.dto.Instance2IdentifiedByInner;
import org.folio.linked.data.domain.dto.IsbnField2;
import org.folio.linked.data.domain.dto.LccnField2;
import org.folio.linked.data.domain.dto.LocalIdentifierField2;
import org.folio.linked.data.domain.dto.OtherIdentifierField2;
import org.folio.linked.data.exception.JsonException;

public class IdentifiedBy2Deserializer extends JsonDeserializer<Instance2IdentifiedByInner> {

  @Override
  public Instance2IdentifiedByInner deserialize(JsonParser jp, DeserializationContext deserializationContext)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(IDENTIFIERS_EAN_URL)) {
      return jp.getCodec().treeToValue(node, EanField2.class);
    } else if (node.has(IDENTIFIERS_ISBN_URL)) {
      return jp.getCodec().treeToValue(node, IsbnField2.class);
    } else if (node.has(IDENTIFIERS_LCCN_URL)) {
      return jp.getCodec().treeToValue(node, LccnField2.class);
    } else if (node.has(IDENTIFIERS_LOCAL_URL)) {
      return jp.getCodec().treeToValue(node, LocalIdentifierField2.class);
    } else if (node.has(IDENTIFIERS_OTHER_URL)) {
      return jp.getCodec().treeToValue(node, OtherIdentifierField2.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(Instance2IdentifiedByInner.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }
}
