package org.folio.linked.data.mapper.dto.monograph.common;

import static org.folio.ld.dictionary.PredicateDictionary.IS_DEFINED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceReference;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.HashService;

@RequiredArgsConstructor
public abstract class CategoryMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(Instance.class, InstanceReference.class, Work.class,
    WorkReference.class);

  private final CoreMapper coreMapper;
  private final HashService hashService;
  private final BiConsumer<Category, Object> categoryConsumer;
  private final ResourceTypeDictionary type;

  protected Optional<Resource> getCategorySet() {
    return Optional.empty();
  }

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    var category = coreMapper.toDtoWithEdges(source, Category.class, false);
    category.setId(String.valueOf(source.getId()));
    categoryConsumer.accept(category, parentDto);
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var category = (Category) dto;
    var resource = new Resource();
    resource.setLabel(getFirstValue(category::getTerm));
    resource.addType(type);
    resource.setDoc(getDoc(category));
    getCategorySet().ifPresent(cs -> resource.addOutgoingEdge(new ResourceEdge(resource, cs, IS_DEFINED_BY)));
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Category dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, CODE, dto.getCode());
    putProperty(map, TERM, dto.getTerm());
    putProperty(map, LINK, dto.getLink());
    putProperty(map, SOURCE, dto.getSource());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }

}
