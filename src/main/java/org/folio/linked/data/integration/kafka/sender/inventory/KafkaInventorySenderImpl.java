package org.folio.linked.data.integration.kafka.sender.inventory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.spring.tools.config.properties.FolioEnvironment.getFolioEnvName;
import static org.folio.spring.tools.kafka.KafkaUtils.getTenantTopicName;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.folio.linked.data.mapper.kafka.KafkaInventoryMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.InstanceIngressEvent;
import org.folio.search.domain.dto.InventoryInstanceIngressEventEventMetadata;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.integration.XOkapiHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

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
    var tenantTopicName = getTenantTopicName(initialInventoryInstanceIngressTopicName, getFolioEnvName(), tenant);
    kafkaInventoryMessageMapper.toInstanceIngressPayload(resource)
      .map(p -> {
          var id = UUID.randomUUID().toString();
          var event = new InstanceIngressEvent()
            .id(id)
            .eventType(InstanceIngressEvent.EventTypeEnum.CREATE_INSTANCE)
            .eventPayload(p)
            .eventMetadata(new InventoryInstanceIngressEventEventMetadata()
              .tenantId(tenant)
            );
          var pr = new ProducerRecord<>(tenantTopicName, id, event);
          pr.headers().add(XOkapiHeaders.URL, folioExecutionContext.getOkapiUrl().getBytes(UTF_8));
          pr.headers().add(XOkapiHeaders.TOKEN, folioExecutionContext.getToken().getBytes(UTF_8));
          return inventoryKafkaTemplate.send(pr);
        }
      ).ifPresent(f -> logSending(tenantTopicName, f));
  }

  @SneakyThrows
  private void logSending(String tenantTopicName, CompletableFuture<SendResult<String, InstanceIngressEvent>> future) {
    log.info("sendInstanceCreated result to topic [{}]: [{}]", tenantTopicName, future.get().toString());
  }

}
