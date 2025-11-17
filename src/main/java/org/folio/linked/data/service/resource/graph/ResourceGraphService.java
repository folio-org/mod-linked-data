package org.folio.linked.data.service.resource.graph;

import org.folio.linked.data.model.entity.Resource;

public interface ResourceGraphService {

  org.folio.ld.dictionary.model.Resource getResourceGraph(Long id);

  SaveGraphResult saveMergingGraphInNewTransaction(Resource resource);

  SaveGraphResult saveMergingGraph(Resource resource);

  void breakEdgesAndDelete(Resource resource);

}
