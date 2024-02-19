package org.folio.linked.data.mapper.resource.kafka;

import java.util.Optional;
import lombok.NonNull;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.BibframeIndex;

public interface KafkaMessageMapper {

  Optional<BibframeIndex> toCreateIndex(@NonNull Resource resource);

  Optional<Long> toDeleteIndex(@NonNull Resource resource);

}
