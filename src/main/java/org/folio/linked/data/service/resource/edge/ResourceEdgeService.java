package org.folio.linked.data.service.resource.edge;

import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.pk.ResourceEdgePk;

public interface ResourceEdgeService {

  void copyOutgoingEdges(Resource from, Resource to);

  long deleteEdgesHavingPredicate(Long resourceId, PredicateDictionary predicatedToDelete);

  ResourceEdgePk saveNewResourceEdge(Long sourceId,
                                     PredicateDictionary predicate,
                                     org.folio.ld.dictionary.model.Resource targetModel);

}
