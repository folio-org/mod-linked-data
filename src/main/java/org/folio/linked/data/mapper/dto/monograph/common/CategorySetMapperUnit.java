package org.folio.linked.data.mapper.dto.monograph.common;

import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY_SET;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.HashService;

public abstract class CategorySetMapperUnit extends CategoryMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  protected CategorySetMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService, CATEGORY);
    this.coreMapper = coreMapper;
    this.hashService = hashService;
  }

  @Override
  protected Optional<Resource> getCategorySet() {
    var label  = getLabel();
    var link  = getLink();
    var map = new HashMap<String, List<String>>();
    putProperty(map, LINK, List.of(link));
    putProperty(map, LABEL, List.of(label));
    var categorySet = new Resource()
      .addTypes(CATEGORY_SET)
      .setDoc(coreMapper.toJson(map))
      .setLabel(label);
    categorySet.setId(hashService.hash(categorySet));
    return Optional.of(categorySet);
  }

  protected abstract String getLabel();

  protected abstract String getLink();
}
