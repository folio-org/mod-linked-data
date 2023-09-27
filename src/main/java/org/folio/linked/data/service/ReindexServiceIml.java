package org.folio.linked.data.service;

import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;

import jakarta.transaction.Transactional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.mapper.resource.kafka.KafkaMessageMapper;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.util.BibframeConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Log4j2
@Service
@Transactional
@Profile(SEARCH_PROFILE)
@RequiredArgsConstructor
public class ReindexServiceIml implements ReindexService {

  @Value("${mod-linked-data.reindex.page-size}")
  private String reindexPageSize;
  private final ResourceRepository resourceRepository;
  private final KafkaSender kafkaSender;
  private final KafkaMessageMapper kafkaMessageMapper;

  @Async
  @Override
  public void reindex() {
    var pageable = Pageable.ofSize(Integer.parseInt(reindexPageSize));
    while (pageable.isPaged()) {
      var page = resourceRepository.findResourcesByTypeFull(Set.of(BibframeConstants.INSTANCE), pageable);
      page.get()
        .forEach(resource -> {
            try {
              var bibframeIndex = kafkaMessageMapper.toIndex(resource);
              kafkaSender.sendResourceCreated(kafkaMessageMapper.toIndex(resource));
              log.info("Sending resource for reindexing with id {}", bibframeIndex.getId());
            } catch (Exception e) {
              log.warn("Failed to send resource for reindexing with id {}",
                kafkaMessageMapper.toIndex(resource).getId());
            }
          }
        );
      pageable = page.nextPageable();
    }
  }
}
