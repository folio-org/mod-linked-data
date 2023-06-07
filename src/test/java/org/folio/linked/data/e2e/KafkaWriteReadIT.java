package org.folio.linked.data.e2e;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_HUNDRED_MILLISECONDS;
import static org.folio.linked.data.TestUtil.random;
import static org.folio.linked.data.TestUtil.randomString;
import static org.folio.linked.data.util.Constants.FOLIO_ENV;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import org.folio.linked.data.domain.dto.KafkaMessage;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Bibframe;
import org.folio.linked.data.repo.BibframeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

@IntegrationTest
class KafkaWriteReadIT {
  @Autowired
  private KafkaTemplate<String, KafkaMessage> kafkaTemplate;
  @Value("${folio.environment}")
  private String folioEnv;
  @MockBean
  private BibframeRepository bibframeRepository;

  @Test
  void writeAndReadKafkaMessage() {
    // given
    var kafkaMessage = random(KafkaMessage.class);
    var topicTenant = FOLIO_ENV.equals(folioEnv) ? "test_tenant" : "standalone";

    // when
    kafkaTemplate.send("folio." + topicTenant + ".linked-data.bibframe", randomString(), kafkaMessage);

    // then
    await().atMost(Duration.ofSeconds(30)).pollInterval(ONE_HUNDRED_MILLISECONDS).untilAsserted(() ->
      verify(bibframeRepository).save(any(Bibframe.class)));
  }

}
