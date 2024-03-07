package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.GOVERNMENT_PUBLICATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.CategoryMapperUnit;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = GOVERNMENT_PUBLICATION, dtoClass = Category.class)
public class GovernmentPublicationMapperUnit extends CategoryMapperUnit {

  public GovernmentPublicationMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService, (category, destination) -> {
      if (destination instanceof Work work) {
        work.addGovernmentPublicationItem(category);
      }
    }, CATEGORY);
  }
}
