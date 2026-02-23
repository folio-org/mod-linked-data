package org.folio.linked.data.mapper.dto.resource.common.title;

import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.BaseTitle;
import org.folio.linked.data.domain.dto.HubRequest;
import org.folio.linked.data.domain.dto.HubResponse;
import org.folio.linked.data.domain.dto.InstanceRequest;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.SingleResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.label.ResourceEntityLabelService;
import org.folio.linked.data.service.resource.hash.HashService;
import tools.jackson.databind.JsonNode;

@RequiredArgsConstructor
public abstract class TitleMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    InstanceRequest.class,
    InstanceResponse.class,
    WorkRequest.class,
    WorkResponse.class,
    HubRequest.class,
    HubResponse.class
  );
  private final HashService hashService;
  private final ResourceEntityLabelService labelService;

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }

  @Override
  public final Resource toEntity(Object dto, Resource parentEntity) {
    var resource = new Resource();
    resource.addTypes(resourceType());
    resource.setDoc(getDoc(dto));
    labelService.assignLabelToResource(resource);
    resource.setIdAndRefreshEdges(hashService.hash(resource));
    return resource;
  }

  protected abstract ResourceTypeDictionary resourceType();

  protected abstract JsonNode getDoc(Object dto);

  protected HashMap<String, List<String>> getBaseTitleProperties(BaseTitle title) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, PART_NAME, title.getPartName());
    putProperty(map, PART_NUMBER, title.getPartNumber());
    putProperty(map, MAIN_TITLE, title.getMainTitle());
    putProperty(map, SUBTITLE, title.getSubTitle());
    return map;
  }
}
