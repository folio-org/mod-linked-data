package org.folio.linked.data.mapper.resource.monograph.work.sub;

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
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.common.CategoryMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = CONTENT, dtoClass = Category.class)
public class ContentMapperUnit extends CategoryMapperUnit {

  private final CoreMapper coreMapper;

  public ContentMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, (category, destination) -> {
      if (destination instanceof Work work) {
        work.addContentItem(category);
      }
      if (destination instanceof WorkReference work) {
        work.addContentItem(category);
      }
    }, CATEGORY);
    this.coreMapper = coreMapper;
  }

  @Override
  protected Optional<Resource> getCategorySet() {
    var map = new HashMap<String, List<String>>();
    putProperty(map, LINK, List.of("http://id.loc.gov/vocabulary/genreFormSchemes/rdacontent"));
    putProperty(map, LABEL, List.of("rdacontent"));
    var categorySet = new Resource()
      .addType(CATEGORY_SET)
      .setDoc(coreMapper.toJson(map));
    categorySet.setResourceHash(coreMapper.hash(categorySet));
    return Optional.of(categorySet);
  }
}
