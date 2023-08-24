package org.folio.linked.data.configuration.json.deserialization.instance;

import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION_URL;
import static org.folio.linked.data.util.BibframeConstants.MANUFACTURE_URL;
import static org.folio.linked.data.util.BibframeConstants.PRODUCTION_URL;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION_URL;
import static org.folio.linked.data.util.Constants.DTO_UNKNOWN_SUB_ELEMENT;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.folio.linked.data.domain.dto.DistributionField2;
import org.folio.linked.data.domain.dto.Instance2ProvisionActivityInner;
import org.folio.linked.data.domain.dto.ManufactureField2;
import org.folio.linked.data.domain.dto.ProductionField2;
import org.folio.linked.data.domain.dto.PublicationField2;
import org.folio.linked.data.exception.JsonException;

public class ProvisionActivity2Deserializer extends JsonDeserializer<Instance2ProvisionActivityInner> {

  @Override
  public Instance2ProvisionActivityInner deserialize(JsonParser jp, DeserializationContext ctxt)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(DISTRIBUTION_URL)) {
      return jp.getCodec().treeToValue(node, DistributionField2.class);
    } else if (node.has(MANUFACTURE_URL)) {
      return jp.getCodec().treeToValue(node, ManufactureField2.class);
    } else if (node.has(PRODUCTION_URL)) {
      return jp.getCodec().treeToValue(node, ProductionField2.class);
    } else if (node.has(PUBLICATION_URL)) {
      return jp.getCodec().treeToValue(node, PublicationField2.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(Instance2ProvisionActivityInner.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }

}
