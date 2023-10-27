package org.folio.linked.data.e2e;

import static java.util.Objects.nonNull;
import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.test.kafka.KafkaSearchIndexTopicListener;
import org.folio.search.domain.dto.ResourceEventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE, SEARCH_PROFILE})
public class ResourceControllerFolioIT extends ResourceControllerIT {

  @Autowired
  private KafkaSearchIndexTopicListener consumer;

  @SneakyThrows
  @Override
  protected void checkKafkaMessageSent(Resource persisted, Long deleted) {
    boolean messageConsumed = consumer.getLatch().await(10, TimeUnit.SECONDS);
    assertTrue(messageConsumed);
    if (nonNull(persisted)) {
      assertThat(consumer.getPayload(), containsString(persisted.getResourceHash().toString()));
      assertThat(consumer.getPayload(), containsString(ResourceEventType.CREATE.getValue()));
    }
    if (nonNull(deleted)) {
      assertThat(consumer.getPayload(), containsString(deleted.toString()));
      assertThat(consumer.getPayload(), containsString(ResourceEventType.DELETE.getValue()));
    }
  }
}
