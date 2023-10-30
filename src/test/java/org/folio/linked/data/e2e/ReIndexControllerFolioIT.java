package org.folio.linked.data.e2e;

import static java.util.Objects.nonNull;
import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.test.kafka.KafkaSearchIndexTopicListener;
import org.folio.search.domain.dto.ResourceEventType;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE, SEARCH_PROFILE})
@Disabled("To be enabled in part 3 task")
public class ReIndexControllerFolioIT extends ReIndexControllerIT {

  @Autowired
  private KafkaSearchIndexTopicListener consumer;

  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @Override
  @SneakyThrows
  protected void checkKafkaMessageSent(Resource persisted) {
    boolean messageConsumed = consumer.getLatch().await(30, TimeUnit.SECONDS);
    assertTrue(messageConsumed);
    if (nonNull(persisted)) {
      MatcherAssert.assertThat(consumer.getPayload(), containsString(persisted.getResourceHash().toString()));
      MatcherAssert.assertThat(consumer.getPayload(), containsString(ResourceEventType.CREATE.getValue()));
    }
  }
}
