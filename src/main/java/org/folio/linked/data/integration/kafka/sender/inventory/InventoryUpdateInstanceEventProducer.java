package org.folio.linked.data.integration.kafka.sender.inventory;

import static org.folio.linked.data.util.BibframeUtils.extractInstances;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.integration.kafka.sender.UpdateMessageSender;
import org.folio.linked.data.mapper.kafka.inventory.KafkaInventoryMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.InstanceIngressEvent;
import org.folio.search.domain.dto.InstanceIngressEvent.EventTypeEnum;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class InventoryUpdateInstanceEventProducer implements UpdateMessageSender {

  private final KafkaInventoryMessageMapper kafkaInventoryMessageMapper;
  private final FolioMessageProducer<InstanceIngressEvent> instanceIngressMessageProducer;

  @Override
  public Collection<ResourcePair> apply(Resource oldResource, Resource newResource) {
    return extractInstances(newResource)
      .stream()
      .map(i -> new ResourcePair(oldResource, i))
      .toList();
  }

  @Override
  public void accept(Resource oldResource, Resource newResource) {
    kafkaInventoryMessageMapper.toInstanceIngressEvent(newResource)
      .map(e -> e.eventType(EventTypeEnum.UPDATE_INSTANCE))
      .map(List::of)
      .ifPresent(instanceIngressMessageProducer::sendMessages);
  }

}
