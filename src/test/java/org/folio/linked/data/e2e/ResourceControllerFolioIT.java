package org.folio.linked.data.e2e;

import static java.util.Objects.nonNull;
import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import lombok.SneakyThrows;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.test.kafka.KafkaSearchIndexTopicListener;
import org.folio.search.domain.dto.ResourceEventType;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE, SEARCH_PROFILE})
public class ResourceControllerFolioIT extends ResourceControllerIT {

  @Autowired
  private KafkaSearchIndexTopicListener consumer;

  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @AfterEach
  public void clean() {
    consumer.getMessages().clear();
  }

  @SneakyThrows
  @Override
  protected void checkKafkaMessageSent(Resource persisted, Long deleted) {
    if (nonNull(persisted)) {
      await().untilAsserted(() -> {
        var message = consumer.getMessages().get(0);
        assertTrue(message.contains(persisted.getResourceHash().toString()));
        assertTrue(message.contains(ResourceEventType.CREATE.getValue()));
      });
    }
    if (nonNull(deleted)) {
      await().untilAsserted(() -> {
        var message = consumer.getMessages().get(0);
        assertTrue(message.contains(deleted.toString()));
        assertTrue(message.contains(ResourceEventType.DELETE.getValue()));
      });
    }
  }
}
