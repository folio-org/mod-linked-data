package org.folio.linked.data.configuration.json.deserialization.event;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
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
  private static final String DI_INVENTORY_INSTANCE_CREATED = "DI_INVENTORY_INSTANCE_CREATED";
  private static final String CURRENT_EVENT_TYPE = "CURRENT_EVENT_TYPE";
  private static final String MARC_AUTHORITY = "MARC_AUTHORITY";

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
      if (isInstanceCreatedEvent(eventPayload)) {
        getMarcBibRecord(eventPayload).ifPresent(event::setMarcBib);
      } else if (isAuthorityEvent(eventPayload)) {
        getMarcAuthorityRecord(eventPayload).ifPresent(event::setMarcAuthority);
      }
    }

    return event;
  }

  private Optional<String> getMarcBibRecord(JsonNode eventPayload) {
    return getMarc(eventPayload, MARC_BIBLIOGRAPHIC_FIELD);
  }

  private Optional<String> getMarcAuthorityRecord(JsonNode eventPayload) {
    return getMarc(eventPayload, MARC_AUTHORITY);
  }

  @SneakyThrows
  private Optional<String> getMarc(JsonNode eventPayload, String contextField) {
    var marcWrapper = eventPayload.path(CONTEXT_FIELD).path(contextField).textValue();
    if (isBlank(marcWrapper)) {
      return Optional.empty();
    }
    var marc = objectMapper.readTree(marcWrapper).path(PARSED_RECORD_FIELD).path(CONTENT_FIELD).textValue();
    return Optional.ofNullable(marc);
  }

  private boolean isInstanceCreatedEvent(JsonNode eventPayload) {
    if (eventPayload.has(CONTEXT_FIELD)) {
      var contextField = eventPayload.path(CONTEXT_FIELD);
      return contextField.has(CURRENT_EVENT_TYPE)
        && DI_INVENTORY_INSTANCE_CREATED.equals(contextField.path(CURRENT_EVENT_TYPE).textValue());
    }
    return false;
  }

  private boolean isAuthorityEvent(JsonNode eventPayload) {
    return eventPayload.has(CONTEXT_FIELD) && eventPayload.path(CONTEXT_FIELD).has(MARC_AUTHORITY);
  }
}
