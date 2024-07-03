package org.folio.linked.data.integration.kafka.sender.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.search.domain.dto.ResourceIndexEventType.DELETE;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.mapper.kafka.search.BibliographicSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.LinkedDataWork;
import org.folio.search.domain.dto.ResourceIndexEvent;
import org.folio.spring.testing.type.UnitTest;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class WorkDeleteMessageSenderTest {

  @InjectMocks
  private WorkDeleteMessageSender producer;
  @Mock
  private FolioMessageProducer<ResourceIndexEvent> resourceIndexEventMessageProducer;
  @Mock
  private BibliographicSearchMessageMapper bibliographicSearchMessageMapper;

  @Test
  void afterDelete_shouldCall_sendResourceDeleted() {
    // given
    var id = 1L;
    var resource = new Resource().setId(id).addTypes(ResourceTypeDictionary.WORK);
    when(bibliographicSearchMessageMapper.toDeleteIndexId(resource))
      .thenReturn(Optional.of(id));

    // when
    producer.produce(resource);

    // then
    var messageCaptor = ArgumentCaptor.forClass(List.class);
    verify(resourceIndexEventMessageProducer)
      .sendMessages(messageCaptor.capture());
    List<ResourceIndexEvent> messages = messageCaptor.getValue();
    var expectedIndex = new LinkedDataWork().id(String.valueOf(id));

    assertThat(messages)
      .singleElement()
      .hasFieldOrProperty("id")
      .hasFieldOrPropertyWithValue("type", DELETE)
      .hasFieldOrPropertyWithValue("resourceName", "linked-data-work")
      .hasFieldOrPropertyWithValue("_new", expectedIndex);
  }

  @Test
  void sendWorkDeleted_shouldSendNothing_ifResourcesIsNotIndexable() {
    // given
    var id = 1L;
    var resource = new Resource().setId(id).addTypes(ResourceTypeDictionary.WORK);
    when(bibliographicSearchMessageMapper.toDeleteIndexId(resource))
      .thenReturn(Optional.empty());

    // when
    producer.produce(resource);

    // then
    verify(resourceIndexEventMessageProducer, never())
      .sendMessages(ArgumentMatchers.any());
  }
}
