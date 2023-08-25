package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.CHILDRENS_COMPONENTS;
import static org.folio.linked.data.util.BibframeConstants.COMPONENTS;
import static org.folio.linked.data.util.BibframeConstants.COMPONENT_LIST_PRED;
import static org.folio.linked.data.util.BibframeConstants.PLACE_COMPONENTS;
import static org.folio.linked.data.util.BibframeConstants.SOURCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.SUBJECT_PRED;
import static org.folio.linked.data.util.BibframeConstants.SUBJECT_WORK;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.PlaceField2;
import org.folio.linked.data.domain.dto.Subject2;
import org.folio.linked.data.domain.dto.TopicField2;
import org.folio.linked.data.domain.dto.Work2;
import org.folio.linked.data.domain.dto.Work2SubjectInner;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(predicate = SUBJECT_PRED)
public class SubjectMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work2 toDto(Resource source, Work2 destination) {
    var subject = coreMapper.readResourceDoc(source, Subject2.class);
    coreMapper.addMappedProperties(source, SOURCE_PRED, subject::addSourceItem);
    coreMapper.addMappedProperties(source, COMPONENT_LIST_PRED, subject::addComponentListItem);
    destination.addSubjectItem(toWorkSubjectInner(source, subject));
    return destination;
  }

  private Work2SubjectInner toWorkSubjectInner(Resource source, Subject2 subject) {
    if (Set.of(COMPONENTS, CHILDRENS_COMPONENTS, SUBJECT_WORK).contains(source.getType().getSimpleLabel())) {
      return new TopicField2().topic(subject);
    } else if (PLACE_COMPONENTS.equals(source.getType().getSimpleLabel())) {
      return new PlaceField2().place(subject);
    } else {
      throw new NotSupportedException(RESOURCE_TYPE + source.getType().getSimpleLabel() + IS_NOT_SUPPORTED);
    }
  }
}
