package org.folio.linked.data.integration.kafka.sender.search;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;
import static org.folio.search.domain.dto.ResourceEventType.DELETE;
import static org.folio.search.domain.dto.ResourceEventType.UPDATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.folio.linked.data.integration.kafka.message.SearchIndexEventMessage;
import org.folio.linked.data.mapper.kafka.KafkaSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.BibframeLanguagesInner;
import org.folio.spring.testing.type.UnitTest;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@UnitTest
@ExtendWith(MockitoExtension.class)
class KafkaSearchSenderTest {

  @InjectMocks
  private KafkaSearchSenderImpl kafkaSearchSender;
  @Mock
  private FolioMessageProducer<SearchIndexEventMessage> searchIndexEventMessageProducer;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private KafkaSearchMessageMapper kafkaSearchMessageMapper;

  @Test
  void sendSingleResourceCreated_shouldNotSendMessage_ifGivenResourceIsNotIndexable() {
    // given
    var resource = new Resource();
    when(kafkaSearchMessageMapper.toIndex(resource, CREATE)).thenReturn(Optional.empty());

    // when
    kafkaSearchSender.sendSingleResourceCreated(resource);

    // then
    verify(searchIndexEventMessageProducer, never()).sendMessages(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendSingleResourceCreated_shouldSendMessageAndPublishEvent_ifGivenResourceIsIndexable() {
    // given
    var resource = new Resource().setId(randomLong());
    var index = new BibframeIndex().id(String.valueOf(resource.getId()));
    when(kafkaSearchMessageMapper.toIndex(resource, CREATE)).thenReturn(Optional.of(index));

    // when
    kafkaSearchSender.sendSingleResourceCreated(resource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(searchIndexEventMessageProducer).sendMessages(messageCaptor.capture());
    List<SearchIndexEventMessage> messages = messageCaptor.getValue();
    assertThat(messages).hasSize(1);
    var message = messages.get(0);
    assertThat(message.getId()).isNotNull();
    assertThat(message.getType()).isEqualTo(CREATE);
    assertThat(message.getResourceName()).isEqualTo(SEARCH_RESOURCE_NAME);
    assertThat(message.getNew()).isEqualTo(index);
    verify(eventPublisher).publishEvent(new ResourceIndexedEvent(parseLong(index.getId())));
  }

  @Test
  void sendMultipleResourceCreated_shouldReturnFalseAndNotSendMessage_ifGivenResourceIsNotIndexable() {
    // given
    var resource = new Resource();
    when(kafkaSearchMessageMapper.toIndex(resource, CREATE)).thenReturn(Optional.empty());

    // when
    var result = kafkaSearchSender.sendMultipleResourceCreated(resource);

    // then
    assertThat(result).isFalse();
    verify(searchIndexEventMessageProducer, never()).sendMessages(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendMultipleResourceCreated_shouldReturnTrueAndSendMessageButNotPublishEvent_ifGivenResourceIsIndexable() {
    // given
    var resource = new Resource().setId(randomLong());
    var index = new BibframeIndex().id(String.valueOf(resource.getId()));
    when(kafkaSearchMessageMapper.toIndex(resource, CREATE)).thenReturn(Optional.of(index));

    // when
    var result = kafkaSearchSender.sendMultipleResourceCreated(resource);

    // then
    assertThat(result).isTrue();
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(searchIndexEventMessageProducer).sendMessages(messageCaptor.capture());
    List<SearchIndexEventMessage> messages = messageCaptor.getValue();
    assertThat(messages).hasSize(1);
    var message = messages.get(0);
    assertThat(message.getId()).isNotNull();
    assertThat(message.getType()).isEqualTo(CREATE);
    assertThat(message.getResourceName()).isEqualTo(SEARCH_RESOURCE_NAME);
    assertThat(message.getNew()).isEqualTo(index);
    verify(eventPublisher, never()).publishEvent(new ResourceIndexedEvent(parseLong(index.getId())));
  }

  @Test
  void sendResourceUpdated_shouldNotSendMessage_ifNewResourceIsNotIndexableAndOldResourceToo() {
    // given
    var newResource = new Resource().setId(1L);
    var oldResource = new Resource().setId(2L);
    when(kafkaSearchMessageMapper.toIndex(newResource, UPDATE)).thenReturn(Optional.empty());
    when(kafkaSearchMessageMapper.toDeleteIndexId(oldResource)).thenReturn(Optional.empty());

    // when
    kafkaSearchSender.sendResourceUpdated(newResource, oldResource);

    // then
    verify(searchIndexEventMessageProducer, never()).sendMessages(any());
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
    when(kafkaSearchMessageMapper.toIndex(newResource, UPDATE))
      .thenReturn(Optional.of(indexNew))
      .thenReturn(Optional.of(indexOld));

    // when
    kafkaSearchSender.sendResourceUpdated(newResource, oldResource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(searchIndexEventMessageProducer).sendMessages(messageCaptor.capture());
    List<SearchIndexEventMessage> messages = messageCaptor.getValue();
    assertThat(messages).hasSize(1);
    var message = messages.get(0);
    assertThat(message.getId()).isNotNull();
    assertThat(message.getType()).isEqualTo(UPDATE);
    assertThat(message.getResourceName()).isEqualTo(SEARCH_RESOURCE_NAME);
    assertThat(message.getNew()).isEqualTo(indexNew);
    assertThat(message.getOld()).isEqualTo(indexOld);
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
    when(kafkaSearchMessageMapper.toIndex(newResource, UPDATE)).thenReturn(Optional.of(indexNew));
    when(kafkaSearchMessageMapper.toDeleteIndexId(oldResource)).thenReturn(Optional.of(oldId));

    // when
    kafkaSearchSender.sendResourceUpdated(newResource, oldResource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(searchIndexEventMessageProducer, times(2)).sendMessages(messageCaptor.capture());
    List<SearchIndexEventMessage> messages = messageCaptor.getAllValues().get(0);
    assertThat(messages).hasSize(1);
    var message = messages.get(0);
    assertThat(message.getId()).isNotNull();
    assertThat(message.getType()).isEqualTo(DELETE);
    assertThat(message.getResourceName()).isEqualTo(SEARCH_RESOURCE_NAME);
    assertThat(message.getNew()).isEqualTo(new BibframeIndex(oldId.toString()));
    List<SearchIndexEventMessage> messages2 = messageCaptor.getAllValues().get(1);
    assertThat(messages2).hasSize(1);
    var message2 = messages2.get(0);
    assertThat(message2.getId()).isNotNull();
    assertThat(message2.getType()).isEqualTo(CREATE);
    assertThat(message2.getResourceName()).isEqualTo(SEARCH_RESOURCE_NAME);
    assertThat(message2.getNew()).isEqualTo(indexNew);
    verify(eventPublisher).publishEvent(new ResourceIndexedEvent(newId));
  }

  @Test
  void sendResourceUpdated_shouldSendDelete_ifNewResourceIsNotIndexableButOldIs() {
    // given
    Long newId = 1L;
    Long oldId = 2L;
    var newResource = new Resource().setId(newId).setLabel("new");
    var oldResource = new Resource().setId(oldId).setLabel("old");
    when(kafkaSearchMessageMapper.toIndex(newResource, UPDATE)).thenReturn(Optional.empty());
    when(kafkaSearchMessageMapper.toDeleteIndexId(oldResource)).thenReturn(Optional.of(oldId));

    // when
    kafkaSearchSender.sendResourceUpdated(newResource, oldResource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(searchIndexEventMessageProducer).sendMessages(messageCaptor.capture());
    List<SearchIndexEventMessage> messages = messageCaptor.getValue();
    assertThat(messages).hasSize(1);
    var message = messages.get(0);
    assertThat(message.getId()).isNotNull();
    assertThat(message.getType()).isEqualTo(DELETE);
    assertThat(message.getResourceName()).isEqualTo(SEARCH_RESOURCE_NAME);
    assertThat(message.getNew()).isEqualTo(new BibframeIndex(oldId.toString()));
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendResourceUpdated_shouldSendNothing_ifBothResourcesAreNotIndexable() {
    // given
    Long newId = 1L;
    Long oldId = 2L;
    var newResource = new Resource().setId(newId).setLabel("new");
    var oldResource = new Resource().setId(oldId).setLabel("old");
    when(kafkaSearchMessageMapper.toIndex(newResource, UPDATE)).thenReturn(Optional.empty());
    when(kafkaSearchMessageMapper.toDeleteIndexId(oldResource)).thenReturn(Optional.empty());

    // when
    kafkaSearchSender.sendResourceUpdated(newResource, oldResource);

    // then
    verify(searchIndexEventMessageProducer, never()).sendMessages(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendResourceDeleted_shouldSendDelete_ifResourcesIsIndexable() {
    // given
    Long id = 1L;
    var resource = new Resource().setId(id);
    when(kafkaSearchMessageMapper.toDeleteIndexId(resource)).thenReturn(Optional.of(id));

    // when
    kafkaSearchSender.sendResourceDeleted(resource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(searchIndexEventMessageProducer).sendMessages(messageCaptor.capture());
    List<SearchIndexEventMessage> messages = messageCaptor.getValue();
    assertThat(messages).hasSize(1);
    var message = messages.get(0);
    assertThat(message.getId()).isNotNull();
    assertThat(message.getType()).isEqualTo(DELETE);
    assertThat(message.getResourceName()).isEqualTo(SEARCH_RESOURCE_NAME);
    assertThat(message.getNew()).isEqualTo(new BibframeIndex(id.toString()));
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendResourceDeleted_shouldSendNothing_ifResourcesIsNotIndexable() {
    // given
    Long id = 1L;
    var resource = new Resource().setId(id);
    when(kafkaSearchMessageMapper.toDeleteIndexId(resource)).thenReturn(Optional.empty());

    // when
    kafkaSearchSender.sendResourceDeleted(resource);

    // then
    verify(searchIndexEventMessageProducer, never()).sendMessages(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

}
