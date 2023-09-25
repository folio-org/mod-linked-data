package org.folio.linked.data.configuration.json.deserialization.instance;

import static org.folio.linked.data.util.BibframeConstants.EAN;
import static org.folio.linked.data.util.BibframeConstants.ISBN;
import static org.folio.linked.data.util.BibframeConstants.LCCN;
import static org.folio.linked.data.util.BibframeConstants.LOCAL_ID;
import static org.folio.linked.data.util.BibframeConstants.OTHER_ID;
import static org.folio.linked.data.util.Constants.DTO_UNKNOWN_SUB_ELEMENT;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.folio.linked.data.domain.dto.EanField;
import org.folio.linked.data.domain.dto.InstanceAllOfMapInner;
import org.folio.linked.data.domain.dto.IsbnField;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.domain.dto.LocalIdField;
import org.folio.linked.data.domain.dto.OtherIdField;
import org.folio.linked.data.exception.JsonException;

public class MapDeserializer extends JsonDeserializer<InstanceAllOfMapInner> {

  @Override
  public InstanceAllOfMapInner deserialize(JsonParser jp, DeserializationContext deserializationContext)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(LCCN)) {
      return jp.getCodec().treeToValue(node, LccnField.class);
    } else if (node.has(ISBN)) {
      return jp.getCodec().treeToValue(node, IsbnField.class);
    } else if (node.has(EAN)) {
      return jp.getCodec().treeToValue(node, EanField.class);
    } else if (node.has(LOCAL_ID)) {
      return jp.getCodec().treeToValue(node, LocalIdField.class);
    } else if (node.has(OTHER_ID)) {
      return jp.getCodec().treeToValue(node, OtherIdField.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(InstanceAllOfMapInner.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }
}
