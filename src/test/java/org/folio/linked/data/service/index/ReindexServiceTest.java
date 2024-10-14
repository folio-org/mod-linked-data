package org.folio.linked.data.service.index;

import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.stream.Stream;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.index.BatchIndexService.BatchIndexResult;
import org.folio.linked.data.service.resource.ResourceService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ReindexServiceTest {

  @InjectMocks
  private ReindexServiceImpl reindexService;
  @Mock
  private ResourceRepository resourceRepository;
  @Mock
  private ResourceService resourceService;
  @Mock
  private BatchIndexService batchIndexService;

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(reindexService, "reindexPageSize", 100);
  }

  @Test
  void reindexWorksFull_shouldReindexWorksAllWorks_UpdateIndexDateOfWorksThatWereSuccessfullyProcessed() {
    // given
    var notIndexedWorkPage = mock(Page.class);
    var workStream = Stream.of(new Resource());
    when(resourceRepository.findAllByType(eq(WORK.getUri()), any(Pageable.class))).thenReturn(notIndexedWorkPage);
    when(notIndexedWorkPage.nextPageable()).thenReturn(Pageable.unpaged());
    when(notIndexedWorkPage.get()).thenReturn(workStream);
    when(batchIndexService.indexWorks(workStream))
      .thenReturn(new BatchIndexResult(1, Set.of(1L, 2L)));

    // when
    reindexService.reindexWorks(true);

    // then
    verify(resourceService).updateIndexDateBatch(Set.of(1L, 2L));
  }

  @Test
  void reindexWorksNotFull_shouldReindexWorksNotIndexedWorks_UpdateIndexDateOfWorksThatWereSuccessfullyProcessed() {
    // given
    var notIndexedWorkPage = mock(Page.class);
    var workStream = Stream.of(new Resource());
    when(resourceRepository.findNotIndexedByType(eq(WORK.getUri()), any(Pageable.class)))
      .thenReturn(notIndexedWorkPage);
    when(notIndexedWorkPage.nextPageable()).thenReturn(Pageable.unpaged());
    when(notIndexedWorkPage.get()).thenReturn(workStream);
    when(batchIndexService.indexWorks(workStream))
      .thenReturn(new BatchIndexResult(1, Set.of(1L, 2L)));

    // when
    reindexService.reindexWorks(false);

    // then
    verify(resourceService).updateIndexDateBatch(Set.of(1L, 2L));
  }

}
