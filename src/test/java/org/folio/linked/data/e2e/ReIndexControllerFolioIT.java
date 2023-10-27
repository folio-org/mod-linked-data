package org.folio.linked.data.e2e;

import static java.util.Objects.nonNull;
import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.test.kafka.KafkaSearchIndexTopicListener;
import org.folio.search.domain.dto.ResourceEventType;
import org.hamcrest.MatcherAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE, SEARCH_PROFILE})
public class ReIndexControllerFolioIT extends ReIndexControllerIT {

  @Autowired
  private KafkaSearchIndexTopicListener consumer;

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
