package org.folio.linked.data.integration.kafka.sender.inventory;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.mapper.kafka.KafkaInventoryMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.InstanceIngressEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class KafkaInventorySenderImpl implements KafkaInventorySender {

  private final KafkaInventoryMessageMapper kafkaInventoryMessageMapper;
  private final FolioMessageProducer<InstanceIngressEvent> instanceIngressMessageProducer;

  @Override
  @SneakyThrows
  public void sendInstanceCreated(Resource resource) {
    kafkaInventoryMessageMapper.toInstanceIngressPayload(resource)
      .ifPresent(p -> {
          var id = UUID.randomUUID().toString();
          var event = new InstanceIngressEvent()
            .id(id)
            .eventType(InstanceIngressEvent.EventTypeEnum.CREATE_INSTANCE)
            .eventPayload(p);
          instanceIngressMessageProducer.sendMessages(List.of(event));
        }
      );
  }

}
