package org.folio.linked.data.mapper.kafka.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;

import java.util.stream.Stream;
import org.folio.linked.data.domain.dto.LinkedDataHub;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class HubSearchMessageMapperTest {
  private final HubSearchMessageMapper mapper = new HubSearchMessageMapperImpl();

  @ParameterizedTest
  @MethodSource("linkAndExpectedOriginalId")
  void testToIndex_extractsOriginalIdFromLink(String link, String expectedOriginalId) throws Exception {
    // Given
    var resource = new Resource()
      .setIdAndRefreshEdges(123L)
      .setLabel("Test Hub")
      .setDoc(TEST_JSON_MAPPER.readTree(String.format("""
        {
           "http://bibfra.me/vocab/lite/link": [ "%s" ]
        }
        """, link)));

    // When
    var indexEvent = mapper.toIndex(resource, CREATE);

    // Then
    assertThat(indexEvent).isNotNull();
    assertThat(indexEvent.getId()).isNotNull();
    assertThat(indexEvent.getResourceName()).isEqualTo("linked-data-hub");
    assertThat(indexEvent.getType()).isEqualTo(CREATE);
    var hub = (LinkedDataHub) indexEvent.getNew();
    assertThat(hub).isNotNull();
    assertThat(hub.getId()).isEqualTo("123");
    assertThat(hub.getLabel()).isEqualTo("Test Hub");
    assertThat(hub.getOriginalId()).isEqualTo(expectedOriginalId);
  }

  @Test
  void testToIndex_nullResourceReturnsNull() {
    assertThat(mapper.toIndex(null, null)).isNull();
  }

  private static Stream<Arguments> linkAndExpectedOriginalId() {
    return Stream.of(
      Arguments.of("http://id.loc.gov/resources/hubs/abc123", "abc123"),
      Arguments.of("http://id.loc.gov/resources/hubs/abc123.html", "abc123"),
      Arguments.of("https://id.loc.gov/resources/hubs/abc123", "abc123"),
      Arguments.of("http://wrong.path/abc123", null)
    );
  }
}
