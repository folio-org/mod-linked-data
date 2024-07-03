package org.folio.linked.data.integration.kafka.sender.search;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.search.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.search.domain.dto.ResourceIndexEventType.DELETE;
import static org.folio.search.domain.dto.ResourceIndexEventType.UPDATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.folio.linked.data.mapper.kafka.search.BibliographicSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.search.domain.dto.BibframeLanguagesInner;
import org.folio.search.domain.dto.LinkedDataWork;
import org.folio.search.domain.dto.ResourceIndexEvent;
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
  private FolioMessageProducer<ResourceIndexEvent> resourceIndexEventMessageProducer;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private BibliographicSearchMessageMapper bibliographicSearchMessageMapper;

  @Test
  void sendSingleWorkCreated_shouldNotSendMessage_ifGivenResourceIsNotIndexable() {
    // given
    var resource = new Resource();
    when(bibliographicSearchMessageMapper.toIndex(resource, CREATE)).thenReturn(Optional.empty());

    // when
    kafkaSearchSender.sendWorkCreated(resource);

    // then
    verify(resourceIndexEventMessageProducer, never()).sendMessages(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendSingleWorkCreated_shouldSendMessageAndPublishEvent_ifGivenResourceIsIndexable() {
    // given
    var resource = new Resource().setId(randomLong());
    var index = new LinkedDataWork().id(String.valueOf(resource.getId()));
    when(bibliographicSearchMessageMapper.toIndex(resource, CREATE)).thenReturn(Optional.of(index));

    // when
    kafkaSearchSender.sendWorkCreated(resource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(resourceIndexEventMessageProducer).sendMessages(messageCaptor.capture());
    List<ResourceIndexEvent> messages = messageCaptor.getValue();
    assertThat(messages).hasSize(1);
    var message = messages.get(0);
    assertThat(message.getId()).isNotNull();
    assertThat(message.getType()).isEqualTo(CREATE);
    assertThat(message.getResourceName()).isEqualTo(SEARCH_RESOURCE_NAME);
    assertThat(message.getNew()).isEqualTo(index);
    verify(eventPublisher).publishEvent(new ResourceIndexedEvent(parseLong(index.getId())));
  }

  @Test
  void sendMultipleWorksCreated_shouldReturnFalseAndNotSendMessage_ifGivenWorksIsNotIndexable() {
    // given
    var resource = new Resource();
    when(bibliographicSearchMessageMapper.toIndex(resource, CREATE)).thenReturn(Optional.empty());

    // when
    var result = kafkaSearchSender.sendMultipleWorksCreated(resource);

    // then
    assertThat(result).isFalse();
    verify(resourceIndexEventMessageProducer, never()).sendMessages(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendMultipleWorksCreated_shouldReturnTrueAndSendMessageButNotPublishEvent_ifGivenWorksIsIndexable() {
    // given
    var resource = new Resource().setId(randomLong());
    var index = new LinkedDataWork().id(String.valueOf(resource.getId()));
    when(bibliographicSearchMessageMapper.toIndex(resource, CREATE)).thenReturn(Optional.of(index));

    // when
    var result = kafkaSearchSender.sendMultipleWorksCreated(resource);

    // then
    assertThat(result).isTrue();
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(resourceIndexEventMessageProducer).sendMessages(messageCaptor.capture());
    List<ResourceIndexEvent> messages = messageCaptor.getValue();
    assertThat(messages).hasSize(1);
    var message = messages.get(0);
    assertThat(message.getId()).isNotNull();
    assertThat(message.getType()).isEqualTo(CREATE);
    assertThat(message.getResourceName()).isEqualTo(SEARCH_RESOURCE_NAME);
    assertThat(message.getNew()).isEqualTo(index);
    verify(eventPublisher, never()).publishEvent(new ResourceIndexedEvent(parseLong(index.getId())));
  }

  @Test
  void sendWorkUpdated_shouldNotSendMessage_ifNewResourceIsNotIndexableAndOldWorkToo() {
    // given
    var newResource = new Resource().setId(1L);
    var oldResource = new Resource().setId(2L);
    when(bibliographicSearchMessageMapper.toIndex(newResource, UPDATE)).thenReturn(Optional.empty());
    when(bibliographicSearchMessageMapper.toDeleteIndexId(oldResource)).thenReturn(Optional.empty());

    // when
    kafkaSearchSender.sendWorkUpdated(newResource, oldResource);

    // then
    verify(resourceIndexEventMessageProducer, never()).sendMessages(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendWorkUpdated_shouldSendUpdate_ifNewWorkIsIndexableAndKeepsSameId() {
    // given
    long id = 1L;
    var newResource = new Resource().setId(id).setLabel("new");
    var oldResource = new Resource().setId(id).setLabel("old");
    var indexNew = new LinkedDataWork().id(String.valueOf(id));
    var indexOld = new LinkedDataWork().id(String.valueOf(id)).addLanguagesItem(new BibframeLanguagesInner());
    when(bibliographicSearchMessageMapper.toIndex(newResource, UPDATE))
      .thenReturn(Optional.of(indexNew))
      .thenReturn(Optional.of(indexOld));

    // when
    kafkaSearchSender.sendWorkUpdated(newResource, oldResource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(resourceIndexEventMessageProducer).sendMessages(messageCaptor.capture());
    List<ResourceIndexEvent> messages = messageCaptor.getValue();
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
  void sendWorkUpdated_shouldSendDeleteAndCreate_ifNewWorkHasNewIdAndBothResourcesAreIndexable() {
    // given
    Long newId = 1L;
    Long oldId = 2L;
    var newResource = new Resource().setId(newId).setLabel("new");
    var oldResource = new Resource().setId(oldId).setLabel("old");
    var indexNew = new LinkedDataWork().id(newId.toString());
    when(bibliographicSearchMessageMapper.toIndex(newResource, UPDATE)).thenReturn(Optional.of(indexNew));
    when(bibliographicSearchMessageMapper.toDeleteIndexId(oldResource)).thenReturn(Optional.of(oldId));

    // when
    kafkaSearchSender.sendWorkUpdated(newResource, oldResource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(resourceIndexEventMessageProducer, times(2)).sendMessages(messageCaptor.capture());
    List<ResourceIndexEvent> messages = messageCaptor.getAllValues().get(0);
    assertThat(messages).hasSize(1);
    var message = messages.get(0);
    assertThat(message.getId()).isNotNull();
    assertThat(message.getType()).isEqualTo(DELETE);
    assertThat(message.getResourceName()).isEqualTo(SEARCH_RESOURCE_NAME);
    assertThat(message.getNew()).isEqualTo(new LinkedDataWork(oldId.toString()));
    List<ResourceIndexEvent> messages2 = messageCaptor.getAllValues().get(1);
    assertThat(messages2).hasSize(1);
    var message2 = messages2.get(0);
    assertThat(message2.getId()).isNotNull();
    assertThat(message2.getType()).isEqualTo(CREATE);
    assertThat(message2.getResourceName()).isEqualTo(SEARCH_RESOURCE_NAME);
    assertThat(message2.getNew()).isEqualTo(indexNew);
    verify(eventPublisher).publishEvent(new ResourceIndexedEvent(newId));
  }

  @Test
  void sendWorkUpdated_shouldSendDelete_ifNewWorkIsNotIndexableButOldIs() {
    // given
    Long newId = 1L;
    Long oldId = 2L;
    var newResource = new Resource().setId(newId).setLabel("new");
    var oldResource = new Resource().setId(oldId).setLabel("old");
    when(bibliographicSearchMessageMapper.toIndex(newResource, UPDATE)).thenReturn(Optional.empty());
    when(bibliographicSearchMessageMapper.toDeleteIndexId(oldResource)).thenReturn(Optional.of(oldId));

    // when
    kafkaSearchSender.sendWorkUpdated(newResource, oldResource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(resourceIndexEventMessageProducer).sendMessages(messageCaptor.capture());
    List<ResourceIndexEvent> messages = messageCaptor.getValue();
    assertThat(messages).hasSize(1);
    var message = messages.get(0);
    assertThat(message.getId()).isNotNull();
    assertThat(message.getType()).isEqualTo(DELETE);
    assertThat(message.getResourceName()).isEqualTo(SEARCH_RESOURCE_NAME);
    assertThat(message.getNew()).isEqualTo(new LinkedDataWork(oldId.toString()));
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendWorkUpdated_shouldSendNothing_ifBothResourcesAreNotIndexable() {
    // given
    Long newId = 1L;
    Long oldId = 2L;
    var newResource = new Resource().setId(newId).setLabel("new");
    var oldResource = new Resource().setId(oldId).setLabel("old");
    when(bibliographicSearchMessageMapper.toIndex(newResource, UPDATE)).thenReturn(Optional.empty());
    when(bibliographicSearchMessageMapper.toDeleteIndexId(oldResource)).thenReturn(Optional.empty());

    // when
    kafkaSearchSender.sendWorkUpdated(newResource, oldResource);

    // then
    verify(resourceIndexEventMessageProducer, never()).sendMessages(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendWorkDeleted_shouldSendDelete_ifResourcesIsIndexable() {
    // given
    Long id = 1L;
    var resource = new Resource().setId(id);
    when(bibliographicSearchMessageMapper.toDeleteIndexId(resource)).thenReturn(Optional.of(id));

    // when
    kafkaSearchSender.sendWorkDeleted(resource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(resourceIndexEventMessageProducer).sendMessages(messageCaptor.capture());
    List<ResourceIndexEvent> messages = messageCaptor.getValue();
    assertThat(messages).hasSize(1);
    var message = messages.get(0);
    assertThat(message.getId()).isNotNull();
    assertThat(message.getType()).isEqualTo(DELETE);
    assertThat(message.getResourceName()).isEqualTo(SEARCH_RESOURCE_NAME);
    assertThat(message.getNew()).isEqualTo(new LinkedDataWork(id.toString()));
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void sendWorkDeleted_shouldSendNothing_ifResourcesIsNotIndexable() {
    // given
    Long id = 1L;
    var resource = new Resource().setId(id);
    when(bibliographicSearchMessageMapper.toDeleteIndexId(resource)).thenReturn(Optional.empty());

    // when
    kafkaSearchSender.sendWorkDeleted(resource);

    // then
    verify(resourceIndexEventMessageProducer, never()).sendMessages(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

}
