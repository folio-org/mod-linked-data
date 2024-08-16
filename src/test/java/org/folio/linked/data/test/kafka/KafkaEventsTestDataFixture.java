package org.folio.linked.data.test.kafka;

import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.RECORD_DOMAIN_EVENT_TOPIC;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.defaultKafkaHeaders;
import static org.folio.search.domain.dto.SourceRecordDomainEvent.EventTypeEnum;
import static org.folio.spring.tools.kafka.KafkaUtils.getTenantTopicName;

import java.util.ArrayList;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.folio.search.domain.dto.SourceRecordDomainEvent;
import org.folio.search.domain.dto.SourceRecordType;

public class KafkaEventsTestDataFixture {

  @SneakyThrows
  public static ProducerRecord<String, String> getSrsDomainEventProducerRecord(String id,
                                                                               String marc,
                                                                               EventTypeEnum type,
                                                                               SourceRecordType recordType) {
    var topic = getTenantTopicName(RECORD_DOMAIN_EVENT_TOPIC, TENANT_ID);
    var value = OBJECT_MAPPER.writeValueAsString(Map.of(
        "id", id,
        "eventType", type,
        "eventPayload", marc
      )
    );
    var headers = new ArrayList<>(defaultKafkaHeaders());
    headers.add(new RecordHeader("folio.srs.recordType", recordType.name().getBytes()));
    return new ProducerRecord(topic, 0, id, value, headers);
  }

  public static SourceRecordDomainEvent getSrsDomainEvent(String id,
                                                          String marc,
                                                          EventTypeEnum type) {
    return new SourceRecordDomainEvent()
      .id(id)
      .eventType(type)
      .eventPayload(marc);
  }

  @SneakyThrows
  public static String instanceCreatedEvent(String eventId, String tenantId, String marc) {
    Map<String, Object> marcBib = Map.of(
      "parsedRecord", Map.of(
        "content", marc
      )
    );
    Map<String, Object> eventPayload = Map.of(
      "eventType", "DI_COMPLETED",
      "tenant", tenantId,
      "context", Map.of(
        "MARC_BIBLIOGRAPHIC", OBJECT_MAPPER.writeValueAsString(marcBib),
        "CURRENT_EVENT_TYPE", "DI_INVENTORY_INSTANCE_CREATED"
      )
    );

    return dataImportEvent(eventId, eventPayload);
  }

  @SneakyThrows
  public static String authorityEvent(String eventId, String tenantId, String marc) {
    Map<String, Object> marcBib = Map.of(
      "parsedRecord", Map.of(
        "content", marc
      )
    );
    Map<String, Object> eventPayload = Map.of(
      "eventType", "DI_COMPLETED",
      "tenant", tenantId,
      "context", Map.of(
        "MARC_AUTHORITY", OBJECT_MAPPER.writeValueAsString(marcBib)
      )
    );
    return dataImportEvent(eventId, eventPayload);
  }

  @SneakyThrows
  public static String dataImportEvent(String eventId, Map<String, Object> eventPayload) {
    Map<String, Object> event = Map.of(
      "id", eventId,
      "eventPayload", OBJECT_MAPPER.writeValueAsString(eventPayload)
    );

    return OBJECT_MAPPER.writeValueAsString(event);
  }

}
