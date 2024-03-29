package org.folio.linked.data.service.impl;

import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;

import jakarta.persistence.EntityManager;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.mapper.kafka.KafkaMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.BatchReindexService;
import org.folio.linked.data.service.KafkaSender;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@Profile(SEARCH_PROFILE)
@RequiredArgsConstructor
public class BatchReindexServiceImpl implements BatchReindexService {

  private final KafkaSender kafkaSender;
  private final KafkaMessageMapper kafkaMessageMapper;
  private final EntityManager entityManager;

  @Override
  public BatchReindexResult batchReindex(Page<Resource> page) {
    var recordsIndexed = new AtomicInteger(0);
    var indexedIds = page.get()
      .map(work -> {
          try {
            var id = kafkaMessageMapper.toIndex(work, CREATE)
              .map(bibframeIndex -> {
                log.info("Sending work for reindexing with id {}", bibframeIndex.getId());
                kafkaSender.sendResourceCreated(bibframeIndex, false);
                recordsIndexed.getAndIncrement();
                return work.getId();
              })
              .orElseGet(() -> {
                log.info("Work with id {} wasn't sent for reindexing, because it doesn't contain any "
                    + "indexable values. Saving with indexDate to keep it ignored in future reindexing",
                  work.getId());
                return work.getId();
              });
            // detach the resource entity from entity manager, enabling garbage collection
            entityManager.detach(work);
            return id;
          } catch (Exception ex) {
            log.warn("Failed to send work for reindexing with id {}", work.getId(), ex);
            return null;
          }
        }
      )
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
    return new BatchReindexResult(recordsIndexed.get(), indexedIds);
  }
}
