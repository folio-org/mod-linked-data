package org.folio.linked.data.configuration.json.deserialization.instance;

import static org.folio.linked.data.util.BibframeConstants.FAMILY_URL;
import static org.folio.linked.data.util.BibframeConstants.JURISDICTION_URL;
import static org.folio.linked.data.util.BibframeConstants.MEETING_URL;
import static org.folio.linked.data.util.BibframeConstants.ORGANIZATION_URL;
import static org.folio.linked.data.util.BibframeConstants.PERSON_URL;
import static org.folio.linked.data.util.Constants.DTO_UNKNOWN_SUB_ELEMENT;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.folio.linked.data.domain.dto.ContributionAgentInner;
import org.folio.linked.data.domain.dto.FamilyField;
import org.folio.linked.data.domain.dto.JurisdictionField;
import org.folio.linked.data.domain.dto.MeetingField;
import org.folio.linked.data.domain.dto.OrganizationField;
import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.exception.JsonException;

public class ContributionAgentDeserializer extends JsonDeserializer<ContributionAgentInner> {

  @Override
  public ContributionAgentInner deserialize(JsonParser jp, DeserializationContext deserializationContext)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(PERSON_URL)) {
      return jp.getCodec().treeToValue(node, PersonField.class);
    } else if (node.has(FAMILY_URL)) {
      return jp.getCodec().treeToValue(node, FamilyField.class);
    } else if (node.has(ORGANIZATION_URL)) {
      return jp.getCodec().treeToValue(node, OrganizationField.class);
    }  else if (node.has(JURISDICTION_URL)) {
      return jp.getCodec().treeToValue(node, JurisdictionField.class);
    } else if (node.has(MEETING_URL)) {
      return jp.getCodec().treeToValue(node, MeetingField.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(ContributionAgentInner.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }
}
