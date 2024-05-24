package org.folio.linked.data.integration.kafka.sender.inventory;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.mapper.kafka.KafkaInventoryMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.InstanceIngressEvent;
import org.folio.spring.FolioExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.spring.tools.kafka.KafkaUtils.getTenantTopicName;

@Log4j2
@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class KafkaInventorySenderImpl implements KafkaInventorySender {

  private final KafkaTemplate<String, InstanceIngressEvent> inventoryKafkaTemplate;
  private final FolioExecutionContext folioExecutionContext;
  private final KafkaInventoryMessageMapper kafkaInventoryMessageMapper;
  @Value("${mod-linked-data.kafka.topic.inventory.instance-ingress}")
  private String initialInventoryInstanceIngressTopicName;

  @Override
  @SneakyThrows
  public void sendInstanceCreated(Resource resource) {
    var tenant = folioExecutionContext.getTenantId();
    var tenantTopicName = getTenantTopicName(tenant, initialInventoryInstanceIngressTopicName);
    kafkaInventoryMessageMapper.toInstanceIngressPayload(resource)
      .map(p -> {
          String id = UUID.randomUUID().toString();
          return inventoryKafkaTemplate.send(tenantTopicName, id,
            new InstanceIngressEvent()
              .id(id)
              .eventType(InstanceIngressEvent.EventTypeEnum.CREATE_INSTANCE)
              .eventPayload(p));
        }
      ).ifPresent(f -> logSending(tenantTopicName, f));
  }

  @SneakyThrows
  private void logSending(String tenantTopicName, CompletableFuture<SendResult<String, InstanceIngressEvent>> future) {
    log.info("sendInstanceCreated result to topic [{}]: [{}]", tenantTopicName, future.get().toString());
  }

}
