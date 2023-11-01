package org.folio.linked.data.configuration.json.deserialization.instance;

import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.util.Constants.DTO_UNKNOWN_SUB_ELEMENT;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.folio.linked.data.domain.dto.AgentTypeInner;
import org.folio.linked.data.domain.dto.FamilyField;
import org.folio.linked.data.domain.dto.MeetingField;
import org.folio.linked.data.domain.dto.OrganizationField;
import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.exception.JsonException;

public class AgentDeserializer extends JsonDeserializer<AgentTypeInner> {

  @Override
  public AgentTypeInner deserialize(JsonParser jp, DeserializationContext deserializationContext)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(PERSON.getUri())) {
      return jp.getCodec().treeToValue(node, PersonField.class);
    } else if (node.has(MEETING.getUri())) {
      return jp.getCodec().treeToValue(node, MeetingField.class);
    } else if (node.has(ORGANIZATION.getUri())) {
      return jp.getCodec().treeToValue(node, OrganizationField.class);
    } else if (node.has(FAMILY.getUri())) {
      return jp.getCodec().treeToValue(node, FamilyField.class);
    }
    var field = node.fieldNames().hasNext() ? node.fieldNames().next() : "";
    throw new JsonException(AgentTypeInner.class.getSimpleName() + DTO_UNKNOWN_SUB_ELEMENT + field);
  }
}
