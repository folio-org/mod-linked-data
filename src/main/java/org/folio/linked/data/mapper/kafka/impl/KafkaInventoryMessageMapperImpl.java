package org.folio.linked.data.mapper.kafka.impl;

import static java.util.Objects.isNull;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.kafka.KafkaInventoryMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.marc4ld.service.ld2marc.Bibframe2MarcMapper;
import org.folio.search.domain.dto.InstanceIngressPayload;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class KafkaInventoryMessageMapperImpl implements KafkaInventoryMessageMapper {

  private static final String LINKED_DATA_ID = "linkedDataId";
  private final Bibframe2MarcMapper bibframe2MarcMapper;
  private final ResourceModelMapper resourceModelMapper;

  @Override
  public Optional<InstanceIngressPayload> toInstanceIngressPayload(Resource instance) {
    if (isNull(instance) || isNull(instance.getId())) {
      return Optional.empty();
    }
    var resourceModel = resourceModelMapper.toModel(instance);
    var marcJson = bibframe2MarcMapper.toMarcJson(resourceModel);
    if (isNull(marcJson)) {
      return Optional.empty();
    }
    var payload = new InstanceIngressPayload()
      .sourceRecordIdentifier(UUID.randomUUID().toString())
      .sourceType(InstanceIngressPayload.SourceTypeEnum.LINKED_DATA)
      .sourceRecordObject(marcJson)
      .putAdditionalProperty(LINKED_DATA_ID, instance.getId());
    return Optional.of(payload);
  }

}
