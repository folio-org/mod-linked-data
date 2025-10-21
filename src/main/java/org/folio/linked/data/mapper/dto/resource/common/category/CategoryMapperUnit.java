package org.folio.linked.data.mapper.dto.resource.common.category;

import static org.folio.ld.dictionary.PredicateDictionary.IS_DEFINED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY_SET;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.CategoryResponse;
import org.folio.linked.data.domain.dto.InstanceRequest;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.SingleResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.resource.hash.HashService;

@RequiredArgsConstructor
public abstract class CategoryMapperUnit implements SingleResourceMapperUnit, MarcCodeProvider {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    InstanceRequest.class,
    InstanceResponse.class,
    WorkRequest.class,
    WorkResponse.class
  );

  protected final HashService hashService;
  private final CoreMapper coreMapper;

  protected abstract void addToParent(CategoryResponse category, Object parentDto);

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    var category = coreMapper.toDtoWithEdges(resourceToConvert, CategoryResponse.class, false);
    category.setId(String.valueOf(resourceToConvert.getId()));
    addToParent(category, parentDto);
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var category = (Category) dto;
    var resource = new Resource();
    var categorySet = new ResourceEdge(resource, getCategorySet(), IS_DEFINED_BY);
    resource.setLabel(getFirstValue(category::getTerm));
    resource.addTypes(CATEGORY);
    resource.setDoc(getDoc(category));
    resource.addOutgoingEdge(categorySet);
    resource.setIdAndRefreshEdges(hashService.hash(resource));
    return resource;
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }

  protected abstract String getCategorySetLabel();

  protected abstract String getCategorySetLink();

  protected JsonNode getDoc(Category dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, CODE, getMarcCodes(dto.getLink()));
    putProperty(map, TERM, dto.getTerm());
    putProperty(map, LINK, dto.getLink());
    putProperty(map, SOURCE, dto.getSource());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

  private Resource getCategorySet() {
    var label  = getCategorySetLabel();
    var link  = getCategorySetLink();
    var map = new HashMap<String, List<String>>();
    putProperty(map, LINK, List.of(link));
    putProperty(map, LABEL, List.of(label));
    var categorySet = new Resource()
      .addTypes(CATEGORY_SET)
      .setDoc(coreMapper.toJson(map))
      .setLabel(label);
    categorySet.setIdAndRefreshEdges(hashService.hash(categorySet));
    return categorySet;
  }
}
