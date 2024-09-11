package org.folio.linked.data.integration.kafka.listener.handler;

import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum.CREATED;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_AUTHORITY;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_BIB;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.ld.dictionary.model.FolioMetadata;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.SourceRecordDomainEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.service.resource.ResourceMarcService;
import org.folio.marc4ld.service.marc2ld.authority.MarcAuthority2ldMapper;
import org.folio.marc4ld.service.marc2ld.bib.MarcBib2ldMapper;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SourceRecordDomainEventHandlerTest {

  @InjectMocks
  private SourceRecordDomainEventHandler sourceRecordDomainEventHandler;

  @Mock
  private MarcBib2ldMapper marcBib2ldMapper;
  @Mock
  private MarcAuthority2ldMapper marcAuthority2ldMapper;
  @Mock
  private ResourceMarcService resourceMarcService;
  @Mock
  private FolioMetadataRepository folioMetadataRepository;

  @Test
  void shouldNotTriggerSaving_ifIncomingEventHasNoMarcInside() {
    // given
    var event = new SourceRecordDomainEvent().id("1");

    // when
    sourceRecordDomainEventHandler.handle(event, MARC_BIB);

    // then
    verifyNoInteractions(resourceMarcService);
  }

  @Test
  void shouldNotTriggerSaving_ifIncomingEventContainsNotSupportedRecordType() {
    // given
    var event = new SourceRecordDomainEvent().id("2")
      .eventPayload("{}");

    // when
    sourceRecordDomainEventHandler.handle(event, null);

    // then
    verifyNoInteractions(resourceMarcService);
  }

  @Test
  void shouldNotTriggerSaving_ifIncomingEventContainsNotSupportedEventType() {
    // given
    var event = new SourceRecordDomainEvent().id("3")
      .eventPayload("{}");

    // when
    sourceRecordDomainEventHandler.handle(event, MARC_BIB);

    // then
    verifyNoInteractions(resourceMarcService);
  }

  @Test
  void shouldNotTriggerSaving_ifResourceMappedOutOfIncomingEventIsEmpty() {
    // given
    var event = new SourceRecordDomainEvent().id("4")
      .eventType(CREATED)
      .eventPayload("{ \"key\": \"value\"}");

    // when
    sourceRecordDomainEventHandler.handle(event, MARC_BIB);

    // then
    verifyNoInteractions(resourceMarcService);
  }

  @Test
  void shouldNotTriggerSaving_ifResourceMappedOutOfIncomingEventIsExistedByIdInstanceWithLinkedDataSource() {
    // given
    var event = new SourceRecordDomainEvent().id("5")
      .eventType(CREATED)
      .eventPayload("{ \"key\": \"value\"}");
    var mapped = new Resource().setId(4L).addType(INSTANCE);
    doReturn(Optional.of(mapped)).when(marcBib2ldMapper).fromMarcJson(event.getEventPayload());
    var existedMetaData =
      new org.folio.linked.data.model.entity.FolioMetadata(new org.folio.linked.data.model.entity.Resource())
        .setSource(LINKED_DATA);
    doReturn(Optional.of(existedMetaData)).when(folioMetadataRepository).findById(mapped.getId());

    // when
    sourceRecordDomainEventHandler.handle(event, MARC_BIB);

    // then
    verifyNoInteractions(resourceMarcService);
  }

  @Test
  void shouldNotTriggerSaving_ifResourceMappedOutOfIncomingEventIsExistedBySrsIdInstanceWithLinkedDataSource() {
    // given
    var event = new SourceRecordDomainEvent().id("6")
      .eventType(CREATED)
      .eventPayload("{ \"key\": \"value\"}");
    var mapped = new Resource().setId(4L).addType(INSTANCE)
      .setFolioMetadata(new FolioMetadata().setSrsId(UUID.randomUUID().toString()));
    doReturn(Optional.of(mapped)).when(marcBib2ldMapper).fromMarcJson(event.getEventPayload());
    var existedMetaData =
      new org.folio.linked.data.model.entity.FolioMetadata(new org.folio.linked.data.model.entity.Resource())
        .setSource(LINKED_DATA);
    doReturn(Optional.of(existedMetaData))
      .when(folioMetadataRepository).findBySrsId(mapped.getFolioMetadata().getSrsId());

    // when
    sourceRecordDomainEventHandler.handle(event, MARC_BIB);

    // then
    verifyNoInteractions(resourceMarcService);
  }

  @Test
  void shouldTriggerResourceSaving_forCorrectMarcBibEvent() {
    // given
    var event = new SourceRecordDomainEvent().id("7")
      .eventType(CREATED)
      .eventPayload("{ \"key\": \"value\"}");
    var mapped = new Resource().setId(7L).addType(INSTANCE);
    doReturn(Optional.of(mapped)).when(marcBib2ldMapper).fromMarcJson(event.getEventPayload());

    // when
    sourceRecordDomainEventHandler.handle(event, MARC_BIB);

    // then
    verify(resourceMarcService).saveMarcResource(mapped);
  }

  @Test
  void shouldTriggerResourceSaving_forCorrectMarcAuthorityEvent() {
    // given
    var event = new SourceRecordDomainEvent().id("8")
      .eventType(CREATED)
      .eventPayload("{ \"key\": \"value\"}");
    var mapped1 = new Resource().setId(9L).addType(PERSON);
    var mapped2 = new Resource().setId(10L).addType(CONCEPT);
    doReturn(List.of(mapped1, mapped2)).when(marcAuthority2ldMapper).fromMarcJson(event.getEventPayload());

    // when
    sourceRecordDomainEventHandler.handle(event, MARC_AUTHORITY);

    // then
    verify(resourceMarcService).saveMarcResource(mapped1);
    verify(resourceMarcService).saveMarcResource(mapped2);
  }
}
