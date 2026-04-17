package org.folio.linked.data.mapper.dto.resource.common.category;

import static org.folio.ld.dictionary.PredicateDictionary.CARRIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.linked.data.util.ResourceUtils.getSourceOrFallback;

import java.util.List;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.CategoryResponse;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = CARRIER, requestDto = Category.class)
public class CarrierMapperUnit extends CategoryMapperUnit {

  private static final String CARRIER_CATEGORY_LABEL = "rdacarrier";
  private static final String CARRIER_CATEGORY_LINK = "http://id.loc.gov/vocabulary/genreFormSchemes/rdacarrier";
  private static final String CARRIER_TYPE_LINK_PREFIX = "http://id.loc.gov/vocabulary/carriers/";

  public CarrierMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(hashService, coreMapper);
  }

  @Override
  protected String getCategorySetLabel() {
    return CARRIER_CATEGORY_LABEL;
  }

  @Override
  protected String getCategorySetLink() {
    return CARRIER_CATEGORY_LINK;
  }

  @Override
  protected List<String> getSource(Category dto) {
    return getSourceOrFallback(dto.getSource(), CARRIER_CATEGORY_LABEL);
  }

  @Override
  protected void addToParent(CategoryResponse category, Object parentDto) {
    if (parentDto instanceof InstanceResponse instance) {
      instance.addCarrierItem(category);
    }
  }

  @Override
  public String getLinkPrefix() {
    return CARRIER_TYPE_LINK_PREFIX;
  }
}
