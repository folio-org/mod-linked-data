package org.folio.linked.data.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.folio.linked.data.mapper.kafka.search.KafkaSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.BatchIndexService;
import org.folio.search.domain.dto.LinkedDataWork;
import org.folio.search.domain.dto.ResourceIndexEvent;
import org.folio.search.domain.dto.ResourceIndexEventType;
import org.folio.spring.testing.type.UnitTest;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class BatchIndexServiceImplTest {

  @Mock
  private EntityManager entityManager;
  @Mock
  private KafkaSearchMessageMapper<LinkedDataWork> searchBibliographicMessageMapper;
  @Mock
  private FolioMessageProducer<ResourceIndexEvent> bibliographicIndexEventProducer;

  @InjectMocks
  private BatchIndexServiceImpl batchIndexService;

  @Test
  void index_shouldReturnNumberOfIndexedResources_andIdsOfResourcesThatWereProcessedWithoutException() {
    //given
    var indexedResource = new Resource().setId(1L);
    var notIndexedResource = new Resource().setId(2L);
    var resourceWithException = new Resource().setId(3L);
    var index = new LinkedDataWork().id(String.valueOf(indexedResource.getId()));
    var resources = Stream.of(indexedResource, notIndexedResource, resourceWithException);
    when(searchBibliographicMessageMapper.toIndex(indexedResource, ResourceIndexEventType.CREATE))
      .thenReturn(Optional.of(index));
    when(searchBibliographicMessageMapper.toIndex(notIndexedResource, ResourceIndexEventType.CREATE))
      .thenReturn(Optional.empty());
    when(searchBibliographicMessageMapper.toIndex(resourceWithException, ResourceIndexEventType.CREATE))
      .thenThrow(new RuntimeException());

    //when
    var result = batchIndexService.index(resources);

    //then
    assertThat(result)
      .hasFieldOrPropertyWithValue("recordsIndexed", 1)
      .extracting(BatchIndexService.BatchIndexResult::indexedIds)
      .asInstanceOf(InstanceOfAssertFactories.COLLECTION)
      .hasSize(2)
      .contains(1L, 2L);
    verify(entityManager)
      .detach(indexedResource);
    verify(entityManager)
      .detach(notIndexedResource);
  }
}
