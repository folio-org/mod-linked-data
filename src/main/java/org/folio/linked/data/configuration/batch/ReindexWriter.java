package org.folio.linked.data.configuration.batch;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.folio.linked.data.util.Constants.SEARCH_HUB_RESOURCE_NAME;
import static org.folio.linked.data.util.Constants.SEARCH_WORK_RESOURCE_NAME;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.LinkedDataHub;
import org.folio.linked.data.domain.dto.LinkedDataWork;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.service.resource.ResourceService;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
@StepScope
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class ReindexWriter implements ItemWriter<ResourceIndexEvent> {

  private final ResourceService resourceService;
  @Qualifier("hubIndexMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> hubIndexMessageProducer;
  @Qualifier("workIndexMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> workIndexMessageProducer;

  @Override
  @Transactional(propagation = REQUIRES_NEW)
  public void write(@NonNull Chunk<? extends ResourceIndexEvent> chunk) {
    hubIndexMessageProducer.sendMessages(getFilteredEvents(chunk, SEARCH_HUB_RESOURCE_NAME));
    workIndexMessageProducer.sendMessages(getFilteredEvents(chunk, SEARCH_WORK_RESOURCE_NAME));
    resourceService.updateIndexDateBatch(getIds(chunk));
  }

  private Set<Long> getIds(Chunk<? extends ResourceIndexEvent> chunk) {
    return chunk.getItems()
      .stream()
      .map(rie -> {
        if (rie.getNew() instanceof LinkedDataWork ldw) {
          return ldw.getId();
        }
        if (rie.getNew() instanceof LinkedDataHub ldh) {
          return ldh.getId();
        }
        return null;
      })
      .filter(Objects::nonNull)
      .map(Long::valueOf)
      .collect(toSet());
  }

  private List<ResourceIndexEvent> getFilteredEvents(Chunk<? extends ResourceIndexEvent> chunk, String resourceName) {
    return chunk.getItems()
      .stream()
      .filter(rie -> resourceName.equals(rie.getResourceName()))
      .distinct()
      .collect(toList());
  }

}
