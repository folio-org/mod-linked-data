package org.folio.linked.data.integration.kafka.sender.search;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.linked.data.util.BibframeUtils.extractWorkFromInstance;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.integration.kafka.sender.CreateMessageSender;
import org.folio.linked.data.mapper.kafka.search.WorkSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class WorkCreateMessageSender implements CreateMessageSender {

  @Qualifier("bibliographicMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer;
  private final WorkSearchMessageMapper workSearchMessageMapper;
  private final ApplicationEventPublisher eventPublisher;
  private final WorkUpdateMessageSender workUpdateMessageSender;

  @Override
  public Collection<Resource> apply(Resource resource) {
    if (resource.isOfType(WORK)) {
      return singletonList(resource);
    }
    if (resource.isOfType(INSTANCE)) {
      triggerParentWorkUpdate(resource);
    }
    return emptyList();
  }

  private void triggerParentWorkUpdate(Resource instance) {
    extractWorkFromInstance(instance)
      .ifPresentOrElse(work -> {
        log.info("Instance [id {}] creation triggered parent Work [id {}] index update",
          instance.getId(), work.getId());
        workUpdateMessageSender.produce(work);
      }, () -> log.error("Instance [id {}] created, but parent work wasn't found!", instance.getId()));
  }

  public void acceptWithoutIndexDateUpdate(Resource resource) {
    this.accept(resource, FALSE);
  }

  @Override
  public void accept(Resource resource) {
    this.accept(resource, TRUE);
  }

  private void accept(Resource resource, Boolean putIndexDate) {
    var message = workSearchMessageMapper.toIndex(resource)
      .type(CREATE);
    bibliographicMessageProducer.sendMessages(List.of(message));
    if (TRUE.equals(putIndexDate)) {
      publishIndexEvent(resource);
    }
  }

  private void publishIndexEvent(Resource resource) {
    Optional.of(resource)
      .map(Resource::getId)
      .map(ResourceIndexedEvent::new)
      .ifPresent(eventPublisher::publishEvent);
  }
}
