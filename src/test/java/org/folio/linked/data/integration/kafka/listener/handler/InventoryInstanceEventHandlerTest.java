package org.folio.linked.data.integration.kafka.listener.handler;

import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.folio.linked.data.domain.dto.InventoryInstanceEvent;
import org.folio.linked.data.domain.dto.ResourceIndexEventType;
import org.folio.linked.data.integration.kafka.sender.search.WorkUpdateMessageSender;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.test.TestUtil;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class InventoryInstanceEventHandlerTest {

  @Mock
  private WorkUpdateMessageSender workUpdateMessageSender;
  @Mock
  private FolioMetadataRepository folioMetadataRepository;
  @InjectMocks
  private InventoryInstanceEventHandler handler;

  private void verifyNoReindexInteractions() {
    verifyNoInteractions(workUpdateMessageSender);
  }

  @Test
  void shouldNotReindex_ifSuppressFlagsNotChanged() {
    // given
    var event = getEventWithNoChangesInSuppressFlags();

    // when
    handler.handle(event, null);

    // then
    verifyNoReindexInteractions();
  }

  @Test
  void shouldNotReindex_ifEventTypeNotEqualToUpdate() {
    // given
    var event = getEventWithTypeCreate();

    // when
    handler.handle(event, null);

    // then
    verifyNoReindexInteractions();
  }

  @Test
  void shouldNotReindex_ifInstanceIsNull() {
    // given
    var event = getEventWithNullableObject();

    // when
    handler.handle(event, null);

    // then
    verifyNoReindexInteractions();
  }

  @Test
  void shouldNotReindex_ifInstanceNotExistsInRepo() {
    // given
    var event = getEvent();

    // when
    handler.handle(event, null);

    // then
    verifyNoReindexInteractions();
  }

  @Test
  void shouldHandle_ifSuppressFlagsChangedAndInstanceExists() {
    // given
    var event = getEventWithChangesInSuppressFlags();
    var resourceMock = mock(Resource.class);
    var metadataMock = metadataWithResource(resourceMock);
    when(folioMetadataRepository.findByInventoryId(any())).thenReturn(Optional.of(metadataMock));
    when(folioMetadataRepository.save(metadataMock)).thenReturn(metadataMock);

    // when
    handler.handle(event, null);

    // then
    verify(metadataMock).setStaffSuppress(event.getNew().getStaffSuppress());
    verify(metadataMock).setSuppressFromDiscovery(event.getNew().getDiscoverySuppress());
    verify(folioMetadataRepository).save(metadataMock);
    verify(workUpdateMessageSender).produce(resourceMock);
  }

  private FolioMetadata metadataWithResource(Resource resource) {
    var metadataMock = mock(FolioMetadata.class);
    when(metadataMock.getResource()).thenReturn(resource);
    return metadataMock;
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
    return TEST_JSON_MAPPER.readValue(eventSource, InventoryInstanceEvent.class);
  }
}
