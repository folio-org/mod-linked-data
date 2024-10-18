package org.folio.linked.data.test.kafka;

import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_BIB;
import static org.folio.linked.data.test.TestUtil.INVENTORY_INSTANCE_EVENT_TOPIC;
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
import org.folio.linked.data.domain.dto.ParsedRecord;
import org.folio.linked.data.domain.dto.SourceRecord;
import org.folio.linked.data.domain.dto.SourceRecordDomainEvent;
import org.folio.linked.data.domain.dto.SourceRecordType;
import org.folio.linked.data.test.TestUtil;

public class KafkaEventsTestDataFixture {

  public static ProducerRecord<String, String> getInventoryInstanceEventSampleProducerRecord() {
    var topic = getTenantTopicName(INVENTORY_INSTANCE_EVENT_TOPIC, TENANT_ID);
    var value = TestUtil.loadResourceAsString("samples/inventoryInstanceEvent.json");
    var headers = new ArrayList<>(defaultKafkaHeaders());
    return new ProducerRecord(topic, 0, "1", value, headers);
  }

  public static ProducerRecord<String, String> getSrsDomainEventSampleProducerRecord() {
    var topic = getTenantTopicName(RECORD_DOMAIN_EVENT_TOPIC, TENANT_ID);
    var value = TestUtil.loadResourceAsString("samples/srsDomainEvent.json");
    var headers = new ArrayList<>(defaultKafkaHeaders());
    headers.add(new RecordHeader("folio.srs.recordType", MARC_BIB.name().getBytes()));
    return new ProducerRecord(topic, 0, "1", value, headers);
  }

  @SneakyThrows
  public static ProducerRecord<String, String> getSrsDomainEventProducerRecord(String id,
                                                                               String marc,
                                                                               EventTypeEnum type,
                                                                               SourceRecordType recordType) {
    var topic = getTenantTopicName(RECORD_DOMAIN_EVENT_TOPIC, TENANT_ID);
    var value = OBJECT_MAPPER.writeValueAsString(Map.of(
        "id", id,
        "eventType", type,
        "eventPayload", new SourceRecord().parsedRecord(new ParsedRecord(marc))
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
      .eventPayload(new SourceRecord().parsedRecord(new ParsedRecord(marc)));
  }

}
