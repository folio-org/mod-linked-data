package org.folio.linked.data.integration.kafka.sender.search;

import static java.lang.String.format;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.BibframeUtils.extractWork;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.NOT_INDEXED;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.search.domain.dto.ResourceIndexEventType.CREATE;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.integration.kafka.sender.CreateMessageSender;
import org.folio.linked.data.mapper.kafka.search.KafkaSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.marc4ld.util.ResourceKind;
import org.folio.search.domain.dto.LinkedDataWork;
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
public class WorkCreateMessageSender implements CreateMessageSender {

  @Qualifier("bibliographicMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer;
  private final KafkaSearchMessageMapper<LinkedDataWork> searchBibliographicMessageMapper;
  private final ApplicationEventPublisher eventPublisher;
  private final ResourceRepository resourceRepository;

  @Override
  public Collection<Resource> apply(Resource resource) {
    return Stream.of(resource)
      .filter(this::test)
      .toList();
  }

  //TODO refactoring to extract resources
  private boolean test(Resource resource) {
    return ResourceKind.BIBLIOGRAPHIC
      .stream()
      .anyMatch(resource::isOfType);
  }

  @Override
  public void accept(Resource resource) {
    if (resource.isOfType(WORK)) {
      sendWorkCreated(resource);
    } else {
      extractWork(resource)
        .map(Resource::getId)
        .map(resourceRepository::getReferenceById)
        .ifPresentOrElse(this::sendWorkCreated,
          () -> log.warn(format(NOT_INDEXED, resource.getId(), "created")));
    }
  }

  private void sendWorkCreated(Resource resource) {
    searchBibliographicMessageMapper.toIndex(resource, CREATE)
      .map(this::getCreateIndexEvent)
      .ifPresent(indexEvent -> {
        bibliographicMessageProducer.sendMessages(List.of(indexEvent));
        publishIndexEvent(resource);
      });
  }

  private ResourceIndexEvent getCreateIndexEvent(LinkedDataWork linkedDataWork) {
    return new ResourceIndexEvent()
      .id(UUID.randomUUID().toString())
      .type(CREATE)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(linkedDataWork);
  }

  private void publishIndexEvent(Resource resource) {
    Optional.of(resource)
      .map(Resource::getId)
      .map(ResourceIndexedEvent::new)
      .ifPresent(eventPublisher::publishEvent);
  }
}
