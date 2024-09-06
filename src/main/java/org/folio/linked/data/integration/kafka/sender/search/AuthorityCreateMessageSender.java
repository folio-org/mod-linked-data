package org.folio.linked.data.integration.kafka.sender.search;

import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.integration.kafka.sender.CreateMessageSender;
import org.folio.linked.data.mapper.kafka.search.AuthoritySearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.marc4ld.util.ResourceKind;
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
  private final AuthoritySearchMessageMapper authoritySearchMessageMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public Collection<Resource> apply(Resource resource) {
    return findAuthorities(resource, new HashSet<>());
  }

  private Set<Resource> findAuthorities(Resource resource, Set<Resource> visitedResources) {
    if (visitedResources.contains(resource)) {
      return Collections.emptySet();
    }
    var authorities = new HashSet<Resource>();
    if (isNewAuthority(resource)) {
      authorities.add(resource);
    }
    visitedResources.add(resource);
    resource.getOutgoingEdges().stream()
      .map(ResourceEdge::getTarget)
      .filter(target -> isNewAndNotVisited(target, visitedResources))
      .flatMap(target -> findAuthorities(target, visitedResources).stream())
      .forEach(authorities::add);
    return authorities;
  }

  private boolean isNewAndNotVisited(Resource resource, Set<Resource> visited) {
    return resource.getIndexDate() == null && !visited.contains(resource);
  }

  private boolean isNewAuthority(Resource resource) {
    return resource.getIndexDate() == null && isAuthority(resource);
  }

  private boolean isAuthority(Resource resource) {
    return ResourceKind.AUTHORITY
      .stream()
      .anyMatch(resource::isOfType);
  }

  @Override
  public void accept(Resource resource) {
    var message = authoritySearchMessageMapper.toIndex(resource)
      .type(CREATE);
    authorityMessageProducer.sendMessages(List.of(message));
    publishIndexEvent(resource);
  }

  private void publishIndexEvent(Resource resource) {
    Optional.of(resource)
      .map(Resource::getId)
      .map(ResourceIndexedEvent::new)
      .ifPresent(eventPublisher::publishEvent);
  }

}
