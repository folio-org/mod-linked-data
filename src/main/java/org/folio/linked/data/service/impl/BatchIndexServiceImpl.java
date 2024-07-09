package org.folio.linked.data.service.impl;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.search.domain.dto.ResourceIndexEventType.CREATE;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.mapper.kafka.search.KafkaSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.BatchIndexService;
import org.folio.search.domain.dto.LinkedDataWork;
import org.folio.search.domain.dto.ResourceIndexEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional(readOnly = true)
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class BatchIndexServiceImpl implements BatchIndexService {

  private final EntityManager entityManager;
  private final KafkaSearchMessageMapper<LinkedDataWork> searchBibliographicMessageMapper;
  @Qualifier("bibliographicMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer;

  @Override
  public BatchIndexResult index(Stream<Resource> resources) {
    var recordsIndexed = new AtomicInteger(0);
    var indexedIds = resources
      .map(resource -> handleResource(resource, recordsIndexed))
      .flatMap(Optional::stream)
      .collect(Collectors.toSet());
    return new BatchIndexResult(recordsIndexed.get(), indexedIds);
  }

  private Optional<Long> handleResource(Resource resource, AtomicInteger recordsIndexed) {
    try {
      //TODO mapper mustn't decide validation problems
      searchBibliographicMessageMapper.toIndex(resource, CREATE)
        .map(this::createIndexEvent)
        .map(List::of)
        .ifPresentOrElse(
          indexList -> {
            bibliographicMessageProducer.sendMessages(indexList);
            recordsIndexed.getAndIncrement();
          },
          () -> logFailure(resource.getId())
        );
      return Optional.ofNullable(resource.getId());
    } catch (Exception ex) {
      log.warn("Failed to send resource for indexing with id {}", resource.getId(), ex);
      return Optional.empty();
    } finally {
      enableGarbageCollection(resource);
    }
  }

  private ResourceIndexEvent createIndexEvent(LinkedDataWork index) {
    return new ResourceIndexEvent()
      .id(UUID.randomUUID().toString())
      .type(CREATE)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(index);
  }

  private void logFailure(Long resourceId) {
    log.info("Resource with id {} wasn't sent for indexing, because it doesn't contain any "
      + "indexable values. Saving with indexDate to keep it ignored in future indexing", resourceId);
  }

  private void enableGarbageCollection(Resource resource) {
    entityManager.detach(resource);
  }
}
