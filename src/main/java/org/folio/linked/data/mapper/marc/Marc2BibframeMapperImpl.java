package org.folio.linked.data.mapper.marc;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.folio.linked.data.util.BibframeConstants.*;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.configuration.property.Marc2BibframeRules;
import org.folio.linked.data.mapper.ResourceMapper;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.InstanceMapperUnit;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Marc2BibframeMapperImpl implements Marc2BibframeMapper {

  private final Marc2BibframeRules rules;
  private final DictionaryService<ResourceType> resourceTypeService;
  private final DictionaryService<Predicate> predicateService;
  private final ObjectMapper objectMapper;
  private final CoreMapper coreMapper;

  @Override
  public Resource map(String marc) {
    var reader = new MarcXmlReader(new ByteArrayInputStream(marc.getBytes(StandardCharsets.UTF_8)));
    var instance = new Resource().addType(resourceTypeService.get(INSTANCE));
    while (reader.hasNext()) {
      var marcRecord = reader.next();
      for (var dataField : marcRecord.getDataFields()) {
        if (isNotEmpty(dataField.getTag())) {
          var fieldRules = rules.getFieldRules().get(dataField.getTag());
          if (nonNull(fieldRules)) {
            fieldRules.forEach(fieldRule -> addFieldResource(instance, dataField, fieldRule));
          }
        }
      }
    }
    instance.setLabel(selectLabel(instance));
    instance.setResourceHash(coreMapper.hash(instance));
    ResourceMapper.setEdgesId(instance);
    return instance;
  }

  private String selectLabel(Resource instance) {
    return getFirstValue(() -> instance.getOutgoingEdges().stream()
      .filter(e -> e.getPredicate().getLabel().equals(INSTANCE_TITLE_PRED))
      .map(re -> re.getTarget().getLabel()).toList());
  }

  private void addFieldResource(Resource instance, DataField dataField, Marc2BibframeRules.FieldRule fieldRule) {
    if (checkConditions(fieldRule, dataField)) {
      var resource = new Resource();
      fieldRule.getTypes().stream().map(resourceTypeService::get).forEach(resource::addType);
      var properties = new HashMap<String, List<String>>();
      fieldRule.getSubfields().forEach((key, value) -> {
        if (nonNull(dataField.getSubfield(key))) {
          properties.computeIfAbsent(value, k -> new ArrayList<>()).add(dataField.getSubfield(key).getData());
        }
      });
      if (nonNull(fieldRule.getInd1())) {
        properties.computeIfAbsent(fieldRule.getInd1(), k -> new ArrayList<>()).add(
          String.valueOf(dataField.getIndicator1()));
      }
      if (nonNull(fieldRule.getInd2())) {
        properties.computeIfAbsent(fieldRule.getInd2(), k -> new ArrayList<>()).add(
          String.valueOf(dataField.getIndicator2()));
      }
      resource.setDoc(getJsonNode(properties));
      resource.setResourceHash(coreMapper.hash(resource));
      resource.setLabel(dataField.getSubfield(fieldRule.getLabelField()).getData());
      instance.getOutgoingEdges().add(new ResourceEdge(instance, resource,
        predicateService.get(fieldRule.getPredicate())));
    }
  }

  private JsonNode getJsonNode(Map<String, ?> map) {
    return objectMapper.convertValue(map, JsonNode.class);
  }

  private boolean checkConditions(Marc2BibframeRules.FieldRule fieldRule, DataField dataField) {
    var condition = fieldRule.getCondition();
    if (isNull(condition)) {
      return true;
    }
    boolean ind1Condition = checkCondition(String.valueOf(dataField.getIndicator1()), condition.getInd1());
    boolean ind2Condition = checkCondition(String.valueOf(dataField.getIndicator2()), condition.getInd2());
    boolean fieldConditions = condition.getFields().entrySet().stream()
      .allMatch(fieldCondition -> ofNullable(dataField.getSubfield(fieldCondition.getKey()))
        .map(sf -> checkCondition(sf.getData(), fieldCondition.getValue()))
        .orElse(false));
    return ind1Condition && ind2Condition && fieldConditions;
  }

  private boolean checkCondition(String value, String condition) {
    if (isEmpty(condition)) {
      return true;
    }
    if (condition.contains("!")) {
      condition = condition.replace("!", "");
      return !Objects.equals(value, condition);
    }
    return Objects.equals(value, condition);
  }
}
