package org.folio.linked.data.integration.kafka.sender.search;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.folio.linked.data.util.ResourceUtils.extractWorkFromInstance;
import static org.folio.linked.data.util.ResourceUtils.isNonLightResourceOfType;

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
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class WorkCreateMessageSender implements CreateMessageSender {

  @Qualifier("workIndexMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> workIndexMessageProducer;
  private final WorkSearchMessageMapper workSearchMessageMapper;
  private final ApplicationEventPublisher eventPublisher;
  private final WorkUpdateMessageSender workUpdateMessageSender;

  @Override
  public Collection<Resource> apply(Resource resource) {
    if (isNonLightResourceOfType(resource, WORK)) {
      return singletonList(resource);
    }
    if (isNonLightResourceOfType(resource, INSTANCE)) {
      triggerParentWorkUpdate(resource);
    }
    return emptyList();
  }

  private void triggerParentWorkUpdate(Resource instance) {
    extractWorkFromInstance(instance)
      .ifPresentOrElse(work -> {
        log.debug("Instance [id {}] creation triggered parent Work [id {}] index update",
          instance.getId(), work.getId());
        workUpdateMessageSender.produce(work);
      }, () -> log.error("Instance [id {}] created, but parent work wasn't found!", instance.getId()));
  }

  @Override
  public void accept(Resource resource) {
    log.debug("Publishing Index create message for work with ID [{}]", resource.getId());
    var message = workSearchMessageMapper.toIndex(resource, CREATE);
    workIndexMessageProducer.sendMessages(List.of(message));
    publishIndexEvent(resource);
  }

  private void publishIndexEvent(Resource resource) {
    Optional.of(resource)
      .map(Resource::getId)
      .map(ResourceIndexedEvent::new)
      .ifPresent(eventPublisher::publishEvent);
  }
}
