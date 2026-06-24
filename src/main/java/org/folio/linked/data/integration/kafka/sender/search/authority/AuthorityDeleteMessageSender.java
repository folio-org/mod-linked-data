package org.folio.linked.data.integration.kafka.sender.search.authority;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.DELETE;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.integration.kafka.sender.DeleteMessageSender;
import org.folio.linked.data.mapper.kafka.search.AuthoritySearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class AuthorityDeleteMessageSender implements DeleteMessageSender {
  @Qualifier("authorityIndexMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> authorityIndexMessageProducer;
  private final AuthoritySearchMessageMapper mapper;

  @Override
  public void accept(Resource resource) {
    log.debug("Publishing Index delete message for AUTHORITY with ID [{}]", resource.getId());
    var onlyIdResource = new Resource().setIdAndRefreshEdges(resource.getId());
    var indexMessage = mapper.toIndex(onlyIdResource, DELETE);
    authorityIndexMessageProducer.sendMessages(List.of(indexMessage));
  }

  @Override
  public Collection<Resource> apply(Resource resource) {
    if (resource.isNotOfType(HUB) && resource.isAuthority()) {
      return singletonList(resource);
    }
    return emptyList();
  }
}
