package org.folio.linked.data.model.entity.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class ResourceUpdatedEventTest {

  private static Stream<Arguments> provideIdsAndExpectedValue() {
    return Stream.of(
      Arguments.of(null, null, false),
      Arguments.of(null, 1L, false),
      Arguments.of(2L, null, false),
      Arguments.of(3L, 4L, false),
      Arguments.of(5L, 5L, true)
    );
  }

  @ParameterizedTest
  @MethodSource("provideIdsAndExpectedValue")
  void isSameResourceUpdated_shouldReturnExpectedResult(Long id1, Long id2, boolean expectedResult) {
    // given
    var resourceUpdatedEvent = new ResourceUpdatedEvent(new Resource().setId(id1), new Resource().setId(id2));

    // when
    var result = resourceUpdatedEvent.isSameResourceUpdated();

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

}
