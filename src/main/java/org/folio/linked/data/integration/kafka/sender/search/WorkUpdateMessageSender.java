package org.folio.linked.data.integration.kafka.sender.search;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.UPDATE;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.ResourceUtils.extractWorkFromInstance;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.integration.kafka.sender.UpdateMessageSender;
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
public class WorkUpdateMessageSender implements UpdateMessageSender {

  @Qualifier("bibliographicMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer;
  private final WorkSearchMessageMapper workSearchMessageMapper;
  private final ApplicationEventPublisher eventPublisher;

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
    var message = workSearchMessageMapper.toIndex(resource)
      .type(UPDATE);
    bibliographicMessageProducer.sendMessages(List.of(message));
    publishIndexEvent(resource);
  }

  private List<Resource> selectParentWorkForUpdate(Resource instance) {
    return extractWorkFromInstance(instance)
      .map(work -> {
        log.info("Instance [{}] update triggered parent Work [{}] index update", instance.getId(), work.getId());
        return Collections.singletonList(work);
      })
      .orElseGet(() -> {
        log.error("Instance [id {}] updated, but parent work wasn't found!", instance.getId());
        return emptyList();
      });
  }

  private void publishIndexEvent(Resource resource) {
    Optional.of(resource)
      .map(Resource::getId)
      .map(ResourceIndexedEvent::new)
      .ifPresent(eventPublisher::publishEvent);
  }

}
