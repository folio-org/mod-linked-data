package org.folio.linked.data.test.kafka;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@Getter
@Profile("!" + STANDALONE_PROFILE)
public class KafkaSearchWorkIndexTopicListener {
  private final List<String> messages = new CopyOnWriteArrayList<>();

  @KafkaListener(topics = "${test.topic.search-work}")
  public void receive(ConsumerRecord<?, ?> consumerRecord) {
    log.info("received consumerRecord = [{}]", consumerRecord.toString());
    messages.add(consumerRecord.value().toString());
  }

}
