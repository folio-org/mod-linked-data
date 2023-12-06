package org.folio.linked.data.e2e;

import static java.util.Objects.nonNull;
import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import static org.testcontainers.shaded.org.awaitility.Durations.FIVE_SECONDS;

import lombok.SneakyThrows;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.test.kafka.KafkaSearchIndexTopicListener;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE, SEARCH_PROFILE})
public class ReIndexControllerFolioIT extends ReIndexControllerIT {

  @Autowired
  private KafkaSearchIndexTopicListener consumer;

  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @Override
  @SneakyThrows
  protected void checkKafkaMessageSent(Resource indexed) {
    if (nonNull(indexed)) {
      await().pollDelay(FIVE_SECONDS).untilAsserted(() -> assertTrue(consumer.getMessages()
        .stream()
        .anyMatch(m -> m.contains(indexed.getResourceHash().toString()) && m.contains(CREATE.getValue()))));
    } else {
      await().pollDelay(FIVE_SECONDS).untilAsserted(() -> assertTrue(consumer.getMessages().isEmpty()));
    }
  }

}
