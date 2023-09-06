package org.folio.linked.data.e2e;

import lombok.SneakyThrows;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.test.kafka.KafkaSearchIndexTopicListener;
import org.folio.search.domain.dto.ResourceEventType;
import org.hamcrest.MatcherAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.nonNull;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles({"folio", "test-folio", "search"})
public class IndexControllerITFolio extends IndexControllerIT {

  @Autowired
  private KafkaSearchIndexTopicListener consumer;

  @Override
  @SneakyThrows
  protected void checkKafkaMessageSent(Resource persisted) {
    boolean messageConsumed = consumer.getLatch().await(10, TimeUnit.SECONDS);
    assertTrue(messageConsumed);
    if (nonNull(persisted)) {
      MatcherAssert.assertThat(consumer.getPayload(), containsString(persisted.getResourceHash().toString()));
      MatcherAssert.assertThat(consumer.getPayload(), containsString(ResourceEventType.CREATE.getValue()));
    }
  }
}
