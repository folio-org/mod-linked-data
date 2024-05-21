package org.folio.linked.data.test;

import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;

import java.util.Map;
import lombok.SneakyThrows;

public class KafkaEventsTestDataFixture {

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
