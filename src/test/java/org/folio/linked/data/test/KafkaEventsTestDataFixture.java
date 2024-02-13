package org.folio.linked.data.test;

import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;

import java.util.Map;
import lombok.SneakyThrows;

public class KafkaEventsTestDataFixture {

  @SneakyThrows
  public static String dataImportEvent(String eventId, String tenantId, String eventType, String marc) {
    Map<String, Object> marcBib = Map.of(
      "parsedRecord", Map.of(
        "content", marc
      )
    );
    Map<String, Object> eventPayload = Map.of(
      "eventType", eventType,
      "tenant", tenantId,
      "context", Map.of(
        "MARC_BIBLIOGRAPHIC", OBJECT_MAPPER.writeValueAsString(marcBib)
      )
    );
    Map<String, Object> event = Map.of(
      "id", eventId,
      "eventPayload", OBJECT_MAPPER.writeValueAsString(eventPayload)
    );

    return OBJECT_MAPPER.writeValueAsString(event);
  }
}
