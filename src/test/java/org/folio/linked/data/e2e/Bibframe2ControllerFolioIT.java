package org.folio.linked.data.e2e;

import static java.util.Objects.nonNull;
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

@ActiveProfiles({"folio", "test-folio", "search"})
public class Bibframe2ControllerFolioIT extends Bibframe2ControllerIT {

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
