package org.folio.linked.data.test.kafka;

import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;

import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@Getter
@Profile(SEARCH_PROFILE)
public class KafkaSearchIndexTopicListener {
  private CountDownLatch latch = new CountDownLatch(1);
  private String payload;

  @KafkaListener(topics = "${test.topic.search}")
  public void receive(ConsumerRecord<?, ?> consumerRecord) {
    log.info("received consumerRecord = [{}]", consumerRecord.toString());
    payload = consumerRecord.value().toString();
    latch.countDown();
  }

  public void resetLatch() {
    latch = new CountDownLatch(1);
  }

}
