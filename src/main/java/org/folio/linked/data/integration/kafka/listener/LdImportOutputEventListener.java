package org.folio.linked.data.integration.kafka.listener;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.folio.linked.data.util.KafkaUtils.handleForExistedTenant;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.integration.kafka.listener.handler.ExternalEventHandler;
import org.folio.linked.data.service.tenant.LinkedDataTenantService;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class LdImportOutputEventListener {

  private static final String LISTENER_ID = "mod-linked-data-ld-import-event-listener";
  private static final String CONTAINER_FACTORY = "importOutputEventListenerContainerFactory";
  private final ExternalEventHandler<ImportOutputEvent> ldImportOutputEventHandler;
  private final TenantScopedExecutionService tenantScopedExecutionService;
  private final LinkedDataTenantService linkedDataTenantService;

  @KafkaListener(
    id = LISTENER_ID,
    containerFactory = CONTAINER_FACTORY,
    groupId = "#{folioKafkaProperties.listener['ld-import-output-event'].groupId}",
    concurrency = "#{folioKafkaProperties.listener['ld-import-output-event'].concurrency}",
    topicPattern = "#{folioKafkaProperties.listener['ld-import-output-event'].topicPattern}")
  public void handleImportOutputEvents(List<ConsumerRecord<String, ImportOutputEvent>> consumerRecords) {
    consumerRecords.forEach(consumerRecord -> {
      var event = consumerRecord.value();
      handleForExistedTenant(consumerRecord, event.getTs(), linkedDataTenantService, log, this::handleRecord);
    });
  }

  private void handleRecord(ConsumerRecord<String, ImportOutputEvent> consumerRecord) {
    log.info("Processing LD-Import output event with Job ID {} and ts {}",
      consumerRecord.value().getJobExecutionId(), consumerRecord.value().getTs());
    var event = consumerRecord.value();
    var startTime = OffsetDateTime.now();
    tenantScopedExecutionService.executeWithRetry(
      consumerRecord.headers(),
      () -> {
        ldImportOutputEventHandler.handle(event, startTime);
        return null;
      },
      ex -> log.error("Failed to handle LD-Import output event with id {}. Retrying: {}", event.getTs(), ex)
    );
  }

}
