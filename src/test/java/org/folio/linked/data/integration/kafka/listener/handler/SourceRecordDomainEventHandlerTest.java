package org.folio.linked.data.integration.kafka.listener.handler;

import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum.SOURCE_RECORD_CREATED;
import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum.SOURCE_RECORD_UPDATED;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_AUTHORITY;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_BIB;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Optional;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ParsedRecord;
import org.folio.linked.data.domain.dto.SourceRecord;
import org.folio.linked.data.domain.dto.SourceRecordDomainEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.folio.linked.data.service.resource.marc.ResourceMarcBibService;
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
  private ResourceMarcAuthorityService resourceMarcAuthorityService;
  @Mock
  private ResourceMarcBibService resourceMarcBibService;
  @Mock
  private FolioMetadataRepository folioMetadataRepository;

  @Test
  void shouldNotTriggerSaving_ifIncomingEventHasNoMarcInside() {
    // given
    var event = new SourceRecordDomainEvent().id("1");

    // when
    sourceRecordDomainEventHandler.handle(event, MARC_BIB);

    // then
    verifyNoInteractions(resourceMarcAuthorityService);
    verifyNoInteractions(resourceMarcBibService);
  }

  @Test
  void shouldNotTriggerSaving_ifIncomingEventContainsNotSupportedRecordType() {
    // given
    var event = new SourceRecordDomainEvent().id("2")
      .eventPayload(new SourceRecord().parsedRecord(new ParsedRecord("{}")));

    // when
    sourceRecordDomainEventHandler.handle(event, null);

    // then
    verifyNoInteractions(resourceMarcAuthorityService);
    verifyNoInteractions(resourceMarcBibService);
  }

  @Test
  void shouldNotTriggerSaving_ifIncomingEventContainsNotSupportedEventType() {
    // given
    var event = new SourceRecordDomainEvent().id("3")
      .eventPayload(new SourceRecord().parsedRecord(new ParsedRecord("{}")));

    // when
    sourceRecordDomainEventHandler.handle(event, MARC_BIB);

    // then
    verifyNoInteractions(resourceMarcAuthorityService);
    verifyNoInteractions(resourceMarcBibService);
  }

  @Test
  void shouldNotTriggerSaving_ifResourceMappedOutOfIncomingEventIsEmpty() {
    // given
    var event = new SourceRecordDomainEvent().id("4")
      .eventType(SOURCE_RECORD_CREATED)
      .eventPayload(new SourceRecord().parsedRecord(new ParsedRecord("{ \"key\": \"value\"}")));

    // when
    sourceRecordDomainEventHandler.handle(event, MARC_BIB);

    // then
    verifyNoInteractions(resourceMarcAuthorityService);
    verifyNoInteractions(resourceMarcBibService);
  }

  @Test
  void shouldTriggerAuthoritySaving_forCorrectMarcAuthorityEvent() {
    // given
    String marc = """
        {
           "fields":[
              { "010":{ "subfields":[ { "a":"no2023016747" } ] } }
           ]
        }
      """;
    var event = new SourceRecordDomainEvent().id("8")
      .eventType(SOURCE_RECORD_CREATED)
      .eventPayload(new SourceRecord().parsedRecord(new ParsedRecord(marc)));
    var mapped1 = new Resource().setId(9L).addType(PERSON);
    var mapped2 = new Resource().setId(10L).addType(CONCEPT);
    doReturn(List.of(mapped1, mapped2)).when(marcAuthority2ldMapper)
      .fromMarcJson(event.getEventPayload().getParsedRecord().getContent());

    // when
    sourceRecordDomainEventHandler.handle(event, MARC_AUTHORITY);

    // then
    verify(resourceMarcAuthorityService).saveMarcAuthority(mapped1);
    verify(resourceMarcAuthorityService).saveMarcAuthority(mapped2);
    verifyNoInteractions(resourceMarcBibService);
  }

  @Test
  void shouldTriggerAdminMetadataSaving_forCorrectMarcBibEvent() {
    // given
    var event = new SourceRecordDomainEvent().id("7")
      .eventType(SOURCE_RECORD_CREATED)
      .eventPayload(new SourceRecord()
        .parsedRecord(new ParsedRecord("{\"fields\": [{\"999\": {\"subfields\": [{\"l\": \"lvalue\"}]}}]}")));
    var mapped = new Resource().setId(9L).addType(INSTANCE);
    doReturn(Optional.of(mapped)).when(marcBib2ldMapper)
      .fromMarcJson(event.getEventPayload().getParsedRecord().getContent());

    // when
    sourceRecordDomainEventHandler.handle(event, MARC_BIB);

    // then
    verify(resourceMarcBibService).saveAdminMetadata(mapped);
    verifyNoInteractions(resourceMarcAuthorityService);
  }

  @Test
  void shouldNotTriggerAdminMetadataSaving_forUpdateMarcBibEvent() {
    // given
    var event = new SourceRecordDomainEvent().id("7")
      .eventType(SOURCE_RECORD_UPDATED)
      .eventPayload(new SourceRecord().parsedRecord(new ParsedRecord("{ \"key\": \"value\"}")));

    // when
    sourceRecordDomainEventHandler.handle(event, MARC_BIB);

    // then
    verifyNoInteractions(resourceMarcBibService);
    verifyNoInteractions(resourceMarcAuthorityService);
  }
}
