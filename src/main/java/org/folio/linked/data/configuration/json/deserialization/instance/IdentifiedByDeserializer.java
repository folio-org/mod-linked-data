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
import org.folio.linked.data.domain.dto.EanField;
import org.folio.linked.data.domain.dto.InstanceIdentifiedByInner;
import org.folio.linked.data.domain.dto.IsbnField;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.domain.dto.LocalIdentifierField;
import org.folio.linked.data.domain.dto.OtherIdentifierField;
import org.folio.linked.data.exception.JsonException;

public class IdentifiedByDeserializer extends JsonDeserializer<InstanceIdentifiedByInner> {

  @Override
  public InstanceIdentifiedByInner deserialize(JsonParser jp, DeserializationContext deserializationContext)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(IDENTIFIERS_EAN_URL)) {
      return jp.getCodec().treeToValue(node, EanField.class);
    } else if (node.has(IDENTIFIERS_ISBN_URL)) {
      return jp.getCodec().treeToValue(node, IsbnField.class);
    } else if (node.has(IDENTIFIERS_LCCN_URL)) {
      return jp.getCodec().treeToValue(node, LccnField.class);
    } else if (node.has(IDENTIFIERS_LOCAL_URL)) {
      return jp.getCodec().treeToValue(node, LocalIdentifierField.class);
    } else if (node.has(IDENTIFIERS_OTHER_URL)) {
      return jp.getCodec().treeToValue(node, OtherIdentifierField.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(InstanceIdentifiedByInner.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }
}
