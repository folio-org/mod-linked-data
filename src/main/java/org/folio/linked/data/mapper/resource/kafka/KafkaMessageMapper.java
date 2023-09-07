package org.folio.linked.data.mapper.resource.kafka;

import lombok.NonNull;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.BibframeIndex;

public interface KafkaMessageMapper {

  BibframeIndex toIndex(@NonNull Resource resource);

}
