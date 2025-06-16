package org.folio.linked.data.service.resource.marc;

import java.util.Optional;
import org.folio.linked.data.model.entity.Resource;

public interface RawMarcService {
  Optional<String> getRawMarc(Resource resource);

  Optional<String> getRawMarc(Long resourceId);

  void saveRawMarc(Resource resource, String rawMarcContent);
}
