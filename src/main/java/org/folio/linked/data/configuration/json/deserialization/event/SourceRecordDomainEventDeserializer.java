package org.folio.linked.data.configuration.json.deserialization.event;

import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ParsedRecord;
import org.folio.linked.data.domain.dto.SourceRecord;
import org.folio.linked.data.domain.dto.SourceRecordDomainEvent;

/**
 * Class responsible for deserializing the Kafka event from SRS module into {@link SourceRecordDomainEvent} object.
 */
@Log4j2
@RequiredArgsConstructor
public class SourceRecordDomainEventDeserializer extends JsonDeserializer<SourceRecordDomainEvent> {

  private static final String ID = "id";
  private static final String EVENT_TYPE = "eventType";
  private static final String EVENT_PAYLOAD = "eventPayload";
  private static final String DELETED = "deleted";
  private static final String PARSED_RECORD = "parsedRecord";
  private static final String CONTENT = "content";

  private final ObjectMapper objectMapper;

  @Override
  public SourceRecordDomainEvent deserialize(JsonParser jp, DeserializationContext context) throws IOException {
    var event = new SourceRecordDomainEvent();
    JsonNode node = jp.getCodec().readTree(jp);
    if (node.has(ID)) {
      event.setId(node.get(ID).textValue());
    }
    if (node.has(EVENT_TYPE)) {
      event.setEventType(getEventType(node));
    }
    if (node.has(EVENT_PAYLOAD)) {
      event.setEventPayload(getSourceRecord(node.get(EVENT_PAYLOAD)));
    }
    return event;
  }

  private EventTypeEnum getEventType(JsonNode node) {
    var incoming = node.get(EVENT_TYPE).textValue();
    try {
      return EventTypeEnum.fromValue(incoming);
    } catch (IllegalArgumentException illegalArgumentException) {
      log.warn("Unknown SRS Domain event type [{}]", incoming);
    }
    return null;
  }

  private SourceRecord getSourceRecord(JsonNode node) throws JsonProcessingException {
    if (node.isTextual()) {
      node = objectMapper.readTree(node.textValue());
    }
    var sourceRecord = new SourceRecord();
    if (node.has(ID)) {
      sourceRecord.setId(node.get(ID).textValue());
    }
    if (node.has(DELETED)) {
      sourceRecord.setDeleted(node.get(DELETED).asBoolean());
    }
    if (node.has(PARSED_RECORD)) {
      sourceRecord.setParsedRecord(getParsedRecord(node.get(PARSED_RECORD)));
    }
    return sourceRecord;
  }

  private ParsedRecord getParsedRecord(JsonNode node) {
    var pr = new ParsedRecord();
    if (node.has(CONTENT)) {
      pr.setContent(getContent(node.get(CONTENT)));
    }
    return pr;
  }

  private String getContent(JsonNode node) {
    if (node.isTextual()) {
      return node.textValue();
    }
    return String.valueOf(node);
  }

}
