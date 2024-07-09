package org.folio.linked.data.integration.kafka.sender.search;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_AUTHORITY_RESOURCE_NAME;
import static org.folio.search.domain.dto.ResourceIndexEventType.CREATE;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.integration.kafka.sender.CreateMessageSender;
import org.folio.linked.data.mapper.kafka.search.KafkaSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.marc4ld.util.ResourceKind;
import org.folio.search.domain.dto.LinkedDataAuthority;
import org.folio.search.domain.dto.ResourceIndexEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class AuthorityCreateMessageSender implements CreateMessageSender {

  @Qualifier("authorityMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> authorityMessageProducer;
  private final KafkaSearchMessageMapper<LinkedDataAuthority> searchAuthorityMessageMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public boolean test(Resource resource) {
    return ResourceKind.AUTHORITY
      .stream()
      .anyMatch(resource::isOfType);
  }

  @Override
  public void accept(Resource resource) {
    searchAuthorityMessageMapper.toIndex(resource, CREATE)
      .map(this::createIndexEvent)
      .ifPresent(authorityIndex -> {
        authorityMessageProducer.sendMessages(List.of(authorityIndex));
        publishIndexEvent(resource);
      });
  }

  private ResourceIndexEvent createIndexEvent(LinkedDataAuthority linkedDataAuthority) {
    return new ResourceIndexEvent()
      .id(UUID.randomUUID().toString())
      .type(CREATE)
      .resourceName(SEARCH_AUTHORITY_RESOURCE_NAME)
      ._new(linkedDataAuthority);
  }

  private void publishIndexEvent(Resource resource) {
    Optional.of(resource)
      .map(Resource::getId)
      .map(ResourceIndexedEvent::new)
      .ifPresent(eventPublisher::publishEvent);
  }
}
