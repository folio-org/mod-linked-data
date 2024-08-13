package org.folio.linked.data.service.index;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.integration.kafka.sender.search.WorkCreateMessageSender;
import org.folio.linked.data.model.entity.Resource;
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
  private final WorkCreateMessageSender workCreateMessageSender;

  @Override
  public BatchIndexResult indexWorks(Stream<Resource> works) {
    var recordsIndexed = new AtomicInteger(0);
    var indexedIds = works
      .map(work -> handleResource(work, recordsIndexed))
      .flatMap(Optional::stream)
      .collect(Collectors.toSet());
    return new BatchIndexResult(recordsIndexed.get(), indexedIds);
  }

  private Optional<Long> handleResource(Resource work, AtomicInteger recordsIndexed) {
    try {
      workCreateMessageSender.acceptWithoutIndexDateUpdate(work);
      recordsIndexed.getAndIncrement();
      return Optional.ofNullable(work.getId());
    } catch (Exception ex) {
      log.warn("Failed to send work for indexing with id {}", work.getId(), ex);
      return Optional.empty();
    } finally {
      enableGarbageCollection(work);
    }
  }

  private void enableGarbageCollection(Resource resource) {
    entityManager.detach(resource);
  }
}
