package org.folio.linked.data.configuration.json.deserialization.event;

import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.utils.KafkaEventsTestDataFixture.dataImportEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.folio.search.domain.dto.DataImportEvent;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class DataImportEventDeserializerTest {

  @Test
  void shouldDeserialize() throws IOException {
    String eventId = "event_id_01";
    String eventType = "event_type_01";
    String tenantId = "tenant_01";
    String marc = """
      {
        "leader":"01767cam a22003977i 4500",
        "fields":[
          {"001":"21009122"},
          {"906":{"subfields":[{"a":"7"}]}}
      }""";

    String eventJson = dataImportEvent(eventId, tenantId, eventType, marc);

    // verify deserializer
    DataImportEvent dataImportEvent = OBJECT_MAPPER.readValue(eventJson, DataImportEvent.class);

    assertEquals(eventId, dataImportEvent.getId());
    assertEquals(eventType, dataImportEvent.getEventType());
    assertEquals(tenantId, dataImportEvent.getTenant());
    assertEquals(marc, dataImportEvent.getMarc());
  }
}
