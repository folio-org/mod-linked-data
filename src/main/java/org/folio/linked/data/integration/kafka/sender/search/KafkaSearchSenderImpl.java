package org.folio.linked.data.integration.kafka.sender.search;

import static java.lang.Long.parseLong;
import static org.folio.linked.data.util.BibframeUtils.isSameResource;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.search.domain.dto.SearchIndexEventType.CREATE;
import static org.folio.search.domain.dto.SearchIndexEventType.DELETE;
import static org.folio.search.domain.dto.SearchIndexEventType.UPDATE;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.mapper.kafka.KafkaSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.SearchIndexEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class KafkaSearchSenderImpl implements KafkaSearchSender {

  private final ApplicationEventPublisher eventPublisher;
  private final KafkaSearchMessageMapper kafkaSearchMessageMapper;
  private final FolioMessageProducer<SearchIndexEvent> searchIndexEventMessageProducer;

  @SneakyThrows
  @Override
  public void sendSingleResourceCreated(Resource resource) {
    kafkaSearchMessageMapper.toIndex(resource, CREATE).ifPresent(bibframeIndex -> sendCreate(bibframeIndex, true));
  }

  @Override
  public boolean sendMultipleResourceCreated(Resource resource) {
    return kafkaSearchMessageMapper.toIndex(resource, CREATE)
      .map(bibframeIndex -> {
        sendCreate(bibframeIndex, false);
        return true;
      })
      .isPresent();
  }

  @SneakyThrows
  private void sendCreate(BibframeIndex bibframeIndex, boolean publishIndexEvent) {
    var message = new SearchIndexEvent()
      .id(UUID.randomUUID().toString())
      .type(CREATE)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(bibframeIndex);
    searchIndexEventMessageProducer.sendMessages(List.of(message));
    if (publishIndexEvent) {
      eventPublisher.publishEvent(new ResourceIndexedEvent(parseLong(bibframeIndex.getId())));
    }
  }

  @Override
  public void sendResourceUpdated(Resource newResource, Resource oldResource) {
    kafkaSearchMessageMapper.toIndex(newResource, UPDATE).ifPresentOrElse(
      newWorkIndex -> indexUpdatedWork(newWorkIndex, oldResource),
      () -> {
        sendResourceDeleted(oldResource);
        log.info("Updated Work [{}] is not indexable anymore, sending DELETE event", oldResource.getId());
      }
    );
  }

  private void indexUpdatedWork(BibframeIndex newWorkIndex, Resource oldWork) {
    if (isSameResource(newWorkIndex, oldWork)) {
      var oldWorkIndex = kafkaSearchMessageMapper.toIndex(oldWork, UPDATE).orElse(null);
      log.info("Updated Work [{}] has the same id as before update, sending UPDATE event", newWorkIndex.getId());
      sendUpdate(newWorkIndex, oldWorkIndex);
    } else {
      log.info("Updated Work [{}] has another id than before update ({}), sending DELETE and CREATE events",
        newWorkIndex.getId(), oldWork.getId());
      sendResourceDeleted(oldWork);
      sendCreate(newWorkIndex, true);
    }
  }

  @SneakyThrows
  private void sendUpdate(BibframeIndex newWorkIndex, BibframeIndex oldWorkIndex) {
    var message = new SearchIndexEvent()
      .id(UUID.randomUUID().toString())
      .type(UPDATE)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(newWorkIndex)
      .old(oldWorkIndex);
    searchIndexEventMessageProducer.sendMessages(List.of(message));
    eventPublisher.publishEvent(new ResourceIndexedEvent(parseLong(newWorkIndex.getId())));
  }

  @Override
  public void sendResourceDeleted(Resource resource) {
    kafkaSearchMessageMapper.toDeleteIndexId(resource).ifPresent(this::sendDelete);
  }

  @SneakyThrows
  private void sendDelete(Long id) {
    var message = new SearchIndexEvent()
      .id(UUID.randomUUID().toString())
      .type(DELETE)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(new BibframeIndex(id.toString()));
    searchIndexEventMessageProducer.sendMessages(List.of(message));
  }

}
