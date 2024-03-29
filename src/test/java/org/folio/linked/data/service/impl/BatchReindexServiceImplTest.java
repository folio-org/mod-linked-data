package org.folio.linked.data.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.folio.linked.data.mapper.kafka.KafkaMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.KafkaSender;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@UnitTest
@ExtendWith(MockitoExtension.class)
class BatchReindexServiceImplTest {

  @Mock
  private KafkaSender kafkaSender;
  @Mock
  private KafkaMessageMapper kafkaMessageMapper;
  @Mock
  private EntityManager entityManager;

  @InjectMocks
  private BatchReindexServiceImpl batchReindexService;

  @Test
  void batchReindex_shouldReturnNumberOfIndexedWorks_andIdsOfWorksThatWereProcessedWithoutException() {
    //given
    var workPage = mock(Page.class);
    var indexedWork = new Resource().setId(1L);
    var workIndex = new BibframeIndex("1");
    var notIndexedWork = new Resource().setId(2L);
    var workThatWasProcessedWithException = new Resource().setId(3L);
    var workStream = Stream.of(indexedWork, notIndexedWork, workThatWasProcessedWithException);

    when(workPage.get()).thenReturn(workStream);
    when(kafkaMessageMapper.toIndex(indexedWork, CREATE)).thenReturn(Optional.of(workIndex));
    when(kafkaMessageMapper.toIndex(notIndexedWork, CREATE)).thenReturn(Optional.empty());
    when(kafkaMessageMapper.toIndex(workThatWasProcessedWithException, CREATE)).thenThrow(new RuntimeException());

    //when
    var result = batchReindexService.batchReindex(workPage);

    //then
    assertThat(result.recordsIndexed()).isEqualTo(1);
    assertThat(result.indexedIds()).isEqualTo(Set.of(1L, 2L));
    verify(kafkaSender).sendResourceCreated(workIndex, false);
    verify(entityManager).detach(indexedWork);
    verify(entityManager).detach(notIndexedWork);
  }
}
