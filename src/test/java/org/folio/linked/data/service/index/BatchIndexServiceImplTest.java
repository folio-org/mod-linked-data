package org.folio.linked.data.service.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import jakarta.persistence.EntityManager;
import java.util.stream.Stream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.folio.linked.data.integration.kafka.sender.search.WorkCreateMessageSender;
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
  private EntityManager entityManager;
  @Mock
  private WorkCreateMessageSender workCreateMessageSender;

  @InjectMocks
  private BatchIndexServiceImpl batchIndexService;

  @Test
  void indexWorks_shouldReturnNumberOfIndexedResources_andIdsOfResourcesThatWereProcessedWithoutException() {
    //given
    var indexedResource = new Resource().setIdAndRefreshEdges(1L);
    var resourceWithException = new Resource().setIdAndRefreshEdges(2L);
    var resources = Stream.of(indexedResource, resourceWithException);
    doNothing().when(workCreateMessageSender).acceptWithoutIndexDateUpdate(indexedResource);
    doThrow(new RuntimeException()).when(workCreateMessageSender).acceptWithoutIndexDateUpdate(resourceWithException);

    //when
    var result = batchIndexService.indexWorks(resources);

    //then
    assertThat(result)
      .hasFieldOrPropertyWithValue("recordsIndexed", 1)
      .extracting(BatchIndexService.BatchIndexResult::indexedIds)
      .asInstanceOf(InstanceOfAssertFactories.COLLECTION)
      .containsOnly(1L);
    verify(entityManager).detach(indexedResource);
    verify(entityManager).detach(resourceWithException);
  }
}
