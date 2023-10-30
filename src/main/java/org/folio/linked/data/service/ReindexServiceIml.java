package org.folio.linked.data.service;

import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;

import jakarta.transaction.Transactional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.mapper.resource.kafka.KafkaMessageMapper;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Log4j2
@Service
@Transactional
@Profile(SEARCH_PROFILE)
@RequiredArgsConstructor
public class ReindexServiceIml implements ReindexService {

  private final ResourceRepository resourceRepository;
  private final KafkaSender kafkaSender;
  private final KafkaMessageMapper kafkaMessageMapper;
  @Value("${mod-linked-data.reindex.page-size}")
  private String reindexPageSize;

  @Async
  @Override
  public void reindex() {
    Pageable pageable = PageRequest.of(0, Integer.parseInt(reindexPageSize), Sort.by("resourceHash"));
    AtomicLong recordsIndexed  = new AtomicLong(0);
    while (pageable.isPaged()) {
      var page = resourceRepository.findResourcesByTypeFull(Set.of(ResourceTypeDictionary.INSTANCE.getUri()), pageable);
      page.get()
        .forEach(resource -> {
            try {
              var bibframeIndex = kafkaMessageMapper.toIndex(resource);
              kafkaSender.sendResourceCreated(bibframeIndex);
              log.info("Sending resource for reindexing with id {}", bibframeIndex.getId());
              recordsIndexed.getAndIncrement();
            } catch (Exception e) {
              log.warn("Failed to send resource for reindexing with id {}", resource.getResourceHash());
            }
          }
        );
      pageable = page.nextPageable();
    }
    log.info("Reindexing finished. {} records indexed", recordsIndexed.get());
  }
}
