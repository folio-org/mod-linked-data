package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.folio.linked.data.domain.dto.DewvyDecimalClassification;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = CATEGORY, predicate = CLASSIFICATION, dtoClass = DewvyDecimalClassification.class)
public class DeweyDecimalClassificationMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var deweyDecimalClassification = coreMapper.readResourceDoc(source, DewvyDecimalClassification.class);
    deweyDecimalClassification.setId(String.valueOf(source.getResourceHash()));
    destination.addClassificationItem(deweyDecimalClassification);
    return destination;
  }

  @Override
  public Resource toEntity(Object dto) {
    // Not implemented yet as we don't support PUT / POST APIs for Work
    throw new NotImplementedException();
  }
}
