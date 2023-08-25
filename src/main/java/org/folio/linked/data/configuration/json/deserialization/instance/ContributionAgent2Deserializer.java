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
import org.folio.linked.data.domain.dto.Contribution2AgentInner;
import org.folio.linked.data.domain.dto.FamilyField2;
import org.folio.linked.data.domain.dto.JurisdictionField2;
import org.folio.linked.data.domain.dto.MeetingField2;
import org.folio.linked.data.domain.dto.OrganizationField2;
import org.folio.linked.data.domain.dto.PersonField2;
import org.folio.linked.data.exception.JsonException;

public class ContributionAgent2Deserializer extends JsonDeserializer<Contribution2AgentInner> {

  @Override
  public Contribution2AgentInner deserialize(JsonParser jp, DeserializationContext deserializationContext)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(PERSON_URL)) {
      return jp.getCodec().treeToValue(node, PersonField2.class);
    } else if (node.has(FAMILY_URL)) {
      return jp.getCodec().treeToValue(node, FamilyField2.class);
    } else if (node.has(ORGANIZATION_URL)) {
      return jp.getCodec().treeToValue(node, OrganizationField2.class);
    }  else if (node.has(JURISDICTION_URL)) {
      return jp.getCodec().treeToValue(node, JurisdictionField2.class);
    } else if (node.has(MEETING_URL)) {
      return jp.getCodec().treeToValue(node, MeetingField2.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(Contribution2AgentInner.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }
}
