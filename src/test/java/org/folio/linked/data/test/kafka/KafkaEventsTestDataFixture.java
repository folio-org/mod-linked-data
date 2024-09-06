package org.folio.linked.data.test.kafka;

import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.RECORD_DOMAIN_EVENT_TOPIC;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.defaultKafkaHeaders;
import static org.folio.spring.tools.kafka.KafkaUtils.getTenantTopicName;

import java.util.ArrayList;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.folio.linked.data.domain.dto.SourceRecordDomainEvent;
import org.folio.linked.data.domain.dto.SourceRecordType;

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

}
