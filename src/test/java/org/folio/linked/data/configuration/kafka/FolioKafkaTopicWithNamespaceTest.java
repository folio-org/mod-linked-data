package org.folio.linked.data.configuration.kafka;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class FolioKafkaTopicWithNamespaceTest {

  @Test
  void getTenantTopicNameWithNamespace_shouldReturn_tenantTopicNameWithNamespace() {
    //given
    var topic = (FolioKafkaTopicWithNamespace) () -> "some-topic-name";

    //expect
    assertEquals("folio.Default.test-tenant.some-topic-name", topic.fullTopicName("test-tenant"));
  }

  @ParameterizedTest
  @CsvSource({
    "' ', tenant",
    "topic, ' '"
  })
  void getTenantTopicNameWithNamespace_shouldThrow_illegalArgumentException(String topicName, String tenant) {
    //given
    var topic = (FolioKafkaTopicWithNamespace) () -> topicName;

    //expect
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> topic.fullTopicName(tenant));
  }

}
