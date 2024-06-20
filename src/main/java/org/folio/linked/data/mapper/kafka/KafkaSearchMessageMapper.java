package org.folio.linked.data.mapper.kafka;

import java.util.Optional;
import lombok.NonNull;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.ResourceIndexEventType;

public interface KafkaSearchMessageMapper {

  Optional<BibframeIndex> toIndex(Resource work, ResourceIndexEventType eventType);

  Optional<Long> toDeleteIndexId(@NonNull Resource work);

}
