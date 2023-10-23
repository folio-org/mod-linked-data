package org.folio.linked.data.configuration.json.deserialization.event;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.folio.search.domain.dto.DataImportEvent;

/**
 * Class responsible for deserializing the Kafka event from Data Import module into {@link DataImportEvent} object.
 */
@Log4j2
@RequiredArgsConstructor
public class DataImportEventDeserializer extends JsonDeserializer<DataImportEvent> {

  private static final String ID_FILED = "id";
  private static final String EVENT_PAYLOAD_FIELD = "eventPayload";
  private static final String EVENT_TYPE_FIELD = "eventType";
  private static final String CONTEXT_FIELD = "context";
  private static final String MARC_BIBLIOGRAPHIC_FIELD = "MARC_BIBLIOGRAPHIC";
  private static final String PARSED_RECORD_FIELD = "parsedRecord";
  private static final String CONTENT_FIELD = "content";
  private static final String TENANT_FIELD = "tenant";

  private final ObjectMapper objectMapper;

  @Override
  public DataImportEvent deserialize(JsonParser jp, DeserializationContext context) throws IOException {
    DataImportEvent event = new DataImportEvent();

    JsonNode node = jp.getCodec().readTree(jp);

    if (node.has(ID_FILED)) {
      event.setId(node.get(ID_FILED).textValue());
    }

    if (node.has(EVENT_PAYLOAD_FIELD)) {
      JsonNode eventPayload = objectMapper.readTree(node.get(EVENT_PAYLOAD_FIELD).textValue());
      event.setEventType(eventPayload.path(EVENT_TYPE_FIELD).textValue());
      event.setTenant(eventPayload.path(TENANT_FIELD).textValue());
      var marcBibStr = eventPayload.path(CONTEXT_FIELD).path(MARC_BIBLIOGRAPHIC_FIELD).textValue();
      if (Strings.isNotEmpty(marcBibStr)) {
        String marc = objectMapper.readTree(marcBibStr).path(PARSED_RECORD_FIELD).path(CONTENT_FIELD).textValue();
        event.setMarc(marc);
      }
    }

    return event;
  }
}
