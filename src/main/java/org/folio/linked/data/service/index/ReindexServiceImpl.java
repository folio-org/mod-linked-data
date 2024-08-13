package org.folio.linked.data.service.index;

import static java.lang.Boolean.TRUE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.ResourceService;
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
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class ReindexServiceImpl implements ReindexService {

  private final ResourceRepository resourceRepository;
  private final ResourceService resourceService;
  private final BatchIndexService batchIndexService;
  @Value("${mod-linked-data.reindex.page-size}")
  private int reindexPageSize;

  @Async
  @Override
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void reindexWorks(Boolean full) {
    Pageable pageable = PageRequest.of(0, reindexPageSize, Sort.by("id"));
    var recordsIndexed = 0L;
    while (pageable.isPaged()) {
      var page = TRUE.equals(full)
        ? resourceRepository.findAllByType(Set.of(WORK.getUri()), pageable)
        : resourceRepository.findNotIndexedByType(Set.of(WORK.getUri()), pageable);
      var batchReindexResult = batchIndexService.indexWorks(page.get());
      recordsIndexed += batchReindexResult.recordsIndexed();
      resourceService.updateIndexDateBatch(batchReindexResult.indexedIds());
      pageable = page.nextPageable();
    }
    log.info("Reindexing finished. {} records indexed", recordsIndexed);
  }
}
