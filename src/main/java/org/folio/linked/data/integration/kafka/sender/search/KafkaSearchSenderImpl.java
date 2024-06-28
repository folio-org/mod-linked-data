package org.folio.linked.data.integration.kafka.sender.search;

import static java.lang.Long.parseLong;
import static org.folio.linked.data.util.BibframeUtils.isSameResource;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_AUTHORITY_RESOURCE_NAME;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.search.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.search.domain.dto.ResourceIndexEventType.DELETE;
import static org.folio.search.domain.dto.ResourceIndexEventType.UPDATE;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.mapper.kafka.KafkaSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.search.domain.dto.BibframeAuthorityIndex;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.ResourceIndexEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class KafkaSearchSenderImpl implements KafkaSearchSender {

  private final ApplicationEventPublisher eventPublisher;
  private final KafkaSearchMessageMapper<BibframeIndex> searchBibliographicMessageMapper;
  private final KafkaSearchMessageMapper<BibframeAuthorityIndex> searchAuthorityMessageMapper;
  @Qualifier("bibliographicIndexEventProducer")
  private final FolioMessageProducer<ResourceIndexEvent> bibliographicIndexEventProducer;
  @Qualifier("authorityIndexEventProducer")
  private final FolioMessageProducer<ResourceIndexEvent> authorityIndexEventProducer;

  @Override
  public void sendWorkCreated(Resource resource) {
    searchBibliographicMessageMapper.toIndex(resource, CREATE).ifPresent(bibframeIndex -> {
      sendIndexCreate(bibframeIndex);
      publishIndexEvent(bibframeIndex.getId());
    });
  }

  @Override
  public boolean sendMultipleWorksCreated(Resource resource) {
    return searchBibliographicMessageMapper.toIndex(resource, CREATE)
      .map(bibframeIndex -> {
        sendIndexCreate(bibframeIndex);
        return true;
      })
      .isPresent();
  }

  @Override
  public void sendWorkUpdated(Resource newResource, Resource oldResource) {
    searchBibliographicMessageMapper.toIndex(newResource, UPDATE).ifPresentOrElse(
      newWorkIndex -> indexUpdatedWork(newWorkIndex, oldResource),
      () -> {
        sendWorkDeleted(oldResource);
        log.info("Updated Work [{}] is not indexable anymore, sending DELETE event", oldResource.getId());
      }
    );
  }

  @Override
  public void sendWorkDeleted(Resource resource) {
    searchBibliographicMessageMapper.toDeleteIndexId(resource).ifPresent(this::sendDelete);
  }

  @Override
  public void sendAuthorityCreated(Resource resource) {
    searchAuthorityMessageMapper.toIndex(resource, CREATE).ifPresent(authorityIndex -> {
      sendAuthorityIndexCreate(authorityIndex);
      publishIndexEvent(authorityIndex.getId());
    });
  }

  private void indexUpdatedWork(BibframeIndex newWorkIndex, Resource oldWork) {
    if (isSameResource(newWorkIndex, oldWork)) {
      var oldWorkIndex = searchBibliographicMessageMapper.toIndex(oldWork, UPDATE).orElse(null);
      log.info("Updated Work [{}] has the same id as before update, sending UPDATE event", newWorkIndex.getId());
      sendUpdate(newWorkIndex, oldWorkIndex);
    } else {
      log.info("Updated Work [{}] has another id than before update ({}), sending DELETE and CREATE events",
        newWorkIndex.getId(), oldWork.getId());
      sendWorkDeleted(oldWork);
      sendIndexCreate(newWorkIndex);
      publishIndexEvent(newWorkIndex.getId());
    }
  }

  private void sendIndexCreate(BibframeIndex index) {
    var message = new ResourceIndexEvent()
      .id(UUID.randomUUID().toString())
      .type(CREATE)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(index);
    bibliographicIndexEventProducer.sendMessages(List.of(message));
  }

  private void sendAuthorityIndexCreate(BibframeAuthorityIndex index) {
    var message = new ResourceIndexEvent()
      .id(UUID.randomUUID().toString())
      .type(CREATE)
      .resourceName(SEARCH_AUTHORITY_RESOURCE_NAME)
      ._new(index);
    authorityIndexEventProducer.sendMessages(List.of(message));
  }

  private void publishIndexEvent(@NotNull String id) {
    eventPublisher.publishEvent(new ResourceIndexedEvent(Long.parseLong(id)));
  }

  private void sendUpdate(BibframeIndex newWorkIndex, BibframeIndex oldWorkIndex) {
    var message = new ResourceIndexEvent()
      .id(UUID.randomUUID().toString())
      .type(UPDATE)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(newWorkIndex)
      .old(oldWorkIndex);
    bibliographicIndexEventProducer.sendMessages(List.of(message));
    eventPublisher.publishEvent(new ResourceIndexedEvent(parseLong(newWorkIndex.getId())));
  }

  private void sendDelete(Long id) {
    var message = new ResourceIndexEvent()
      .id(UUID.randomUUID().toString())
      .type(DELETE)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(new BibframeIndex(id.toString()));
    bibliographicIndexEventProducer.sendMessages(List.of(message));
  }

}
