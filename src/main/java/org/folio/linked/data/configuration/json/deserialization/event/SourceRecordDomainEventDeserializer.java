package org.folio.linked.data.configuration.json.deserialization.event;

import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum;
import static org.folio.linked.data.util.JsonUtils.JSON_MAPPER;

import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ParsedRecord;
import org.folio.linked.data.domain.dto.SourceRecord;
import org.folio.linked.data.domain.dto.SourceRecordDomainEvent;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Class responsible for deserializing the Kafka event from SRS module into {@link SourceRecordDomainEvent} object.
 */
@Log4j2
public class SourceRecordDomainEventDeserializer extends ValueDeserializer<SourceRecordDomainEvent> {

  private static final String ID = "id";
  private static final String EVENT_TYPE = "eventType";
  private static final String EVENT_PAYLOAD = "eventPayload";
  private static final String DELETED = "deleted";
  private static final String PARSED_RECORD = "parsedRecord";
  private static final String CONTENT = "content";
  private static final String NEW = "new";

  @Override
  public SourceRecordDomainEvent deserialize(JsonParser jp, DeserializationContext context) {
    var event = new SourceRecordDomainEvent();
    JsonNode node = jp.readValueAsTree();
    if (node.has(ID)) {
      event.setId(node.get(ID).asString());
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
    var incoming = node.get(EVENT_TYPE).asString();
    try {
      return EventTypeEnum.fromValue(incoming);
    } catch (IllegalArgumentException illegalArgumentException) {
      log.warn("Unknown SRS Domain event type [{}]", incoming);
    }
    return null;
  }

  private SourceRecord getSourceRecord(JsonNode node) {
    if (node.isValueNode() && node.isString()) {
      var jsonString = node.asString();
      node = JSON_MAPPER.readTree(jsonString);
    }
    if (node.has(NEW)) {
      node = node.get(NEW);
    }
    var sourceRecord = new SourceRecord();
    if (node.has(ID)) {
      sourceRecord.setId(node.get(ID).asString());
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
    if (node.isValueNode() && node.isString()) {
      return node.asString();
    }
    return String.valueOf(node);
  }

}
