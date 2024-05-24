package org.folio.linked.data.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.stream.Stream;
import org.folio.linked.data.integration.kafka.sender.search.KafkaSearchSender;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class BatchIndexServiceImplTest {

  @Mock
  private KafkaSearchSender kafkaSearchSender;
  @Mock
  private EntityManager entityManager;

  @InjectMocks
  private BatchIndexServiceImpl batchIndexService;

  @Test
  void index_shouldReturnNumberOfIndexedResources_andIdsOfResourcesThatWereProcessedWithoutException() {
    //given
    var indexedResource = new Resource().setId(1L);
    var notIndexedResource = new Resource().setId(2L);
    var resourceWithException = new Resource().setId(3L);
    var resources = Stream.of(indexedResource, notIndexedResource, resourceWithException);
    when(kafkaSearchSender.sendMultipleResourceCreated(indexedResource)).thenReturn(true);
    when(kafkaSearchSender.sendMultipleResourceCreated(notIndexedResource)).thenReturn(false);
    when(kafkaSearchSender.sendMultipleResourceCreated(resourceWithException)).thenThrow(new RuntimeException());

    //when
    var result = batchIndexService.index(resources);

    //then
    assertThat(result.recordsIndexed()).isEqualTo(1);
    assertThat(result.indexedIds()).isEqualTo(Set.of(1L, 2L));
    verify(entityManager).detach(indexedResource);
    verify(entityManager).detach(notIndexedResource);
  }
}
