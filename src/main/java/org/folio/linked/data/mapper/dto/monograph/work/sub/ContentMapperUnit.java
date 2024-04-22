package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY_SET;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.CategoryMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = CONTENT, dtoClass = Category.class)
public class ContentMapperUnit extends CategoryMapperUnit {

  private static final String CATEGORY_SET_LABEL = "rdacontent";
  private static final String CATEGORY_SET_LINK = "http://id.loc.gov/vocabulary/genreFormSchemes/rdacontent";
  private static final String CONTENT_TYPE_LINK_PREFIX = "http://id.loc.gov/vocabulary/contentTypes/";

  private final CoreMapper coreMapper;
  private final HashService hashService;

  public ContentMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService, CATEGORY);
    this.coreMapper = coreMapper;
    this.hashService = hashService;
  }

  @Override
  protected Optional<Resource> getCategorySet() {
    var map = new HashMap<String, List<String>>();
    putProperty(map, LINK, List.of(CATEGORY_SET_LINK));
    putProperty(map, LABEL, List.of(CATEGORY_SET_LABEL));
    var categorySet = new Resource()
      .addTypes(CATEGORY_SET)
      .setDoc(coreMapper.toJson(map))
      .setLabel(CATEGORY_SET_LABEL);
    categorySet.setId(hashService.hash(categorySet));
    return Optional.of(categorySet);
  }

  @Override
  protected void addToParent(Category category, Object parentDto) {
    if (parentDto instanceof Work work) {
      work.addContentItem(category);
    }
    if (parentDto instanceof WorkReference workReference) {
      workReference.addContentItem(category);
    }
  }

  @Override
  protected String getLinkPrefix() {
    return CONTENT_TYPE_LINK_PREFIX;
  }
}
