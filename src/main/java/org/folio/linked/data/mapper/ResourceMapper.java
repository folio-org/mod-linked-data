package org.folio.linked.data.mapper;

import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.ITEM;
import static org.folio.linked.data.util.BibframeConstants.MONOGRAPH;
import static org.folio.linked.data.util.BibframeConstants.WORK;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.folio.linked.data.domain.dto.ResourceInstanceInner;
import org.folio.linked.data.domain.dto.ResourceItemInner;
import org.folio.linked.data.domain.dto.ResourceResponse;
import org.folio.linked.data.domain.dto.ResourceShort;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
import org.folio.linked.data.domain.dto.ResourceWorkInner;
import org.folio.linked.data.exception.JsonException;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.model.ResourceHashAndProfile;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.util.TextUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

@Mapper(componentModel = SPRING, imports = {TextUtil.class})
public abstract class ResourceMapper {

  public static final String RESOURCE_TYPE = "Resource type [";
  public static final String IS_NOT_SUPPORTED = "] is not supported";
  public static final String IS_NOT_SUPPORTED_HERE = IS_NOT_SUPPORTED + " here";
  private static final String ERROR_JSON_PROCESSING = "Error while json processing";
  private static final String IS_NOT_BIBFRAME_ROOT = "] is not a Bibframe root";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private WorkMapper workMapper;

  @Autowired
  private InstanceMapper instanceMapper;

  @Autowired
  private ItemMapper itemMapper;

  @Mapping(target = "id", source = "resourceHash")
  public abstract ResourceShort map(ResourceHashAndProfile resourceHashAndProfile);

  public abstract ResourceShortInfoPage map(Page<ResourceShort> page);

  @Named("jsonToBibframe")
  public ResourceResponse map(JsonNode node) {
    return node != null
        ? objectMapper.convertValue(node, ResourceResponse.class)
        : new ResourceResponse();
  }

  public ResourceResponse map(Resource resource) {
    var response = new ResourceResponse();
    if (MONOGRAPH.equals(resource.getType().getSimpleLabel())) {
      addResources(response, resource.getOutgoingEdges(), workMapper, instanceMapper, itemMapper);
    } else {
      throw new NotSupportedException(RESOURCE_TYPE + resource.getType() + IS_NOT_BIBFRAME_ROOT);
    }
    response.setId(resource.getResourceHash());
    return response;
  }

  @Named("objectToJson")
  public JsonNode toJson(Object configuration) {
    try {
      JsonNode node = null;
      if (configuration instanceof String json) {
        node = objectMapper.readTree(json);
      } else if (configuration instanceof Map json) {
        node = objectMapper.convertValue(json, JsonNode.class);
      } else {
        node = objectMapper.valueToTree(configuration);
      }
      return !(node instanceof NullNode) ? node : objectMapper.createObjectNode();
    } catch (JsonProcessingException e) {
      throw new JsonException(ERROR_JSON_PROCESSING, e);
    }
  }

  private void addResources(ResourceResponse response,
                            Set<ResourceEdge> resourceEdges,
                            WorkMapper workMapper,
                            InstanceMapper instanceMapper,
                            ItemMapper itemMapper) {
    var works = new ArrayList<ResourceWorkInner>();
    var instances = new ArrayList<ResourceInstanceInner>();
    var items = new ArrayList<ResourceItemInner>();

    for (var edge : resourceEdges) {
      var resource = edge.getTarget();
      switch (resource.getType().getSimpleLabel()) {
        case WORK -> works.add(workMapper.toWork(resource));
        case INSTANCE -> instances.add(instanceMapper.toInstance(resource));
        case ITEM -> items.add(itemMapper.toItem(resource));
        default -> throw new NotSupportedException(RESOURCE_TYPE + resource.getType() + IS_NOT_SUPPORTED);
      }
    }

    response.setWork(works);
    response.setInstance(instances);
    response.setItem(items);
  }
}
