package org.folio.linked.data.integration;

import static org.folio.linked.data.util.TextUtil.slugify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.folio.linked.data.domain.dto.KafkaMessage;
import org.folio.linked.data.model.entity.Bibframe;
import org.folio.linked.data.repo.BibframeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class KafkaMessageListener {

  public static final String LISTENER_ID = "LISTENER_BIBFRAME";
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private final BibframeRepository bibframeRepository;

  @KafkaListener(
    id = LISTENER_ID,
    containerFactory = "kafkaListenerContainerFactory",
    topicPattern = "#{folioKafkaProperties.listener['bibframes'].topicPattern}",
    groupId = "#{folioKafkaProperties.listener['bibframes'].groupId}",
    concurrency = "#{folioKafkaProperties.listener['bibframes'].concurrency}")
  public void handleMessages(List<ConsumerRecord<String, KafkaMessage>> consumerRecords) {
    log.info("Processing kafka messages [number of messages: {}]", consumerRecords.size());
    consumerRecords.stream()
      .map(ConsumerRecord::value)
      .forEach(this::handleSingle);
  }

  @SneakyThrows
  private void handleSingle(KafkaMessage kafkaMessage) {
    var entity = new Bibframe();
    entity.setGraphName("kafkaMessage_" + kafkaMessage.getId());
    entity.setGraphHash(entity.getGraphName().hashCode());
    entity.setSlug(slugify(entity.getGraphName()));
    var jsonNode = objectMapper.readTree("{\"value\": \"" + kafkaMessage.getValue() + "\"}");
    entity.setConfiguration(jsonNode);
    bibframeRepository.save(entity);
  }

}
