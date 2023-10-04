package org.folio.linked.data.mapper.resource.monograph.inner.work;

import static org.folio.linked.data.util.BibframeConstants.CLASSIFICATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANTIATES_PRED;
import static org.folio.linked.data.util.BibframeConstants.WORK;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.DeweyDecimalClassificationMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = WORK, predicate = INSTANTIATES_PRED, dtoClass = Work.class)
public class WorkMapperUnit implements InstanceSubResourceMapperUnit {
  private final CoreMapper coreMapper;
  private final DeweyDecimalClassificationMapperUnit deweyMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var work = coreMapper.readResourceDoc(source, Work.class);
    work.setId(String.valueOf(source.getResourceHash()));
    coreMapper.addMappedResources(deweyMapper, source, CLASSIFICATION_PRED, work);
    return destination.addInstantiatesItem(work);
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    // Not implemented yet as we don't support PUT / POST APIs for Work
    throw new NotImplementedException();
  }
}
