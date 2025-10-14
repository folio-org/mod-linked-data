package org.folio.linked.data.service.resource.events;

import static org.folio.ld.dictionary.PredicateDictionary.EXPRESSION_OF;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceEventsPublisherImplTest {
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private ResourceRepository resourceRepository;
  @InjectMocks
  private ResourceEventsPublisherImpl resourceEventsPublisher;

  @Test
  void testPublishEventsForCreate_publishesCreatedEventAndHubEvents() {
    // given
    var work = new Resource()
      .setId(1L)
      .addTypes(WORK);
    var hub = new Resource()
      .setId(2L)
      .addTypes(HUB);
    var indexedHub = new Resource()
      .setId(3L)
      .addTypes(HUB)
      .setIndexDate(new Date());
    var expressionOf1 = new ResourceEdge(work, hub, EXPRESSION_OF);
    var expressionOf2 = new ResourceEdge(work, indexedHub, EXPRESSION_OF);
    work.addOutgoingEdge(expressionOf1);
    work.addOutgoingEdge(expressionOf2);

    when(resourceRepository.findById(hub.getId())).thenReturn(Optional.empty());
    when(resourceRepository.findById(indexedHub.getId())).thenReturn(Optional.of(indexedHub));

    // when
    resourceEventsPublisher.publishEventsForCreate(work);

    // then
    verify(applicationEventPublisher).publishEvent(new ResourceCreatedEvent(work));
    verify(applicationEventPublisher).publishEvent(new ResourceCreatedEvent(hub));
    verifyNoMoreInteractions(applicationEventPublisher);
  }

  @Test
  void testPublishEventsForCreate_shouldHandleCyclesInGraph() {
    // given
    var work = new Resource()
      .setId(1L)
      .addTypes(WORK);
    var hub = new Resource()
      .setId(2L)
      .addTypes(HUB);

    // Create cycle: work → hub → work
    var edgeToHub = new ResourceEdge(work, hub, EXPRESSION_OF);
    var edgeToWork = new ResourceEdge(hub, work, EXPRESSION_OF);
    work.addOutgoingEdge(edgeToHub);
    hub.addOutgoingEdge(edgeToWork);

    when(resourceRepository.findById(hub.getId())).thenReturn(Optional.empty());

    // when
    resourceEventsPublisher.publishEventsForCreate(work);

    // then
    verify(applicationEventPublisher).publishEvent(new ResourceCreatedEvent(work));
    verify(applicationEventPublisher).publishEvent(new ResourceCreatedEvent(hub));
    verifyNoMoreInteractions(applicationEventPublisher);
  }

  @Test
  void testPublishEventsForUpdate_publishesUpdatedEventAndHubEvents() {
    // given
    var work = new Resource()
      .setId(1L)
      .addTypes(WORK);
    var hub = new Resource()
      .setId(2L)
      .addTypes(HUB);
    var expressionOf = new ResourceEdge(work, hub, EXPRESSION_OF);
    work.addOutgoingEdge(expressionOf);
    when(resourceRepository.findById(hub.getId())).thenReturn(Optional.empty());

    // when
    resourceEventsPublisher.publishEventsForUpdate(work);

    // then
    verify(applicationEventPublisher).publishEvent(new ResourceUpdatedEvent(work));
    verify(applicationEventPublisher).publishEvent(new ResourceCreatedEvent(hub));
    verifyNoMoreInteractions(applicationEventPublisher);
  }

  @Test
  void testPublishEventsForUpdate_publishesUpdatedEvent_whenOldResourceIdIsSameAsNewResourceId() {
    // given
    var oldWork = new Resource()
      .setId(1L)
      .addTypes(WORK);
    var newWork = new Resource()
      .setId(1L)
      .addTypes(WORK);

    // when
    resourceEventsPublisher.publishEventsForUpdate(oldWork, newWork);

    // then
    verify(applicationEventPublisher).publishEvent(new ResourceUpdatedEvent(newWork));
    verifyNoMoreInteractions(applicationEventPublisher);
  }

  @Test
  void testPublishEventsForUpdate_publishesReplacedEventAndHubEvents_whenOldResourceIdIsDifferentFromNewResourceId() {
    // given
    var oldWork = new Resource()
      .setId(100L)
      .addTypes(WORK);
    var newWork = new Resource()
      .setId(1L)
      .addTypes(WORK);
    var hub = new Resource()
      .setId(2L)
      .addTypes(HUB);
    var expressionOf = new ResourceEdge(newWork, hub, EXPRESSION_OF);
    newWork.addOutgoingEdge(expressionOf);
    when(resourceRepository.findById(hub.getId())).thenReturn(Optional.empty());

    // when
    resourceEventsPublisher.publishEventsForUpdate(oldWork, newWork);

    // then
    verify(applicationEventPublisher).publishEvent(new ResourceReplacedEvent(oldWork, newWork.getId()));
    verify(applicationEventPublisher).publishEvent(new ResourceCreatedEvent(hub));
    verifyNoMoreInteractions(applicationEventPublisher);
  }

  @Test
  void testPublishEventsForDelete_publishesDeletedEvent() {
    // given
    var work = new Resource()
      .setId(1L)
      .addTypes(WORK);

    // when
    resourceEventsPublisher.publishEventsForDelete(work);

    // then
    verify(applicationEventPublisher).publishEvent(new ResourceDeletedEvent(work));
    verifyNoMoreInteractions(applicationEventPublisher);
  }
}
