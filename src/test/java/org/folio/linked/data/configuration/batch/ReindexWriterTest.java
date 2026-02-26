package org.folio.linked.data.configuration.batch;

import static org.folio.linked.data.util.Constants.SEARCH_HUB_RESOURCE_NAME;
import static org.folio.linked.data.util.Constants.SEARCH_WORK_RESOURCE_NAME;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;
import org.folio.linked.data.domain.dto.LinkedDataHub;
import org.folio.linked.data.domain.dto.LinkedDataWork;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.service.resource.ResourceService;
import org.folio.spring.testing.type.UnitTest;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.Chunk;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ReindexWriterTest {

  private ReindexWriter reindexWriter;
  @Mock
  private ResourceService resourceService;
  @Mock
  private FolioMessageProducer<ResourceIndexEvent> hubIndexMessageProducer;
  @Mock
  private FolioMessageProducer<ResourceIndexEvent> workIndexMessageProducer;

  @BeforeEach
  void setUp() {
    reindexWriter = new ReindexWriter(resourceService, hubIndexMessageProducer, workIndexMessageProducer);
  }

  @Test
  void write_shouldProcessWorkEvents() {
    // given
    var work1 = new LinkedDataWork().id("123");
    var work2 = new LinkedDataWork().id("456");
    var event1 = new ResourceIndexEvent().resourceName(SEARCH_WORK_RESOURCE_NAME)._new(work1);
    var event2 = new ResourceIndexEvent().resourceName(SEARCH_WORK_RESOURCE_NAME)._new(work2);
    var chunk = new Chunk<>(List.of(event1, event2));

    // when
    reindexWriter.write(chunk);

    // then
    verify(resourceService).updateIndexDateBatch(argThat(ids ->
      ids.size() == 2 && ids.contains(123L) && ids.contains(456L)
    ));
    verify(workIndexMessageProducer).sendMessages(argThat(events ->
      events.size() == 2 && events.contains(event1) && events.contains(event2)
    ));
    verify(hubIndexMessageProducer).sendMessages(List.of());
  }

  @Test
  void write_shouldProcessHubEvents() {
    // given
    var hub1 = new LinkedDataHub().id("789");
    var hub2 = new LinkedDataHub().id("101");
    var event1 = new ResourceIndexEvent().resourceName(SEARCH_HUB_RESOURCE_NAME)._new(hub1);
    var event2 = new ResourceIndexEvent().resourceName(SEARCH_HUB_RESOURCE_NAME)._new(hub2);
    var chunk = new Chunk<>(List.of(event1, event2));

    // when
    reindexWriter.write(chunk);

    // then
    verify(resourceService).updateIndexDateBatch(argThat(ids ->
      ids.size() == 2 && ids.contains(789L) && ids.contains(101L)
    ));
    verify(hubIndexMessageProducer).sendMessages(argThat(events ->
      events.size() == 2 && events.contains(event1) && events.contains(event2)
    ));
    verify(workIndexMessageProducer).sendMessages(List.of());
  }

  @Test
  void write_shouldProcessMixedEvents() {
    // given
    var work = new LinkedDataWork().id("111");
    var hub = new LinkedDataHub().id("222");
    var workEvent = new ResourceIndexEvent().resourceName(SEARCH_WORK_RESOURCE_NAME)._new(work);
    var hubEvent = new ResourceIndexEvent().resourceName(SEARCH_HUB_RESOURCE_NAME)._new(hub);
    var chunk = new Chunk<>(List.of(workEvent, hubEvent));

    // when
    reindexWriter.write(chunk);

    // then
    verify(resourceService).updateIndexDateBatch(argThat(ids ->
      ids.size() == 2 && ids.contains(111L) && ids.contains(222L)
    ));
    verify(workIndexMessageProducer).sendMessages(argThat(events ->
      events.size() == 1 && events.contains(workEvent)
    ));
    verify(hubIndexMessageProducer).sendMessages(argThat(events ->
      events.size() == 1 && events.contains(hubEvent)
    ));
  }

  @Test
  void write_shouldHandleEmptyChunk() {
    // given
    var chunk = new Chunk<ResourceIndexEvent>(List.of());

    // when
    reindexWriter.write(chunk);

    // then
    verify(resourceService).updateIndexDateBatch(Set.of());
    verify(hubIndexMessageProducer).sendMessages(List.of());
    verify(workIndexMessageProducer).sendMessages(List.of());
  }

  @Test
  void write_shouldFilterOutEventsWithNullData() {
    // given
    var work = new LinkedDataWork().id("333");
    var validEvent = new ResourceIndexEvent().resourceName(SEARCH_WORK_RESOURCE_NAME)._new(work);
    var invalidEvent = new ResourceIndexEvent().resourceName(SEARCH_WORK_RESOURCE_NAME)._new(new Object());
    var chunk = new Chunk<>(List.of(validEvent, invalidEvent));

    // when
    reindexWriter.write(chunk);

    // then
    verify(resourceService).updateIndexDateBatch(argThat(ids ->
      ids.size() == 1 && ids.contains(333L)
    ));
    verify(workIndexMessageProducer).sendMessages(argThat(events ->
      events.size() == 2
    ));
    verify(hubIndexMessageProducer).sendMessages(List.of());
  }

  @Test
  void write_shouldHandleDuplicateResources() {
    // given
    var work1 = new LinkedDataWork().id("444");
    var work2 = new LinkedDataWork().id("444");
    var event1 = new ResourceIndexEvent().resourceName(SEARCH_WORK_RESOURCE_NAME)._new(work1);
    var event2 = new ResourceIndexEvent().resourceName(SEARCH_WORK_RESOURCE_NAME)._new(work2);
    var chunk = new Chunk<>(List.of(event1, event2));

    // when
    reindexWriter.write(chunk);

    // then
    verify(resourceService).updateIndexDateBatch(argThat(ids ->
      ids.size() == 1 && ids.contains(444L)
    ));
    verify(workIndexMessageProducer).sendMessages(argThat(events ->
      events.size() == 1
    ));
    verify(hubIndexMessageProducer).sendMessages(List.of());
  }

}
