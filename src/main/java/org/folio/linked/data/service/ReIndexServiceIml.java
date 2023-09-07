package org.folio.linked.data.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.configuration.properties.BibframeProperties;
import org.folio.linked.data.mapper.resource.kafka.KafkaMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class ReIndexServiceIml implements ReIndexService {

  @Value("${mod-linked-data.reindex.page-size}")
  private String pageSize;
  private final ResourceRepository resourceRepository;
  private final KafkaSender kafkaSender;
  private final BibframeProperties bibframeProperties;
  private final KafkaMessageMapper kafkaMessageMapper;

  @Override
  public void reindex() {
    Pageable pageable = Pageable.ofSize(Integer.parseInt(pageSize));
    while (pageable.isPaged()) {
      Page<Resource> page = resourceRepository.findAllResourcesByType(bibframeProperties.getProfiles(), pageable);
      page.get()
        .map(kafkaMessageMapper::toIndex)
        .map(bibframeIndex -> {
          try {
            kafkaSender.sendResourceCreated(bibframeIndex);
          } catch (Exception e) {
            log.warn("Failed to send resource for reindexing with id {}", bibframeIndex.getId());
          }
          return bibframeIndex;
        })
        .forEach(bibframeIndex -> log.info("Sending resource for reindexing with id {}", bibframeIndex.getId()));
      pageable = page.nextPageable();
    }
  }
}
