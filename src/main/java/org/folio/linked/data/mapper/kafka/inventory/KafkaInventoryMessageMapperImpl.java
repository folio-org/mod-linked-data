package org.folio.linked.data.mapper.kafka.inventory;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.InstanceMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.marc4ld.service.ld2marc.Bibframe2MarcMapper;
import org.folio.search.domain.dto.InstanceIngressEvent;
import org.folio.search.domain.dto.InstanceIngressPayload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaInventoryMessageMapperImpl implements KafkaInventoryMessageMapper {

  private static final String LINKED_DATA_ID = "linkedDataId";
  private final Bibframe2MarcMapper bibframe2MarcMapper;
  private final ResourceModelMapper resourceModelMapper;

  @Override
  public Optional<InstanceIngressEvent> toInstanceIngressEvent(Resource instance) {
    if (isNull(instance) || isNull(instance.getId())) {
      return Optional.empty();
    }
    var resourceModel = resourceModelMapper.toModel(instance);
    var marcJson = bibframe2MarcMapper.toMarcJson(resourceModel);
    if (isNull(marcJson)) {
      return Optional.empty();
    }
    var payload = new InstanceIngressPayload()
      .sourceRecordIdentifier(ensureInventoryId(instance))
      .sourceType(InstanceIngressPayload.SourceTypeEnum.LINKED_DATA)
      .sourceRecordObject(marcJson)
      .putAdditionalProperty(LINKED_DATA_ID, instance.getId());
    var event = new InstanceIngressEvent()
      .id(String.valueOf(instance.getId()))
      .eventPayload(payload);
    return Optional.of(event);
  }

  private String ensureInventoryId(Resource instance) {
    return ofNullable(instance.getInstanceMetadata())
      .map(InstanceMetadata::getInventoryId)
      .orElseGet(() -> UUID.randomUUID().toString());
  }

}
