package org.folio.linked.data.integration.kafka.listener;

import static java.util.Optional.ofNullable;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_AUTHORITY;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_BIB;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.folio.linked.data.util.KafkaUtils.getHeaderValueByName;
import static org.folio.linked.data.util.KafkaUtils.handleForExistedTenant;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.URL;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.logging.log4j.Level;
import org.folio.linked.data.domain.dto.SourceRecordDomainEvent;
import org.folio.linked.data.domain.dto.SourceRecordType;
import org.folio.linked.data.integration.kafka.listener.handler.srs.SourceRecordDomainEventHandler;
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
public class SourceRecordDomainEventListener {

  private static final String SRS_DOMAIN_EVENT_LISTENER = "mod-linked-data-source-record-domain-event-listener";
  private static final String RECORD_TYPE = "folio.srs.recordType";
  private static final Set<String> REQUIRED_HEADERS = Set.of(TENANT, URL, RECORD_TYPE);
  private final TenantScopedExecutionService tenantScopedExecutionService;
  private final SourceRecordDomainEventHandler sourceRecordDomainEventHandler;
  private final LinkedDataTenantService linkedDataTenantService;

  @KafkaListener(
    id = SRS_DOMAIN_EVENT_LISTENER,
    containerFactory = "srsEventListenerContainerFactory",
    groupId = "#{folioKafkaProperties.listener['source-record-domain-event'].groupId}",
    concurrency = "#{folioKafkaProperties.listener['source-record-domain-event'].concurrency}",
    topicPattern = "#{folioKafkaProperties.listener['source-record-domain-event'].topicPattern}")
  public void handleSourceRecordDomainEvent(List<ConsumerRecord<String, SourceRecordDomainEvent>> consumerRecords) {
    consumerRecords.forEach(consumerRecord -> {
      var event = consumerRecord.value();
      handleForExistedTenant(consumerRecord, event.getId(), linkedDataTenantService, log, this::processRecord);
    });
  }

  private void processRecord(ConsumerRecord<String, SourceRecordDomainEvent> consumerRecord) {
    log.debug("Received event [key {}]", consumerRecord.key());
    var event = consumerRecord.value();
    if (notAllRequiredHeaders(consumerRecord.headers())) {
      log.warn("Received SourceRecordDomainEvent [id {}] will be ignored since not all required headers were provided",
        event.getId());
      return;
    }
    var sourceRecordType = getHeaderValueByName(consumerRecord, RECORD_TYPE)
      .map(SourceRecordType::fromValue)
      .orElseThrow();

    if (sourceRecordType == MARC_AUTHORITY || sourceRecordType == MARC_BIB) {
      tenantScopedExecutionService.executeWithRetry(
        consumerRecord.headers(),
        retryContext -> runRetryableJob(event, sourceRecordType, retryContext),
        ex -> logFailedEvent(event, sourceRecordType, ex, false)
      );
    }
  }

  private void runRetryableJob(SourceRecordDomainEvent event, SourceRecordType type, RetryContext context) {
    ofNullable(context.getLastThrowable())
      .ifPresent(ex -> logFailedEvent(event, type, ex, true));
    sourceRecordDomainEventHandler.handle(event, type);
  }

  private void logFailedEvent(SourceRecordDomainEvent event, SourceRecordType type, Throwable ex, boolean isRetrying) {
    var marcRecord = event.getEventPayload();
    var logLevel = isRetrying ? Level.INFO : Level.ERROR;
    log.log(logLevel, "Failed to process MARC {} record {}. Retrying: {}", type.name(), marcRecord, isRetrying, ex);
  }

  private boolean notAllRequiredHeaders(Headers headers) {
    return !REQUIRED_HEADERS.stream()
      .map(required -> headers.headers(required).iterator())
      .allMatch(iterator -> iterator.hasNext() && iterator.next().value().length > 0);
  }
}
