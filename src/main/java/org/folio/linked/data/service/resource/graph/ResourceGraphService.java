package org.folio.linked.data.service.resource.graph;

import org.folio.linked.data.domain.dto.ResourceGraphDto;
import org.folio.linked.data.model.entity.Resource;

public interface ResourceGraphService {

  ResourceGraphDto getResourceGraph(Long id);

  SaveGraphResult saveMergingGraph(Resource resource);

  void breakEdgesAndDelete(Resource resource);

}
