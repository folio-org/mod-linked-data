package org.folio.linked.data.integration.kafka.listener.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.linked.data.domain.dto.InventoryInstanceEvent;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.domain.dto.ResourceIndexEventType;
import org.folio.linked.data.mapper.kafka.inventory.InventoryInstanceMapper;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.test.TestUtil;
import org.folio.spring.testing.type.UnitTest;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@UnitTest
@ExtendWith(MockitoExtension.class)
public class InventoryInstanceEventHandlerTest {

  private static final ObjectMapper MAPPER = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Mock
  private FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private InventoryInstanceMapper inventoryInstanceMapper;
  @Mock
  private FolioMetadataRepository folioMetadataRepository;
  @InjectMocks
  private InventoryInstanceEventHandler handler;

  private void verifyNoReindexInteractions() {
    verifyNoInteractions(inventoryInstanceMapper);
    verifyNoInteractions(bibliographicMessageProducer);
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void shouldNotReindex_ifSuppressFlagsNotChanged() throws JsonProcessingException {
    // given
    var event = getEventWithNoChangesInSuppressFlags();

    // when
    handler.handle(event);

    // then
    verifyNoReindexInteractions();
  }

  @Test
  void shouldNotReindex_ifEventTypeNotEqualToUpdate() {
    // given
    var event = getEventWithTypeCreate();

    // when
    handler.handle(event);

    // then
    verifyNoReindexInteractions();
  }

  @Test
  void shouldNotReindex_ifInstanceIsNull() throws JsonProcessingException {
    // given
    var event = getEventWithNullableObject();

    // when
    handler.handle(event);

    // then
    verifyNoReindexInteractions();
  }

  @Test
  void shouldNotReindex_ifInstanceNotExistsInRepo() {
    // given
    var event = getEvent();
    when(folioMetadataRepository.existsByInventoryId(any())).thenReturn(false);

    // when
    handler.handle(event);

    // then
    verify(folioMetadataRepository).existsByInventoryId(any());
    verifyNoReindexInteractions();
  }

  @Test
  void shouldHandle_ifSuppressFlagsChangedAndInstanceExists() {
    // given
    var event = getEventWithChangesInSuppressFlags();
    when(inventoryInstanceMapper.toReindexEvent(event)).thenReturn(new ResourceIndexEvent());
    when(folioMetadataRepository.existsByInventoryId(event.getNew().getId())).thenReturn(true);
    doNothing().when(bibliographicMessageProducer).sendMessages(anyList());
    doNothing().when(eventPublisher).publishEvent(any(ResourceIndexEvent.class));

    // when
    handler.handle(event);

    // then
    verify(inventoryInstanceMapper).toReindexEvent(event);
    verify(bibliographicMessageProducer).sendMessages(anyList());
    verify(eventPublisher).publishEvent(any(ResourceIndexEvent.class));
    verify(folioMetadataRepository).existsByInventoryId(event.getOld().getId());
  }

  private InventoryInstanceEvent getEventWithNoChangesInSuppressFlags() {
    var event = getEvent();
    return event._new(event.getOld());
  }

  private InventoryInstanceEvent getEventWithNullableObject() {
    return getEvent()._new(null);
  }

  private InventoryInstanceEvent getEventWithTypeCreate() {
    return getEvent().type(ResourceIndexEventType.CREATE);
  }

  private InventoryInstanceEvent getEventWithChangesInSuppressFlags() {
    return getEvent();
  }

  private InventoryInstanceEvent getEvent() {
    var eventSource = TestUtil.loadResourceAsString("samples/inventoryInstanceEvent.json");
    try {
      return MAPPER.readValue(eventSource, InventoryInstanceEvent.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
