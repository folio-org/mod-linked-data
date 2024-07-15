package org.folio.linked.data.mapper.kafka.inventory;

import static java.util.Objects.isNull;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.mapper.ResourceModelMapper;
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
    if (isNull(instance) || isNull(instance.getId()) || instance.isNotOfType(INSTANCE)) {
      return Optional.empty();
    }
    var resourceModel = resourceModelMapper.toModel(instance);
    var marcJson = bibframe2MarcMapper.toMarcJson(resourceModel);
    if (isNull(marcJson)) {
      return Optional.empty();
    }
    var payload = new InstanceIngressPayload()
      .sourceRecordIdentifier(instance.getInstanceMetadata().getInventoryId())
      .sourceType(InstanceIngressPayload.SourceTypeEnum.LINKED_DATA)
      .sourceRecordObject(marcJson)
      .putAdditionalProperty(LINKED_DATA_ID, instance.getId());
    var event = new InstanceIngressEvent()
      .id(String.valueOf(instance.getId()))
      .eventPayload(payload);
    return Optional.of(event);
  }

}
