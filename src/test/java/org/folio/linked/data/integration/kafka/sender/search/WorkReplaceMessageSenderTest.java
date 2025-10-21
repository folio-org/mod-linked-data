package org.folio.linked.data.integration.kafka.sender.search;

import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class WorkReplaceMessageSenderTest {

  @InjectMocks
  private WorkReplaceMessageSender producer;
  @Mock
  private WorkDeleteMessageSender workDeleteMessageSender;
  @Mock
  private WorkUpdateMessageSender workUpdateMessageSender;
  @Mock
  private WorkCreateMessageSender workCreateMessageSender;

  @Test
  void produce_shouldDoNothing_ifGivenNewResourceIsNotWorkOrInstance() {
    // given
    var old = new Resource();
    var resource = new Resource().addTypes(FAMILY);

    // when
    producer.produce(old, resource);

    // then
    verifyNoInteractions(workDeleteMessageSender, workUpdateMessageSender, workCreateMessageSender);
  }

  @Test
  void produce_shouldSendDeleteAndCreateEvents_ifNewResourceIsWork() {
    // given
    long id = 1L;
    var newResource = new Resource().setIdAndRefreshEdges(id).setLabel("new").addTypes(WORK);
    var old = new Resource();

    // when
    producer.produce(old, newResource);

    // then
    verifyNoInteractions(workUpdateMessageSender);
    verify(workDeleteMessageSender).produce(old);
    verify(workCreateMessageSender).produce(newResource);
  }

  @Test
  void produce_shouldSendUpdateWorkEvent_ifResourceIsInstance() {
    // given
    var newInstance = new Resource().setIdAndRefreshEdges(1L).setLabel("newInstance").addTypes(INSTANCE);
    var work = new Resource().setIdAndRefreshEdges(3L).setLabel("work").addTypes(WORK);
    newInstance.addOutgoingEdge(new ResourceEdge(newInstance, work, INSTANTIATES));
    var old = new Resource();

    // when
    producer.produce(old, newInstance);

    // then
    verifyNoInteractions(workDeleteMessageSender, workCreateMessageSender);
    verify(workUpdateMessageSender).produce(work);
  }

}
