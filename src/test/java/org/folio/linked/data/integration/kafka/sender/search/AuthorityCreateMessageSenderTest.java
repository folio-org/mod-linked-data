package org.folio.linked.data.integration.kafka.sender.search;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.search.domain.dto.ResourceIndexEventType.CREATE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.mapper.kafka.search.AuthoritySearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceIndexedEvent;
import org.folio.search.domain.dto.LinkedDataAuthority;
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
class AuthorityCreateMessageSenderTest {

  @InjectMocks
  private AuthorityCreateMessageSender producer;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private AuthoritySearchMessageMapper authoritySearchMessageMapper;
  @Mock
  private FolioMessageProducer<ResourceIndexEvent> authorityMessageProducer;

  @Test
  void sendAuthorityCreated_shouldNotSendMessageAndPublishEvent_ifAuthorityAlreadyIndexed() {
    // given
    var resource = new Resource().addTypes(PERSON).setId(randomLong());
    resource.setIndexDate(new Date());

    // when
    producer.produce(resource, true);

    // then
    verifyNoInteractions(eventPublisher, authorityMessageProducer);
  }

  @Test
  void sendAuthorityCreated_shouldSendMessageAndPublishEvent_ifAuthorityNotIndexed() {
    // given
    var resource = new Resource().addTypes(PERSON).setId(randomLong());
    var expectedMessage = new ResourceIndexEvent()
      .id(String.valueOf(resource.getId()));
    when(authoritySearchMessageMapper.toIndex(resource)).thenReturn(expectedMessage);
    var putIndexDate = true;

    // when
    producer.produce(resource, putIndexDate);

    // then
    verifyMessageAndEvent(expectedMessage, putIndexDate);
  }

  @Test
  void sendAuthorityCreated_shouldSendMessageButNotPublishEvent_ifAuthorityNotIndexed() {
    // given
    var resource = new Resource().addTypes(PERSON).setId(randomLong());
    var expectedMessage = new ResourceIndexEvent()
      .id(String.valueOf(resource.getId()));
    when(authoritySearchMessageMapper.toIndex(resource)).thenReturn(expectedMessage);
    var putIndexDate = false;

    // when
    producer.produce(resource, putIndexDate);

    // then
    verifyMessageAndEvent(expectedMessage, putIndexDate);
  }

  @Test
  void sendAuthorityCreated_shouldNotSendMessageAndPublishEvent_ifAuthorityAlreadyIndexedAndLinkedToWork() {
    // given
    var work = new Resource().addTypes(WORK).setId(randomLong());
    var authority = new Resource().addTypes(PERSON).setId(randomLong());
    authority.setIndexDate(new Date());
    work.getOutgoingEdges().add(new ResourceEdge(work, authority, PredicateDictionary.NULL));

    // when
    producer.produce(work, true);

    // then
    verifyNoInteractions(eventPublisher, authorityMessageProducer);
  }

  @Test
  void sendAuthorityCreated_shouldSendMessageAndPublishEvent_ifAuthorityLinkedToWork() {
    // given
    var work = new Resource().addTypes(WORK).setId(randomLong());
    var person = new Resource().addTypes(PERSON)
      .setId(randomLong())
      .setManaged(true);
    var concept = new Resource().addTypes(CONCEPT)
      .setId(randomLong())
      .setIndexDate(new Date());
    var family = new Resource().addTypes(FAMILY)
      .setId(randomLong())
      .setManaged(true)
      .setIndexDate(new Date());
    work.addOutgoingEdge(new ResourceEdge(work, person, PredicateDictionary.NULL));
    work.addOutgoingEdge(new ResourceEdge(work, concept, PredicateDictionary.NULL));
    work.addOutgoingEdge(new ResourceEdge(work, family, PredicateDictionary.NULL));

    var index = new ResourceIndexEvent()
      .id(String.valueOf(person.getId()))
      ._new(new LinkedDataAuthority().id(String.valueOf(person.getId())));
    when(authoritySearchMessageMapper.toIndex(person)).thenReturn(index);
    var putIndexDate = true;

    // when
    producer.produce(work, putIndexDate);

    // then
    verifyMessageAndEvent(index, putIndexDate);
  }

  @Test
  void sendAuthorityCreated_shouldSendMessageButNotPublishEvent_ifAuthorityLinkedToWork() {
    // given
    var work = new Resource().addTypes(WORK).setId(randomLong());
    var person = new Resource().addTypes(PERSON)
      .setId(randomLong())
      .setManaged(true);
    var concept = new Resource().addTypes(CONCEPT)
      .setId(randomLong())
      .setIndexDate(new Date());
    var family = new Resource().addTypes(FAMILY)
      .setId(randomLong())
      .setManaged(true)
      .setIndexDate(new Date());
    work.addOutgoingEdge(new ResourceEdge(work, person, PredicateDictionary.NULL));
    work.addOutgoingEdge(new ResourceEdge(work, concept, PredicateDictionary.NULL));
    work.addOutgoingEdge(new ResourceEdge(work, family, PredicateDictionary.NULL));

    var index = new ResourceIndexEvent()
      .id(String.valueOf(person.getId()))
      ._new(new LinkedDataAuthority().id(String.valueOf(person.getId())));
    when(authoritySearchMessageMapper.toIndex(person)).thenReturn(index);
    var putIndexDate = false;

    // when
    producer.produce(work, putIndexDate);

    // then
    verifyMessageAndEvent(index, putIndexDate);
  }

  private void verifyMessageAndEvent(ResourceIndexEvent expectedMessage, boolean putIndexDate) {
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(authorityMessageProducer).sendMessages(messageCaptor.capture());
    assertThat(messageCaptor.getValue()).containsOnly(expectedMessage);
    assertThat(expectedMessage.getType()).isEqualTo(CREATE);
    if (putIndexDate) {
      var expectedIndexEvent = new ResourceIndexedEvent(parseLong(expectedMessage.getId()));
      verify(eventPublisher).publishEvent(expectedIndexEvent);
    } else {
      verifyNoInteractions(eventPublisher);
    }
  }
}
