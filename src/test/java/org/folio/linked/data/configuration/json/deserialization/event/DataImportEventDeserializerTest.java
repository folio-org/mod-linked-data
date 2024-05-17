package org.folio.linked.data.configuration.json.deserialization.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.KafkaEventsTestDataFixture.authorityEvent;
import static org.folio.linked.data.test.KafkaEventsTestDataFixture.dataImportEvent;
import static org.folio.linked.data.test.KafkaEventsTestDataFixture.instanceCreatedEvent;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;

import java.io.IOException;
import java.util.Map;
import org.folio.search.domain.dto.DataImportEvent;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class DataImportEventDeserializerTest {

  @Test
  void should_deserialize_instance_created_event() throws IOException {
    // given
    String eventId = "event_id_01";
    String tenantId = "tenant_01";
    String marc = """
      {
        "leader":"01767cam a22003977i 4500",
        "fields":[
          {"001":"21009122"},
          {"906":{"subfields":[{"a":"7"}]}}
      }""";

    String eventJson = instanceCreatedEvent(eventId, tenantId, marc);

    // when
    DataImportEvent dataImportEvent = OBJECT_MAPPER.readValue(eventJson, DataImportEvent.class);

    // then
    assertThat(eventId).isEqualTo(dataImportEvent.getId());
    assertThat(tenantId).isEqualTo(dataImportEvent.getTenant());
    assertThat(marc).isEqualTo(dataImportEvent.getMarcBib());
    assertThat(dataImportEvent.getMarcAuthority()).isNull();
  }

  @Test
  void should_deserialize_authority_event() throws IOException {
    // given
    String eventId = "event_id_01";
    String tenantId = "tenant_01";
    String marc = """
      {
        "leader":"01767cam a22003977i 4500",
        "fields":[
          {"001":"21009122"},
          {"100":{"subfields":[{"a":"John Doe"}]}}
      }""";

    String eventJson = authorityEvent(eventId, tenantId, marc);

    // when
    DataImportEvent dataImportEvent = OBJECT_MAPPER.readValue(eventJson, DataImportEvent.class);

    // then
    assertThat(eventId).isEqualTo(dataImportEvent.getId());
    assertThat(tenantId).isEqualTo(dataImportEvent.getTenant());
    assertThat(marc).isEqualTo(dataImportEvent.getMarcAuthority());
    assertThat(dataImportEvent.getMarcBib()).isNull();
  }


  @Test
  void should_not_deserialize_unsupported_event() throws IOException {
    // given
    String eventId = "event_id_01";

    String unsupportedEvent = dataImportEvent(eventId, Map.of(
      "eventType", "DI_COMPLETED",
      "tenant", "tenant_01",
      "context", Map.of(
        "MARC_BIBLIOGRAPHIC", "{}",
        "CURRENT_EVENT_TYPE", "UNSUPPORTED_EVENT_TYPE"
      )
    ));

    // when
    DataImportEvent dataImportEvent = OBJECT_MAPPER.readValue(unsupportedEvent, DataImportEvent.class);

    // then
    assertThat(dataImportEvent.getMarcBib()).isNull();
    assertThat(dataImportEvent.getMarcAuthority()).isNull();
  }
}
