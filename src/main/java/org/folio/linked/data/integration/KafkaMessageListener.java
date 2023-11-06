package org.folio.linked.data.integration;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.folio.linked.data.integration.consumer.DataImportEventHandler;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.folio.search.domain.dto.DataImportEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class KafkaMessageListener {

  private static final String DI_INSTANCE_CREATED_LISTENER = "mod-linked-data-data-import-instance-created-listener";
  private final TenantScopedExecutionService tenantScopedExecutionService;
  private final DataImportEventHandler dataImportEventHandler;

  @KafkaListener(
    id = DI_INSTANCE_CREATED_LISTENER,
    containerFactory = "dataImportListenerContainerFactory",
    groupId = "#{folioKafkaProperties.listener['data-import-instance-create'].groupId}",
    concurrency = "#{folioKafkaProperties.listener['data-import-instance-create'].concurrency}",
    topicPattern = "#{folioKafkaProperties.listener['data-import-instance-create'].topicPattern}")
  public void handleDataImportInstanceCreatedEvent(List<ConsumerRecord<String, DataImportEvent>> consumerRecords) {
    for (ConsumerRecord<String, DataImportEvent> consumerRecord : consumerRecords) {
      log.info("Received: {}", consumerRecord);
      var event = consumerRecord.value();
      if (isNotBlank(event.getTenant())) {
        tenantScopedExecutionService.executeAsyncTenantScoped(event.getTenant(),
          () -> dataImportEventHandler.handle(event));
      } else {
        log.warn("Received DataImportEvent with no TenantId: {}", event);
      }
    }
  }
}
