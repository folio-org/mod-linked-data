package org.folio.linked.data.test.kafka;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.folio.linked.data.domain.dto.ImportResultEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@Getter
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class KafkaImportResultTopicListener {
  private final ObjectMapper objectMapper;
  private final List<ImportResultEvent> messages = new CopyOnWriteArrayList<>();

  @KafkaListener(topics = "${test.topic.ld-import-result}")
  @SneakyThrows
  public void receive(ConsumerRecord<?, ?> consumerRecord) {
    log.info("received consumerRecord = [{}]", consumerRecord.toString());
    var result = objectMapper.readValue(consumerRecord.value().toString(), ImportResultEvent.class);
    messages.add(result);
  }

  public void clear() {
    messages.clear();
  }
}

