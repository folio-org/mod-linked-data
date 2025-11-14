package org.folio.linked.data.integration.kafka.listener;

import static java.util.Optional.ofNullable;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.folio.linked.data.util.KafkaUtils.handleForExistedTenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.Level;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.integration.kafka.listener.handler.ExternalEventHandler;
import org.folio.linked.data.service.tenant.LinkedDataTenantService;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.RetryContext;
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
  public void handleImportOutputEvent(ConsumerRecord<String, ImportOutputEvent> consumerRecord) {
    var event = consumerRecord.value();
    handleForExistedTenant(consumerRecord, event.getTs(), linkedDataTenantService, log, this::handleRecord);
  }

  private void handleRecord(ConsumerRecord<String, ImportOutputEvent> consumerRecord) {
    log.info("Processing LD-Import output event with Job ID {} and ts {}",
      consumerRecord.value().getJobInstanceId(), consumerRecord.value().getTs());
    var event = consumerRecord.value();
    tenantScopedExecutionService.executeAsyncWithRetry(
      consumerRecord.headers(),
      retryContext -> runRetryableJob(event, retryContext),
      ex -> logFailedEvent(event, ex, false)
    );
  }

  private void runRetryableJob(ImportOutputEvent event, RetryContext retryContext) {
    ofNullable(retryContext.getLastThrowable())
      .ifPresent(ex -> logFailedEvent(event, ex, true));
    ldImportOutputEventHandler.handle(event);
  }

  private void logFailedEvent(ImportOutputEvent event, Throwable ex, boolean isRetrying) {
    var logLevel = isRetrying ? Level.INFO : Level.ERROR;
    log.log(logLevel, "Failed to handle LD-Import output event with id {}. Retrying: {}",
      event.getTs(), isRetrying, ex);
  }
}
