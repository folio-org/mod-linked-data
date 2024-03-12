package org.folio.linked.data.integration.kafka;

import static java.lang.Long.parseLong;
import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.spring.tools.config.properties.FolioEnvironment.getFolioEnvName;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.linked.data.service.KafkaSender;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.ResourceEvent;
import org.folio.search.domain.dto.ResourceEventType;
import org.folio.spring.FolioExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Profile(SEARCH_PROFILE)
@RequiredArgsConstructor
public class KafkaSenderFolio implements KafkaSender {

  private final KafkaTemplate<String, ResourceEvent> kafkaTemplate;
  private final FolioExecutionContext folioExecutionContext;
  private final ApplicationEventPublisher eventPublisher;
  @Value("${mod-linked-data.kafka.topic.bibframe-index}")
  private String initialBibframeIndexTopicName;

  @SneakyThrows
  @Override
  public void sendResourceCreated(BibframeIndex bibframeIndex, boolean isSingle) {
    var tenant = folioExecutionContext.getTenantId();
    var tenantTopicName = getTenantTopicName(tenant);
    var future = kafkaTemplate.send(tenantTopicName, bibframeIndex.getId(),
      new ResourceEvent()
        .id(bibframeIndex.getId())
        .type(ResourceEventType.CREATE)
        .tenant(tenant)
        .resourceName(SEARCH_RESOURCE_NAME)
        ._new(bibframeIndex)
    );
    if (isSingle) {
      future.thenRun(() -> eventPublisher.publishEvent(new ResourceIndexedEvent(parseLong(bibframeIndex.getId()))));
    }
    log.info("sendResourceCreated result to topic [{}]: [{}]", tenantTopicName, future.get().toString());
  }

  @SneakyThrows
  @Override
  public void sendResourceUpdated(BibframeIndex newBibframeIndex, BibframeIndex oldBibframeIndex) {
    var tenant = folioExecutionContext.getTenantId();
    var tenantTopicName = getTenantTopicName(tenant);
    var future = kafkaTemplate.send(tenantTopicName, newBibframeIndex.getId(),
      new ResourceEvent()
        .id(newBibframeIndex.getId())
        .type(ResourceEventType.UPDATE)
        .tenant(tenant)
        .resourceName(SEARCH_RESOURCE_NAME)
        ._new(newBibframeIndex)
        .old(oldBibframeIndex)
    );
    future.thenRun(() -> eventPublisher.publishEvent(new ResourceIndexedEvent(parseLong(newBibframeIndex.getId()))));
    log.info("sendResourceUpdated result to topic [{}]: [{}]", tenantTopicName, future.get().toString());
  }

  @SneakyThrows
  @Override
  public void sendResourceDeleted(Long id) {
    var tenant = folioExecutionContext.getTenantId();
    var tenantTopicName = getTenantTopicName(tenant);
    var future = kafkaTemplate.send(tenantTopicName, id.toString(),
      new ResourceEvent()
        .id(id.toString())
        .type(ResourceEventType.DELETE)
        .tenant(tenant)
        .resourceName(SEARCH_RESOURCE_NAME)
        ._new(new BibframeIndex(id.toString()))
    );
    log.info("sendResourceDeleted result to topic [{}]: [{}]", tenantTopicName, future.get().toString());
  }

  private String getTenantTopicName(String tenantId) {
    return String.format("%s.%s.%s", getFolioEnvName(), tenantId, initialBibframeIndexTopicName);
  }
}
