package org.folio.linked.data.integration.kafka.sender.search;

import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class HubReplaceMessageSenderTest {

  @InjectMocks
  private HubReplaceMessageSender sender;
  @Mock
  private HubCreateMessageSender hubCreateMessageSender;
  @Mock
  private HubDeleteMessageSender hubDeleteMessageSender;

  @Test
  void produce_shouldSendDeleteAndCreate_ifCurrentIsHub() {
    // given
    var previous = new Resource().addTypes(HUB).setIdAndRefreshEdges(1L);
    var current = new Resource().addTypes(HUB).setIdAndRefreshEdges(2L);

    // when
    sender.produce(previous, current);

    // then
    verify(hubDeleteMessageSender).produce(previous);
    verify(hubCreateMessageSender).produce(current);
  }

  @Test
  void produce_shouldNotSendDeleteOrCreate_ifCurrentIsNotHub() {
    // given
    var previous = new Resource().addTypes(HUB).setIdAndRefreshEdges(1L);
    var current = new Resource().addTypes(FAMILY).setIdAndRefreshEdges(2L);

    // when
    sender.produce(previous, current);

    // then
    verifyNoInteractions(hubDeleteMessageSender, hubCreateMessageSender);
  }
}
