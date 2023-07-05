package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.RELATED_TO_PRED;
import static org.folio.linked.data.util.BibframeConstants.RELATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.REL_WORK_LOOKUP;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Relationship;
import org.folio.linked.data.domain.dto.RelationshipField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = REL_WORK_LOOKUP)
public class RelationshipMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var relationship = coreMapper.readResourceDoc(source, Relationship.class);
    coreMapper.addMappedProperties(source, RELATED_TO_PRED, relationship::addRelatedToItem);
    coreMapper.addMappedProperties(source, RELATION_PRED, relationship::addRelationItem);
    destination.addRelationshipItem(new RelationshipField().relationship(relationship));
    return destination;
  }
}
