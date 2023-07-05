package org.folio.linked.data.service.dictionary;

import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.repo.ResourceTypeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceTypeService implements DictionaryService<ResourceType> {

  private final ResourceTypeRepository repo;

  @Override
  public ResourceType get(String key) {
    return repo.findBySimpleLabel(key).or(() -> repo.findByTypeUri(key))
      .orElseThrow(() -> new NotSupportedException(RESOURCE_TYPE + key + IS_NOT_SUPPORTED));
  }

}
