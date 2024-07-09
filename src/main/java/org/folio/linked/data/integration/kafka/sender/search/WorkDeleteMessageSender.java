package org.folio.linked.data.integration.kafka.sender.search;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.search.domain.dto.ResourceIndexEventType.DELETE;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.integration.kafka.sender.DeleteMessageSender;
import org.folio.linked.data.mapper.kafka.search.KafkaSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.marc4ld.util.ResourceKind;
import org.folio.search.domain.dto.LinkedDataWork;
import org.folio.search.domain.dto.ResourceIndexEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class WorkDeleteMessageSender implements DeleteMessageSender {

  @Qualifier("bibliographicMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer;
  private final KafkaSearchMessageMapper<LinkedDataWork> searchBibliographicMessageMapper;

  @Override
  public boolean test(Resource resource) {
    return ResourceKind.BIBLIOGRAPHIC
      .stream()
      .anyMatch(resource::isOfType);
  }

  @Override
  public void accept(Resource resource) {
    searchBibliographicMessageMapper.toDeleteIndexId(resource)
      .map(Object::toString)
      .map(this::getDeleteIndexEvent)
      .map(List::of)
      .ifPresent(bibliographicMessageProducer::sendMessages);
  }

  private ResourceIndexEvent getDeleteIndexEvent(String id) {
    return new ResourceIndexEvent()
      .id(UUID.randomUUID().toString())
      .type(DELETE)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(new LinkedDataWork(id));
  }
}
