package org.folio.linked.data.service;

import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.folio.linked.data.mapper.kafka.KafkaMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.impl.ReindexServiceImpl;
import org.folio.search.domain.dto.BibframeIndex;
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
  private KafkaSender kafkaSender;
  @Mock
  private KafkaMessageMapper kafkaMessageMapper;
  @Mock
  private ResourceService resourceService;
  @Mock
  private EntityManager entityManager;

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(reindexService, "reindexPageSize", "100");
  }

  @Test
  void reindexFull_shouldSendAllIndexableWorksToKafka_detachAndSaveWithIndexDateAllWorks() {
    // given
    var notIndexedWorkPage = mock(Page.class);
    when(resourceRepository.findAllByType(eq(Set.of(WORK.getUri())), any(Pageable.class)))
      .thenReturn(notIndexedWorkPage);
    var indexableWork1 = new Resource().setId(1L);
    var notIndexableWork2 = new Resource().setId(2L);
    var notIndexedWorkStream = Stream.of(indexableWork1, notIndexableWork2);
    when(notIndexedWorkPage.get()).thenReturn(notIndexedWorkStream);
    when(notIndexedWorkPage.nextPageable()).thenReturn(Pageable.unpaged());
    var work1Index = new BibframeIndex("1");
    when(kafkaMessageMapper.toIndex(indexableWork1, CREATE)).thenReturn(Optional.of(work1Index));
    when(kafkaMessageMapper.toIndex(notIndexableWork2, CREATE)).thenReturn(Optional.empty());

    // when
    reindexService.reindex(true);

    // then
    verify(kafkaSender).sendResourceCreated(work1Index, false);
    verify(entityManager).detach(indexableWork1);
    verify(entityManager).detach(notIndexableWork2);
    verify(resourceService).updateIndexDateBatch(Set.of(1L, 2L));
  }

  @Test
  void reindexNotFull_shouldSendNotIndexedIndexableWorksToKafka_detachAndSaveWithIndexDateAllWorks() {
    // given
    var notIndexedWorkPage = mock(Page.class);
    when(resourceRepository.findNotIndexedByType(eq(Set.of(WORK.getUri())), any(Pageable.class)))
      .thenReturn(notIndexedWorkPage);
    var indexableWork1 = new Resource().setId(1L);
    var notIndexableWork2 = new Resource().setId(2L);
    var notIndexedWorkStream = Stream.of(indexableWork1, notIndexableWork2);
    when(notIndexedWorkPage.get()).thenReturn(notIndexedWorkStream);
    when(notIndexedWorkPage.nextPageable()).thenReturn(Pageable.unpaged());
    var work1Index = new BibframeIndex("1");
    when(kafkaMessageMapper.toIndex(indexableWork1, CREATE)).thenReturn(Optional.of(work1Index));
    when(kafkaMessageMapper.toIndex(notIndexableWork2, CREATE)).thenReturn(Optional.empty());

    // when
    reindexService.reindex(false);

    // then
    verify(kafkaSender).sendResourceCreated(work1Index, false);
    verify(entityManager).detach(indexableWork1);
    verify(entityManager).detach(notIndexableWork2);
    verify(resourceService).updateIndexDateBatch(Set.of(1L, 2L));
  }

}
