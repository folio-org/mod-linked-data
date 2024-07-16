package org.folio.linked.data.service.resource.meta;

import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;

import java.util.UUID;
import java.util.function.Consumer;
import org.folio.linked.data.model.entity.InstanceMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Service;

@Service
public class MetadataServiceImpl implements MetadataService {

  @Override
  public void ensureMetadata(Resource resource) {
    ensure(resource, metadata ->
      metadata.setInventoryId(UUID.randomUUID().toString())
    );
  }

  @Override
  public void ensureMetadata(Resource resource, InstanceMetadata oldResourceMeta) {
    ensure(resource, metadata -> {
      metadata.setInventoryId(oldResourceMeta.getInventoryId());
      metadata.setSrsId(oldResourceMeta.getSrsId());
    });
  }

  private void ensure(Resource resource, Consumer<InstanceMetadata> metadataConsumer) {
    if (resource.isNotOfType(INSTANCE)) {
      return;
    }
    var metadata = new InstanceMetadata(resource)
      .setResource(resource)
      .setSource(LINKED_DATA);
    metadataConsumer.accept(metadata);
    resource.setInstanceMetadata(metadata);
  }
}
