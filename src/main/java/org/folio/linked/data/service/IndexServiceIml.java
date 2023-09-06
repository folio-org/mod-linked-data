package org.folio.linked.data.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

  private final BibframeMapper bibframeMapper;

  @Override
  public IndexResponse createIndex(IndexRequest request) {
    var indexResponse = new IndexResponse();
    if (request.getReindex()) {
      long count = resourceRepository.findAll()
        .stream()
        .map(bibframeMapper::mapToIndex2)
        .peek(kafkaSender::sendResourceCreated)
        .count();
      indexResponse.setStatus(IndexResponse.StatusEnum.OK);
      indexResponse.setCount(count);
    } else {
      indexResponse.setStatus(IndexResponse.StatusEnum.OK);
      indexResponse.setCount(0L);
    }

    return indexResponse;
  }
}
