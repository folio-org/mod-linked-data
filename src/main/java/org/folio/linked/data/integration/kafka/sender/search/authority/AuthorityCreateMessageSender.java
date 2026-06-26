package org.folio.linked.data.integration.kafka.sender.search.authority;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.integration.kafka.sender.CreateMessageSender;
import org.folio.linked.data.mapper.kafka.search.AuthoritySearchMessageMapper;
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
public class AuthorityCreateMessageSender implements CreateMessageSender {

  private final AuthoritySearchMessageMapper mapper;
  private final ApplicationEventPublisher eventPublisher;
  @Qualifier("authorityIndexMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> authorityIndexMessageProducer;

  @Override
  public void accept(Resource resource) {
    log.debug("Publishing Index create message for AUTHORITY with ID [{}]", resource.getId());
    var message = mapper.toIndex(resource, CREATE);
    authorityIndexMessageProducer.sendMessages(singletonList(message));
    publishIndexEvent(resource);
  }

  @Override
  public Collection<Resource> apply(Resource resource) {
    if (resource.isNotOfType(HUB) && resource.isAuthority()) {
      return singletonList(resource);
    }
    return emptyList();
  }

  private void publishIndexEvent(Resource resource) {
    Optional.of(resource)
      .map(Resource::getId)
      .map(ResourceIndexedEvent::new)
      .ifPresent(eventPublisher::publishEvent);
  }
}
