package org.folio.linked.data.integration;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.search.domain.dto.DataImportEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class KafkaMessageListener {

  private static final String DI_INSTANCE_CREATED_LISTENER = "mod-linked-data-data-import-instance-created-listener";

  @Qualifier("dataImportEventProcessor")
  private final Consumer<DataImportEvent> eventProcessor;

  @KafkaListener(
    id = DI_INSTANCE_CREATED_LISTENER,
    containerFactory = "dataImportListenerContainerFactory",
    groupId = "#{folioKafkaProperties.listener['data-import-instance-create'].groupId}",
    concurrency = "#{folioKafkaProperties.listener['data-import-instance-create'].concurrency}",
    topicPattern = "#{folioKafkaProperties.listener['data-import-instance-create'].topicPattern}")
  public void handleDataImportInstanceCreatedEvent(DataImportEvent event) {
    log.info("Received event: {}", event);
    eventProcessor.accept(event);
  }
}
