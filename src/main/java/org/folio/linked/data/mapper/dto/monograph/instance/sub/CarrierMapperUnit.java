package org.folio.linked.data.mapper.dto.monograph.instance.sub;

import static org.folio.ld.dictionary.PredicateDictionary.CARRIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceReference;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.CategoryMapperUnit;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = CARRIER, dtoClass = Category.class)
public class CarrierMapperUnit extends CategoryMapperUnit {

  private static final String CATEGORY_SET_LABEL = "rdacarrier";
  private static final String CATEGORY_SET_LINK = "http://id.loc.gov/vocabulary/genreFormSchemes/rdacarrier";
  private static final String CARRIER_TYPE_LINK_PREFIX = "http://id.loc.gov/vocabulary/carriers/";

  public CarrierMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService);
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
  protected void addToParent(Category category, Object parentDto) {
    if (parentDto instanceof Instance instance) {
      instance.addCarrierItem(category);
    }
    if (parentDto instanceof InstanceReference instanceReference) {
      instanceReference.addCarrierItem(category);
    }
  }

  @Override
  public String getLinkPrefix() {
    return CARRIER_TYPE_LINK_PREFIX;
  }
}
