package org.folio.linked.data.configuration.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.mapper.kafka.search.HubSearchMessageMapper;
import org.folio.linked.data.mapper.kafka.search.WorkSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ReindexProcessorTest {

  @InjectMocks
  private ReindexProcessor reindexProcessor;
  @Mock
  private HubSearchMessageMapper hubSearchMessageMapper;
  @Mock
  private WorkSearchMessageMapper workSearchMessageMapper;

  @Test
  void process_shouldProcessHubResource() {
    // given
    var resource = new Resource().setIdAndRefreshEdges(123L).addTypes(HUB);
    var expectedEvent = new ResourceIndexEvent();
    when(hubSearchMessageMapper.toIndex(resource, CREATE)).thenReturn(expectedEvent);

    // when
    var result = reindexProcessor.process(resource);

    // then
    assertThat(result).isEqualTo(expectedEvent);
    verify(hubSearchMessageMapper).toIndex(resource, CREATE);
    verifyNoInteractions(workSearchMessageMapper);
  }

  @Test
  void process_shouldProcessWorkResource() {
    // given
    var resource = new Resource().setIdAndRefreshEdges(456L).addTypes(WORK);
    var expectedEvent = new ResourceIndexEvent();
    when(workSearchMessageMapper.toIndex(resource, CREATE)).thenReturn(expectedEvent);

    // when
    var result = reindexProcessor.process(resource);

    // then
    assertThat(result).isEqualTo(expectedEvent);
    verify(workSearchMessageMapper).toIndex(resource, CREATE);
    verifyNoInteractions(hubSearchMessageMapper);
  }

  @Test
  void process_shouldReturnNullForUnsupportedResourceType() {
    var resource = new Resource().setIdAndRefreshEdges(789L).addTypes(INSTANCE);

    var result = reindexProcessor.process(resource);

    assertThat(result).isNull();
    verifyNoInteractions(hubSearchMessageMapper);
    verifyNoInteractions(workSearchMessageMapper);
  }

  @Test
  void process_shouldReturnNullForResourceWithoutTypes() {
    // given
    var resource = new Resource().setIdAndRefreshEdges(999L);

    // when
    var result = reindexProcessor.process(resource);

    // then
    assertThat(result).isNull();
    verifyNoInteractions(hubSearchMessageMapper);
    verifyNoInteractions(workSearchMessageMapper);
  }

  @Test
  void process_shouldProcessHubWhenResourceHasMultipleTypes() {
    // given
    var resource = new Resource().setIdAndRefreshEdges(111L).addTypes(HUB).addTypes(WORK);
    var expectedEvent = new ResourceIndexEvent();
    when(hubSearchMessageMapper.toIndex(resource, CREATE)).thenReturn(expectedEvent);

    // when
    var result = reindexProcessor.process(resource);

    // then
    assertThat(result).isEqualTo(expectedEvent);
    verify(hubSearchMessageMapper).toIndex(resource, CREATE);
    verifyNoInteractions(workSearchMessageMapper);
  }

}
