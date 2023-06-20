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
import org.folio.linked.data.domain.dto.BibframeInstanceInner;
import org.folio.linked.data.domain.dto.BibframeItemInner;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShort;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.domain.dto.BibframeWorkInner;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.Work;
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
public abstract class BibframeMapper {

  public static final String RESOURCE_TYPE = "Resource type [";
  public static final String IS_NOT_SUPPORTED = "] is not supported";
  public static final String IS_NOT_SUPPORTED_HERE = IS_NOT_SUPPORTED + " here";
  private static final String ERROR_JSON_PROCESSING = "Error while json processing";
  private static final String IS_NOT_BIBFRAME_ROOT = "] is not a Bibframe root";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private BaseResourceMapper<Work> workMapper;

  @Autowired
  private BaseResourceMapper<Instance> instanceMapper;

  @Autowired
  private BaseResourceMapper<Item> itemMapper;

  @Mapping(target = "id", source = "resourceHash")
  public abstract BibframeShort map(ResourceHashAndProfile resourceHashAndProfile);

  public abstract BibframeShortInfoPage map(Page<BibframeShort> page);

  @Named("jsonToBibframe")
  public BibframeResponse map(JsonNode node) {
    return node != null
        ? objectMapper.convertValue(node, BibframeResponse.class)
        : new BibframeResponse();
  }

  public BibframeResponse map(Resource resource) {
    var response = new BibframeResponse();
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

  private void addResources(BibframeResponse response,
                            Set<ResourceEdge> resourceEdges,
                            BaseResourceMapper<Work> workMapper,
                            BaseResourceMapper<Instance> instanceMapper,
                            BaseResourceMapper<Item> itemMapper) {
    var works = new ArrayList<BibframeWorkInner>();
    var instances = new ArrayList<BibframeInstanceInner>();
    var items = new ArrayList<BibframeItemInner>();

    for (var edge : resourceEdges) {
      var resource = edge.getTarget();
      switch (resource.getType().getSimpleLabel()) {
        case WORK -> works.add(workMapper.map(resource));
        case INSTANCE -> instances.add(instanceMapper.map(resource));
        case ITEM -> items.add(itemMapper.map(resource));
        default -> throw new NotSupportedException(RESOURCE_TYPE + resource.getType() + IS_NOT_SUPPORTED);
      }
    }

    response.setWork(works);
    response.setInstance(instances);
    response.setItem(items);
  }
}
