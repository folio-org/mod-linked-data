package org.folio.linked.data.integration.kafka.sender.search;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.UPDATE;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.folio.linked.data.util.ResourceUtils.extractWorkFromInstance;
import static org.folio.linked.data.util.ResourceUtils.isNonLightResourceOfType;

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
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class WorkUpdateMessageSender implements UpdateMessageSender {

  @Qualifier("workIndexMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> workIndexMessageProducer;
  private final WorkSearchMessageMapper workSearchMessageMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public Collection<Resource> apply(Resource resource) {
    if (isNonLightResourceOfType(resource, WORK)) {
      return singletonList(resource);
    }
    if (isNonLightResourceOfType(resource, INSTANCE)) {
      return selectParentWorkForUpdate(resource);
    }
    return emptyList();
  }

  @Override
  public void accept(Resource resource) {
    var message = workSearchMessageMapper.toIndex(resource, UPDATE);
    workIndexMessageProducer.sendMessages(List.of(message));
    publishIndexEvent(resource);
  }

  private List<Resource> selectParentWorkForUpdate(Resource instance) {
    return extractWorkFromInstance(instance)
      .map(work -> {
        log.debug("Instance [{}] update triggered parent Work [{}] index update", instance.getId(), work.getId());
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
