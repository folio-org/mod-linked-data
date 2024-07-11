package org.folio.linked.data.service.resource.meta;

import static java.util.Objects.nonNull;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;

import java.util.UUID;
import org.folio.linked.data.model.entity.InstanceMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Service;

@Service
public class MetadataServiceImpl implements MetadataService {

  @Override
  public void ensureMetadata(Resource resource) {
    ensureMetadata(resource, null);
  }

  @Override
  public void ensureMetadata(Resource resource, InstanceMetadata oldResourceMeta) {
    if (resource.isOfType(INSTANCE)) {
      var metadata = new InstanceMetadata(resource)
        .setResource(resource)
        .setSource(LINKED_DATA);
      if (nonNull(oldResourceMeta)) {
        metadata.setInventoryId(oldResourceMeta.getInventoryId());
        metadata.setSrsId(oldResourceMeta.getSrsId());
      } else {
        metadata.setInventoryId(UUID.randomUUID().toString());
      }
      resource.setInstanceMetadata(metadata);
    }
  }
}
