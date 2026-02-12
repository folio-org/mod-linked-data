package org.folio.linked.data.configuration.json.deserialization.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.domain.dto.SourceRecord.StateEnum.ACTUAL;
import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum.SOURCE_RECORD_CREATED;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class SourceRecordDomainEventDeserializerTest {

  private final SourceRecordDomainEventDeserializer deserializer = new SourceRecordDomainEventDeserializer();

  @Test
  void deserialize_shouldReturnCorrectlyMappedEvent() {
    // given
    var jsonParser = TEST_JSON_MAPPER.createParser(loadResourceAsString("samples/srsDomainEvent.json"));

    // when
    var deserializedEvent = deserializer.deserialize(jsonParser, null);

    // then
    assertThat(deserializedEvent.getId()).isEqualTo("23b34a6f-c095-41ea-917b-9765add1a444");
    assertThat(deserializedEvent.getEventType()).isEqualTo(SOURCE_RECORD_CREATED);
    assertThat(deserializedEvent.getEventMetadata()).isNull();

    var deserializedPayload = deserializedEvent.getEventPayload();
    assertThat(deserializedPayload.getId()).isEqualTo("758f57ff-7dad-419e-803c-44b621400c11");
    assertThat(deserializedPayload.getDeleted()).isTrue();
    assertThat(deserializedPayload.getState()).isEqualTo(ACTUAL);
    assertThat(deserializedPayload.getParsedRecord().getContent()).isNotBlank();
  }
}
