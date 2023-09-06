package org.folio.linked.data.service;

import jakarta.transaction.Transactional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.configuration.properties.BibframeProperties;
import org.folio.linked.data.domain.dto.IndexRequest;
import org.folio.linked.data.domain.dto.IndexResponse;
import org.folio.linked.data.mapper.BibframeMapper;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class IndexServiceIml implements IndexService {

  private final ResourceRepository resourceRepository;
  private final KafkaSender kafkaSender;
  private final BibframeProperties bibframeProperties;
  private final BibframeMapper bibframeMapper;

  @Override
  public IndexResponse createIndex(IndexRequest request) {
    var indexResponse = new IndexResponse();
    if (request.getReindex()) {
      long count = resourceRepository.findResourcesByType(bibframeProperties.getProfiles())
        .stream()
        .map(bibframeMapper::mapToIndex2)
        .map(bibframeIndex -> {
          kafkaSender.sendResourceCreated(bibframeIndex);
          return bibframeIndex;
        })
        .collect(Collectors.toList())
        .size();
      indexResponse.setStatus(IndexResponse.StatusEnum.OK);
      indexResponse.setCount(count);
    } else {
      indexResponse.setStatus(IndexResponse.StatusEnum.OK);
      indexResponse.setCount(0L);
    }

    return indexResponse;
  }
}
