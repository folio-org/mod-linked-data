package org.folio.linked.data.integration.kafka.sender.search;

import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.mapper.kafka.search.HubSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class HubDeleteMessageSenderTest {

  @InjectMocks
  private HubDeleteMessageSender sender;
  @Mock
  private FolioMessageProducer<ResourceIndexEvent> hubIndexMessageProducer;
  @Mock
  private HubSearchMessageMapper mapper;

  @Test
  void produce_shouldSendDeleteMessage_ifResourceIsHub() {
    // given
    var resource = new Resource().addTypes(HUB).setIdAndRefreshEdges(456L);
    var onlyIdResource = new Resource().setIdAndRefreshEdges(resource.getId());
    var expectedMessage = new ResourceIndexEvent().id(String.valueOf(resource.getId()));
    when(mapper.toIndex(onlyIdResource)).thenReturn(expectedMessage);

    // when
    sender.produce(resource);

    // then
    verify(hubIndexMessageProducer).sendMessages(List.of(expectedMessage));
    verify(mapper).toIndex(onlyIdResource);
  }

  @Test
  void produce_shouldNotSendDeleteMessage_ifResourceIsNotHub() {
    // given
    var resource = new Resource().addTypes(FAMILY);

    // when
    sender.produce(resource);

    // then
    verifyNoInteractions(hubIndexMessageProducer, mapper);
  }
}
