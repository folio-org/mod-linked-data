package org.folio.linked.data.service.impl;

import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;

import jakarta.persistence.EntityManager;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.mapper.kafka.KafkaMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.BatchIndexService;
import org.folio.linked.data.service.KafkaSender;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@Profile(SEARCH_PROFILE)
@RequiredArgsConstructor
public class BatchIndexServiceImpl implements BatchIndexService {

  private final KafkaSender kafkaSender;
  private final KafkaMessageMapper kafkaMessageMapper;
  private final EntityManager entityManager;

  @Override
  public BatchIndexResult index(Stream<Resource> resources) {
    var recordsIndexed = new AtomicInteger(0);
    var indexedIds = resources
      .map(resource -> {
        try {
          var id = kafkaMessageMapper.toIndex(resource, CREATE)
            .map(bibframeIndex -> {
              log.info("Sending resource for indexing with id {}", bibframeIndex.getId());
              kafkaSender.sendResourceCreated(bibframeIndex, false);
              recordsIndexed.getAndIncrement();
              return resource.getId();
            })
            .orElseGet(() -> {
              log.info("Resource with id {} wasn't sent for indexing, because it doesn't contain any "
                  + "indexable values. Saving with indexDate to keep it ignored in future indexing",
                resource.getId());
              return resource.getId();
            });
          // detach the resource entity from entity manager, enabling garbage collection
          entityManager.detach(resource);
          return id;
        } catch (Exception ex) {
          log.warn("Failed to send resource for indexing with id {}", resource.getId(), ex);
          return null;
        }
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
    return new BatchIndexResult(recordsIndexed.get(), indexedIds);
  }
}
