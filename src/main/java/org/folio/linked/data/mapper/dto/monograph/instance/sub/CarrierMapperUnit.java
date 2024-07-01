package org.folio.linked.data.mapper.dto.monograph.instance.sub;

import static org.folio.ld.dictionary.PredicateDictionary.CARRIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.CategoryResponse;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.CategoryMapperUnit;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = CARRIER, requestDto = Category.class)
public class CarrierMapperUnit extends CategoryMapperUnit {

  private static final String CATEGORY_SET_LABEL = "rdacarrier";
  private static final String CATEGORY_SET_LINK = "http://id.loc.gov/vocabulary/genreFormSchemes/rdacarrier";
  private static final String CARRIER_TYPE_LINK_PREFIX = "http://id.loc.gov/vocabulary/carriers/";

  public CarrierMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(hashService, coreMapper);
  }

  @Override
  protected String getCategorySetLabel() {
    return CATEGORY_SET_LABEL;
  }

  @Override
  protected String getCategorySetLink() {
    return CATEGORY_SET_LINK;
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
