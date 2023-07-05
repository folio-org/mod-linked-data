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
import org.folio.linked.data.domain.dto.PlaceField;
import org.folio.linked.data.domain.dto.Subject;
import org.folio.linked.data.domain.dto.TopicField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkSubjectInner;
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
  public Work toDto(Resource source, Work destination) {
    var subject = coreMapper.readResourceDoc(source, Subject.class);
    coreMapper.addMappedProperties(source, SOURCE_PRED, subject::addSourceItem);
    coreMapper.addMappedProperties(source, COMPONENT_LIST_PRED, subject::addComponentListItem);
    destination.addSubjectItem(toWorkSubjectInner(source, subject));
    return destination;
  }

  private WorkSubjectInner toWorkSubjectInner(Resource source, Subject subject) {
    if (Set.of(COMPONENTS, CHILDRENS_COMPONENTS, SUBJECT_WORK).contains(source.getType().getSimpleLabel())) {
      return new TopicField().topic(subject);
    } else if (PLACE_COMPONENTS.equals(source.getType().getSimpleLabel())) {
      return new PlaceField().place(subject);
    } else {
      throw new NotSupportedException(RESOURCE_TYPE + source.getType().getSimpleLabel() + IS_NOT_SUPPORTED);
    }
  }
}
