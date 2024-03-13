package org.folio.linked.data.service.impl;

import static java.lang.Boolean.TRUE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;

import jakarta.persistence.EntityManager;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.mapper.kafka.KafkaMessageMapper;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.KafkaSender;
import org.folio.linked.data.service.ReindexService;
import org.folio.linked.data.service.ResourceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@Profile(SEARCH_PROFILE)
@RequiredArgsConstructor
public class ReindexServiceImpl implements ReindexService {

  private final ResourceRepository resourceRepository;
  private final KafkaSender kafkaSender;
  private final KafkaMessageMapper kafkaMessageMapper;
  private final ResourceService resourceService;
  private final EntityManager entityManager;
  @Value("${mod-linked-data.reindex.page-size}")
  private String reindexPageSize;

  @Async
  @Override
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void reindex(Boolean full) {
    Pageable pageable = PageRequest.of(0, Integer.parseInt(reindexPageSize), Sort.by("resourceHash"));
    var recordsIndexed = new AtomicLong(0);
    while (pageable.isPaged()) {
      var page = TRUE.equals(full)
        ? resourceRepository.findAllByType(Set.of(WORK.getUri()), pageable)
        : resourceRepository.findNotIndexedByType(Set.of(WORK.getUri()), pageable);
      var indexedIds = page.get()
        .map(work -> {
            try {
              var id = kafkaMessageMapper.toIndex(work, CREATE)
                .map(bibframeIndex -> {
                  log.info("Sending work for reindexing with id {}", bibframeIndex.getId());
                  kafkaSender.sendResourceCreated(bibframeIndex, false);
                  recordsIndexed.getAndIncrement();
                  return work.getResourceHash();
                })
                .orElseGet(() -> {
                  log.info("Work with id {} wasn't sent for reindexing, because it doesn't contain any "
                    + "indexable values. Saving with indexDate to keep it ignored in future reindexing",
                    work.getResourceHash());
                  return work.getResourceHash();
                });
              // detach the resource entity from entity manager, enabling garbage collection
              entityManager.detach(work);
              return id;
            } catch (Exception ex) {
              log.warn("Failed to send work for reindexing with id {}", work.getResourceHash(), ex);
              return null;
            }
          }
        )
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
      resourceService.updateIndexDateBatch(indexedIds);
      pageable = page.nextPageable();
    }
    log.info("Reindexing finished. {} records indexed", recordsIndexed.get());
  }
}
