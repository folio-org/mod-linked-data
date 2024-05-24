package org.folio.linked.data.integration.kafka;

import static java.lang.Long.parseLong;
import static org.folio.linked.data.util.BibframeUtils.isSameResource;
import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;
import static org.folio.search.domain.dto.ResourceEventType.DELETE;
import static org.folio.search.domain.dto.ResourceEventType.UPDATE;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.mapper.kafka.KafkaMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.linked.data.service.KafkaSender;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.ResourceEvent;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.tools.kafka.KafkaUtils;
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
  private final KafkaMessageMapper kafkaMessageMapper;
  @Value("${mod-linked-data.kafka.topic.bibframe-index}")
  private String initialBibframeIndexTopicName;

  @SneakyThrows
  @Override
  public void sendSingleResourceCreated(Resource resource) {
    kafkaMessageMapper.toIndex(resource, CREATE)
      .ifPresent(bibframeIndex -> sendCreate(bibframeIndex, true));
  }

  @Override
  public boolean sendMultipleResourceCreated(Resource resource) {
    return kafkaMessageMapper.toIndex(resource, CREATE)
      .map(bibframeIndex -> {
        sendCreate(bibframeIndex, false);
        return true;
      })
      .isPresent();
  }

  @SneakyThrows
  private void sendCreate(BibframeIndex bibframeIndex, boolean publishIndexEvent) {
    var tenant = folioExecutionContext.getTenantId();
    var tenantTopicName = getTenantTopicName(tenant);
    var future = kafkaTemplate.send(tenantTopicName, bibframeIndex.getId(),
      new ResourceEvent()
        .id(bibframeIndex.getId())
        .type(CREATE)
        .tenant(tenant)
        .resourceName(SEARCH_RESOURCE_NAME)
        ._new(bibframeIndex)
    );
    if (publishIndexEvent) {
      future.thenRun(() -> eventPublisher.publishEvent(new ResourceIndexedEvent(parseLong(bibframeIndex.getId()))));
    }
    log.info("sendResourceCreated result to topic [{}]: [{}]", tenantTopicName, future.get().toString());
  }

  @Override
  public void sendResourceUpdated(Resource newResource, Resource oldResource) {
    kafkaMessageMapper.toIndex(newResource, UPDATE).ifPresentOrElse(
      newWorkIndex -> indexUpdatedWork(newWorkIndex, oldResource),
      () -> {
        sendResourceDeleted(oldResource);
        log.info("Updated Work [{}] is not indexable anymore, sending DELETE event", oldResource.getId());
      }
    );
  }

  private void indexUpdatedWork(BibframeIndex newWorkIndex, Resource oldWork) {
    if (isSameResource(newWorkIndex, oldWork)) {
      var oldWorkIndex = kafkaMessageMapper.toIndex(oldWork, UPDATE).orElse(null);
      sendUpdate(newWorkIndex, oldWorkIndex);
      log.info("Updated Work [{}] has the same id as before update, sending UPDATE event", newWorkIndex.getId());
    } else {
      sendResourceDeleted(oldWork);
      sendCreate(newWorkIndex, true);
      log.info("Updated Work [{}] has another id than before update ({}), sending DELETE and CREATE events",
        newWorkIndex.getId(), oldWork.getId());
    }
  }

  @SneakyThrows
  private void sendUpdate(BibframeIndex newWorkIndex, BibframeIndex oldWorkIndex) {
    var tenant = folioExecutionContext.getTenantId();
    var tenantTopicName = getTenantTopicName(tenant);
    var future = kafkaTemplate.send(tenantTopicName, newWorkIndex.getId(),
      new ResourceEvent()
        .id(newWorkIndex.getId())
        .type(UPDATE)
        .tenant(tenant)
        .resourceName(SEARCH_RESOURCE_NAME)
        ._new(newWorkIndex)
        .old(oldWorkIndex)
    );
    future.thenRun(() -> eventPublisher.publishEvent(new ResourceIndexedEvent(parseLong(newWorkIndex.getId()))));
    log.info("sendResourceUpdated result to topic [{}]: [{}]", tenantTopicName, future.get().toString());
  }

  @Override
  public void sendResourceDeleted(Resource resource) {
    kafkaMessageMapper.toDeleteIndexId(resource).ifPresent(this::sendDelete);
  }

  @SneakyThrows
  private void sendDelete(Long id) {
    var tenant = folioExecutionContext.getTenantId();
    var tenantTopicName = getTenantTopicName(tenant);
    var future = kafkaTemplate.send(tenantTopicName, id.toString(),
      new ResourceEvent()
        .id(id.toString())
        .type(DELETE)
        .tenant(tenant)
        .resourceName(SEARCH_RESOURCE_NAME)
        ._new(new BibframeIndex(id.toString()))
    );
    log.info("sendResourceDeleted result to topic [{}]: [{}]", tenantTopicName, future.get().toString());
  }

  private String getTenantTopicName(String tenantId) {
    return KafkaUtils.getTenantTopicName(initialBibframeIndexTopicName, tenantId);
  }

}
