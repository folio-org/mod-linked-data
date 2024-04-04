package org.folio.linked.data.integration.kafka;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;
import static org.folio.search.domain.dto.ResourceEventType.DELETE;
import static org.folio.search.domain.dto.ResourceEventType.UPDATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import org.folio.linked.data.mapper.kafka.KafkaMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.BibframeLanguagesInner;
import org.folio.search.domain.dto.ResourceEvent;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

@UnitTest
@ExtendWith(MockitoExtension.class)
class KafkaSenderFolioTest {

  private static final String TOPIC = "topic";
  private static final String TENANT = "tenant";
  private static final String TOPIC_FULL = "folio." + TENANT + "." + TOPIC;
  @InjectMocks
  private KafkaSenderFolio kafkaSenderFolio;
  @Mock
  private KafkaTemplate<String, ResourceEvent> kafkaTemplate;
  @Mock
  private FolioExecutionContext folioExecutionContext;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private KafkaMessageMapper kafkaMessageMapper;

  @BeforeEach
  public void setup() {
    ReflectionTestUtils.setField(kafkaSenderFolio, "initialBibframeIndexTopicName", TOPIC);
    lenient().when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
  }

  @Test
  void sendSingleResourceCreated_shouldNotSendMessage_ifGivenResourceIsNotIndexable() {
    // given
    var resource = new Resource();
    when(kafkaMessageMapper.toIndex(resource, CREATE)).thenReturn(Optional.empty());

    // when
    kafkaSenderFolio.sendSingleResourceCreated(resource);

    // then
    verify(kafkaTemplate, never()).send(any(), any(), any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendSingleResourceCreated_shouldSendMessageAndPublishEvent_ifGivenResourceIsIndexable() {
    // given
    var resource = new Resource().setId(randomLong());
    var index = new BibframeIndex().id(String.valueOf(resource.getId()));
    when(kafkaMessageMapper.toIndex(resource, CREATE)).thenReturn(Optional.of(index));
    var expectedEvent = new ResourceEvent()
      .id(String.valueOf(resource.getId()))
      .type(CREATE)
      .tenant(TENANT)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(index);
    var futureMock = getFutureMock();
    when(kafkaTemplate.send(TOPIC_FULL, index.getId(), expectedEvent)).thenReturn(futureMock);

    // when
    kafkaSenderFolio.sendSingleResourceCreated(resource);

    // then
    verify(kafkaTemplate).send(TOPIC_FULL, index.getId(), expectedEvent);
    verify(eventPublisher).publishEvent(new ResourceIndexedEvent(parseLong(index.getId())));
  }

  @Test
  void sendMultipleResourceCreated_shouldReturnFalseAndNotSendMessage_ifGivenResourceIsNotIndexable() {
    // given
    var resource = new Resource();
    when(kafkaMessageMapper.toIndex(resource, CREATE)).thenReturn(Optional.empty());

    // when
    var result = kafkaSenderFolio.sendMultipleResourceCreated(resource);

    // then
    assertThat(result).isFalse();
    verify(kafkaTemplate, never()).send(any(), any(), any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendMultipleResourceCreated_shouldReturnTrueAndSendMessageButNotPublishEvent_ifGivenResourceIsIndexable() {
    // given
    var resource = new Resource().setId(randomLong());
    var index = new BibframeIndex().id(String.valueOf(resource.getId()));
    when(kafkaMessageMapper.toIndex(resource, CREATE)).thenReturn(Optional.of(index));
    var expectedEvent = new ResourceEvent()
      .id(String.valueOf(resource.getId()))
      .type(CREATE)
      .tenant(TENANT)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(index);
    var futureMock = getFutureMock();
    when(kafkaTemplate.send(TOPIC_FULL, index.getId(), expectedEvent)).thenReturn(futureMock);

    // when
    var result = kafkaSenderFolio.sendMultipleResourceCreated(resource);

    // then
    assertThat(result).isTrue();
    verify(kafkaTemplate).send(TOPIC_FULL, index.getId(), expectedEvent);
    verify(eventPublisher, never()).publishEvent(new ResourceIndexedEvent(parseLong(index.getId())));
  }

  @Test
  void sendResourceUpdated_shouldNotSendMessage_ifNewResourceIsNotIndexableAndOldResourceToo() {
    // given
    var newResource = new Resource().setId(1L);
    var oldResource = new Resource().setId(2L);
    when(kafkaMessageMapper.toIndex(newResource, UPDATE)).thenReturn(Optional.empty());
    when(kafkaMessageMapper.toDeleteIndexId(oldResource)).thenReturn(Optional.empty());

    // when
    kafkaSenderFolio.sendResourceUpdated(newResource, oldResource);

    // then
    verify(kafkaTemplate, never()).send(any(), any(), any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendResourceUpdated_shouldSendUpdate_ifNewResourceIsIndexableAndKeepsSameId() {
    // given
    long id = 1L;
    var newResource = new Resource().setId(id).setLabel("new");
    var oldResource = new Resource().setId(id).setLabel("old");
    var indexNew = new BibframeIndex().id(String.valueOf(id));
    var indexOld = new BibframeIndex().id(String.valueOf(id)).addLanguagesItem(new BibframeLanguagesInner());
    when(kafkaMessageMapper.toIndex(newResource, UPDATE))
      .thenReturn(Optional.of(indexNew))
      .thenReturn(Optional.of(indexOld));
    var expectedEvent = new ResourceEvent()
      .id(String.valueOf(id))
      .type(UPDATE)
      .tenant(TENANT)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(indexNew)
      .old(indexOld);
    var futureMock = getFutureMock();
    when(kafkaTemplate.send(TOPIC_FULL, String.valueOf(id), expectedEvent)).thenReturn(futureMock);

    // when
    kafkaSenderFolio.sendResourceUpdated(newResource, oldResource);

    // then
    verify(kafkaTemplate).send(TOPIC_FULL, String.valueOf(id), expectedEvent);
    verify(eventPublisher).publishEvent(new ResourceIndexedEvent(id));
  }

  @Test
  void sendResourceUpdated_shouldSendDeleteAndCreate_ifNewResourceHasNewIdAndBothResourcesAreIndexable() {
    // given
    Long newId = 1L;
    Long oldId = 2L;
    var newResource = new Resource().setId(newId).setLabel("new");
    var oldResource = new Resource().setId(oldId).setLabel("old");
    var indexNew = new BibframeIndex().id(newId.toString());
    when(kafkaMessageMapper.toIndex(newResource, UPDATE)).thenReturn(Optional.of(indexNew));
    when(kafkaMessageMapper.toDeleteIndexId(oldResource)).thenReturn(Optional.of(oldId));
    var expectedFuture = getFutureMock();
    var expectedDeleteEvent = new ResourceEvent()
      .id(oldId.toString())
      .type(DELETE)
      .tenant(TENANT)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(new BibframeIndex(oldId.toString()));
    when(kafkaTemplate.send(TOPIC_FULL, oldId.toString(), expectedDeleteEvent)).thenReturn(expectedFuture);
    var expectedCreateEvent = new ResourceEvent()
      .id(newId.toString())
      .type(CREATE)
      .tenant(TENANT)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(indexNew);
    when(kafkaTemplate.send(TOPIC_FULL, newId.toString(), expectedCreateEvent)).thenReturn(expectedFuture);

    // when
    kafkaSenderFolio.sendResourceUpdated(newResource, oldResource);

    // then
    verify(kafkaTemplate).send(TOPIC_FULL, oldId.toString(), expectedDeleteEvent);
    verify(kafkaTemplate).send(TOPIC_FULL, newId.toString(), expectedCreateEvent);
    verify(eventPublisher).publishEvent(new ResourceIndexedEvent(newId));
  }

  @Test
  void sendResourceUpdated_shouldSendDelete_ifNewResourceIsNotIndexableButOldIs() {
    // given
    Long newId = 1L;
    Long oldId = 2L;
    var newResource = new Resource().setId(newId).setLabel("new");
    var oldResource = new Resource().setId(oldId).setLabel("old");
    when(kafkaMessageMapper.toIndex(newResource, UPDATE)).thenReturn(Optional.empty());
    when(kafkaMessageMapper.toDeleteIndexId(oldResource)).thenReturn(Optional.of(oldId));
    var expectedFuture = getFutureMock();
    var expectedDeleteEvent = new ResourceEvent()
      .id(oldId.toString())
      .type(DELETE)
      .tenant(TENANT)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(new BibframeIndex(oldId.toString()));
    when(kafkaTemplate.send(TOPIC_FULL, oldId.toString(), expectedDeleteEvent)).thenReturn(expectedFuture);

    // when
    kafkaSenderFolio.sendResourceUpdated(newResource, oldResource);

    // then
    verify(kafkaTemplate).send(TOPIC_FULL, oldId.toString(), expectedDeleteEvent);
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendResourceUpdated_shouldSendNothing_ifBothResourcesAreNotIndexable() {
    // given
    Long newId = 1L;
    Long oldId = 2L;
    var newResource = new Resource().setId(newId).setLabel("new");
    var oldResource = new Resource().setId(oldId).setLabel("old");
    when(kafkaMessageMapper.toIndex(newResource, UPDATE)).thenReturn(Optional.empty());
    when(kafkaMessageMapper.toDeleteIndexId(oldResource)).thenReturn(Optional.empty());

    // when
    kafkaSenderFolio.sendResourceUpdated(newResource, oldResource);

    // then
    verify(kafkaTemplate, never()).send(any(), any(), any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendResourceDeleted_shouldSendDelete_ifResourcesIsIndexable() {
    // given
    Long id = 1L;
    var resource = new Resource().setId(id);
    when(kafkaMessageMapper.toDeleteIndexId(resource)).thenReturn(Optional.of(id));
    var expectedFuture = getFutureMock();
    var expectedDeleteEvent = new ResourceEvent()
      .id(id.toString())
      .type(DELETE)
      .tenant(TENANT)
      .resourceName(SEARCH_RESOURCE_NAME)
      ._new(new BibframeIndex(id.toString()));
    when(kafkaTemplate.send(TOPIC_FULL, id.toString(), expectedDeleteEvent)).thenReturn(expectedFuture);

    // when
    kafkaSenderFolio.sendResourceDeleted(resource);

    // then
    verify(kafkaTemplate).send(TOPIC_FULL, id.toString(), expectedDeleteEvent);
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendResourceDeleted_shouldSendNothing_ifResourcesIsNotIndexable() {
    // given
    Long id = 1L;
    var resource = new Resource().setId(id);
    when(kafkaMessageMapper.toDeleteIndexId(resource)).thenReturn(Optional.empty());

    // when
    kafkaSenderFolio.sendResourceDeleted(resource);

    // then
    verify(kafkaTemplate, never()).send(any(), any(), any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @SneakyThrows
  private CompletableFuture<SendResult<String, ResourceEvent>> getFutureMock() {
    var future = mock(CompletableFuture.class);
    var expectedSendResult = mock(SendResult.class);
    when(future.get()).thenReturn(expectedSendResult);
    var actionCaptor = ArgumentCaptor.forClass(Runnable.class);
    lenient().when(future.thenRun(actionCaptor.capture())).thenAnswer(invocationOnMock -> {
      actionCaptor.getValue().run();
      return null;
    });
    return future;
  }

}
