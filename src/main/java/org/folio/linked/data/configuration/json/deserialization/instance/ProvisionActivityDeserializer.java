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
import org.folio.linked.data.domain.dto.DistributionField;
import org.folio.linked.data.domain.dto.InstanceProvisionActivityInner;
import org.folio.linked.data.domain.dto.ManufactureField;
import org.folio.linked.data.domain.dto.ProductionField;
import org.folio.linked.data.domain.dto.PublicationField;
import org.folio.linked.data.exception.JsonException;

public class ProvisionActivityDeserializer extends JsonDeserializer<InstanceProvisionActivityInner> {

  @Override
  public InstanceProvisionActivityInner deserialize(JsonParser jp, DeserializationContext ctxt)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(DISTRIBUTION_URL)) {
      return jp.getCodec().treeToValue(node, DistributionField.class);
    } else if (node.has(MANUFACTURE_URL)) {
      return jp.getCodec().treeToValue(node, ManufactureField.class);
    } else if (node.has(PRODUCTION_URL)) {
      return jp.getCodec().treeToValue(node, ProductionField.class);
    } else if (node.has(PUBLICATION_URL)) {
      return jp.getCodec().treeToValue(node, PublicationField.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(InstanceProvisionActivityInner.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }

}
