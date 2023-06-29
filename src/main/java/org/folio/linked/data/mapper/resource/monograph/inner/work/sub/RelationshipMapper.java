package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.RELATED_TO_PRED;
import static org.folio.linked.data.util.BibframeConstants.RELATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.REL_WORK_LOOKUP;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Relationship;
import org.folio.linked.data.domain.dto.RelationshipField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = REL_WORK_LOOKUP)
public class RelationshipMapper implements WorkSubResourceMapper {

  private final ObjectMapper mapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var relationship = readResourceDoc(mapper, source, Relationship.class);
    addMappedProperties(mapper, source, RELATED_TO_PRED, relationship::addRelatedToItem);
    addMappedProperties(mapper, source, RELATION_PRED, relationship::addRelationItem);
    destination.addRelationshipItem(new RelationshipField().relationship(relationship));
    return destination;
  }
}
