package org.folio.linked.data.mapper.resource.monograph.common;

import static org.folio.ld.dictionary.PredicateDictionary.IS_DEFINED_BY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY_SET;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR_PREDICATE;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.CategorySet;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.SingleResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = CATEGORY_SET, predicate = IS_DEFINED_BY, dtoClass = CategorySet.class)
public class CategorySetMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(Category.class);
  private final CoreMapper coreMapper;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof Category category) {
      var categorySet = coreMapper.toDtoWithEdges(source, CategorySet.class, false);
      categorySet.setId(String.valueOf(source.getResourceHash()));
      category.addIsDefinedByItem(categorySet);
    } else {
      throw new NotSupportedException(RESOURCE_TYPE + parentDto.getClass().getSimpleName()
        + IS_NOT_SUPPORTED_FOR_PREDICATE + IS_DEFINED_BY.getUri() + RIGHT_SQUARE_BRACKET);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    throw new UnsupportedOperationException("This operation is not supported.");
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }
}
