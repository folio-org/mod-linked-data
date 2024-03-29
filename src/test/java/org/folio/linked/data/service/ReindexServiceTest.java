package org.folio.linked.data.service;

import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.BatchReindexService.BatchReindexResult;
import org.folio.linked.data.service.impl.ReindexServiceImpl;
import org.folio.spring.test.type.UnitTest;
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
  private BatchReindexService batchReindexService;

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(reindexService, "reindexPageSize", 100);
  }

  @Test
  void reindexFull_shouldReindexAllWorks_UpdateIndexDateOfWorksThatWereSuccessfullyProcessed() {
    // given
    var notIndexedWorkPage = mock(Page.class);
    when(resourceRepository.findAllByType(eq(Set.of(WORK.getUri())), any(Pageable.class)))
      .thenReturn(notIndexedWorkPage);
    when(notIndexedWorkPage.nextPageable()).thenReturn(Pageable.unpaged());
    when(batchReindexService.batchReindex(notIndexedWorkPage))
      .thenReturn(new BatchReindexResult(1, Set.of(1L, 2L)));

    // when
    reindexService.reindex(true);

    // then
    verify(resourceService).updateIndexDateBatch(Set.of(1L, 2L));
  }

  @Test
  void reindexNotFull_shouldReindexNotIndexedWorks_UpdateIndexDateOfWorksThatWereSuccessfullyProcessed() {
    // given
    var notIndexedWorkPage = mock(Page.class);
    when(resourceRepository.findNotIndexedByType(eq(Set.of(WORK.getUri())), any(Pageable.class)))
      .thenReturn(notIndexedWorkPage);
    when(notIndexedWorkPage.nextPageable()).thenReturn(Pageable.unpaged());
    when(batchReindexService.batchReindex(notIndexedWorkPage))
      .thenReturn(new BatchReindexResult(1, Set.of(1L, 2L)));

    // when
    reindexService.reindex(false);

    // then
    verify(resourceService).updateIndexDateBatch(Set.of(1L, 2L));
  }

}
