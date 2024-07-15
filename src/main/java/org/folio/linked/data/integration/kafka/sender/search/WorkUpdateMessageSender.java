package org.folio.linked.data.integration.kafka.sender.search;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.BibframeUtils.extractWork;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.search.domain.dto.ResourceIndexEventType.UPDATE;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.integration.ResourceModificationEventListener;
import org.folio.linked.data.integration.kafka.sender.UpdateMessageSender;
import org.folio.linked.data.mapper.kafka.search.KafkaSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.search.domain.dto.LinkedDataWork;
import org.folio.search.domain.dto.ResourceIndexEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Profile(FOLIO_PROFILE)
public class WorkUpdateMessageSender implements UpdateMessageSender {

  @Qualifier("bibliographicMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer;
  private final KafkaSearchMessageMapper<LinkedDataWork> searchBibliographicMessageMapper;
  private final ResourceModificationEventListener eventListener;

  public WorkUpdateMessageSender(FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer,
                                 KafkaSearchMessageMapper<LinkedDataWork> searchBibliographicMessageMapper,
                                 @Lazy ResourceModificationEventListener eventListener) {
    this.bibliographicMessageProducer = bibliographicMessageProducer;
    this.searchBibliographicMessageMapper = searchBibliographicMessageMapper;
    this.eventListener = eventListener;
  }

  @Override
  public Collection<Resource> apply(Resource resource) {
    if (resource.isOfType(WORK)) {
      return singletonList(resource);
    }
    if (resource.isOfType(INSTANCE)) {
      return selectParentWorkForUpdate(resource);
    }
    return emptyList();
  }

  @Override
  public void accept(Resource resource) {
    searchBibliographicMessageMapper.toIndex(resource, UPDATE)
      .map(this::getUpdateIndexEvent)
      .ifPresent(
        indexEvent -> {
          bibliographicMessageProducer.sendMessages(List.of(indexEvent));
          publishIndexEvent(resource);
        }
      );
  }

  private List<Resource> selectParentWorkForUpdate(Resource instance) {
    return extractWork(instance)
      .map(work -> {
        log.info("Instance [{}] update triggered parent Work [{}] index update", instance.getId(), work.getId());
        return Collections.singletonList(work);
      })
      .orElseGet(() -> {
        log.error("Instance [id {}] updated, but parent work wasn't found!", instance.getId());
        return emptyList();
      });
  }

  private ResourceIndexEvent getUpdateIndexEvent(LinkedDataWork linkedDataWork) {
    return new ResourceIndexEvent()
      .id(linkedDataWork.getId())
      .type(UPDATE)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(linkedDataWork);
  }

  private void publishIndexEvent(Resource resource) {
    Optional.of(resource)
      .map(Resource::getId)
      .map(ResourceIndexedEvent::new)
      .ifPresent(eventListener::afterIndex);
  }

}
