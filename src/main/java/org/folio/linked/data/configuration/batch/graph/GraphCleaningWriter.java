package org.folio.linked.data.configuration.batch.graph;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.repo.ResourceRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
@StepScope
@RequiredArgsConstructor
public class GraphCleaningWriter implements ItemWriter<Long> {

  private final ResourceRepository resourceRepository;

  @Override
  @Transactional(propagation = REQUIRES_NEW)
  public void write(@NonNull Chunk<? extends Long> chunk) {
    resourceRepository.deleteAllById(chunk.getItems());
  }

}
