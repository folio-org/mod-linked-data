package org.folio.linked.data.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;
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

@UnitTest
@ExtendWith(MockitoExtension.class)
class BatchIndexServiceImplTest {

  @Mock
  private KafkaSender kafkaSender;
  @Mock
  private KafkaMessageMapper kafkaMessageMapper;
  @Mock
  private EntityManager entityManager;

  @InjectMocks
  private BatchIndexServiceImpl batchIndexService;

  @Test
  void index_shouldReturnNumberOfIndexedResources_andIdsOfResourcesThatWereProcessedWithoutException() {
    //given
    var indexedResource = new Resource().setId(1L);
    var resourceIndex = new BibframeIndex("1");
    var notIndexedResource = new Resource().setId(2L);
    var resourceThatWasProcessedWithException = new Resource().setId(3L);
    var resources = Stream.of(indexedResource, notIndexedResource, resourceThatWasProcessedWithException);

    when(kafkaMessageMapper.toIndex(indexedResource, CREATE)).thenReturn(Optional.of(resourceIndex));
    when(kafkaMessageMapper.toIndex(notIndexedResource, CREATE)).thenReturn(Optional.empty());
    when(kafkaMessageMapper.toIndex(resourceThatWasProcessedWithException, CREATE)).thenThrow(new RuntimeException());

    //when
    var result = batchIndexService.index(resources);

    //then
    assertThat(result.recordsIndexed()).isEqualTo(1);
    assertThat(result.indexedIds()).isEqualTo(Set.of(1L, 2L));
    verify(kafkaSender).sendResourceCreated(resourceIndex, false);
    verify(entityManager).detach(indexedResource);
    verify(entityManager).detach(notIndexedResource);
  }
}
