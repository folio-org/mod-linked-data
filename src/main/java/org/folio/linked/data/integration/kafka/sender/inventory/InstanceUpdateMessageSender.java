package org.folio.linked.data.integration.kafka.sender.inventory;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.folio.linked.data.domain.dto.InstanceIngressEvent.EventTypeEnum.UPDATE_INSTANCE;
import static org.folio.linked.data.model.entity.ResourceSource.MARC;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.folio.linked.data.util.ResourceUtils.extractInstancesFromWork;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.InstanceIngressEvent;
import org.folio.linked.data.integration.kafka.sender.UpdateMessageSender;
import org.folio.linked.data.mapper.kafka.inventory.InstanceIngressMessageMapper;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class InstanceUpdateMessageSender implements UpdateMessageSender {

  private final InstanceIngressMessageMapper instanceIngressMessageMapper;
  private final FolioMessageProducer<InstanceIngressEvent> instanceIngressMessageProducer;

  @Override
  public Collection<Resource> apply(Resource resource) {
    if (isSourcedFromMarc(resource)) {
      return emptyList();
    }
    return extractInstancesFromWork(resource);
  }

  @Override
  public void accept(Resource resource) {
    log.info("Publishing UPDATE_INSTANCE message to inventory for instance with ID [{}]", resource.getId());
    var message = instanceIngressMessageMapper.toInstanceIngressEvent(resource)
      .eventType(UPDATE_INSTANCE);
    instanceIngressMessageProducer.sendMessages(List.of(message));
  }

  private boolean isSourcedFromMarc(Resource resource) {
    return ofNullable(resource.getFolioMetadata())
      .map(FolioMetadata::getSource)
      .map(source -> source == MARC)
      .orElse(false);
  }

}
