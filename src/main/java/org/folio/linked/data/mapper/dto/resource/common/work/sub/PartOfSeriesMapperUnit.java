package org.folio.linked.data.mapper.dto.resource.common.work.sub;

import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.IS_PART_OF;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PropertyDictionary.ISSN;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.VOLUME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISSN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LIGHT_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.SERIES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.getPropertyValues;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.PartOfSeries;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
@MapperUnit(type = SERIES, predicate = IS_PART_OF, requestDto = PartOfSeries.class)
public class PartOfSeriesMapperUnit implements WorkSubResourceMapperUnit {
  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof WorkResponse workResponse) {
      var partOfSeries = coreMapper.toDtoWithEdges(resourceToConvert, PartOfSeries.class, false);
      getSeriesNode(resourceToConvert).ifPresent(series -> partOfSeries.setIssn(getPropertyValues(series, ISSN)));
      workResponse.addPartOfSeriesItem(partOfSeries);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var partOfSeries = (PartOfSeries) dto;

    var work = createResource(
      getWorkSeriesDoc(partOfSeries),
      join(" ", getFirstValue(partOfSeries::getName), getFirstValue(partOfSeries::getVolume)),
      WORK, SERIES, LIGHT_RESOURCE
    );
    var instance = createResource(
      getInstanceOrSeriesDoc(partOfSeries),
      getFirstValue(partOfSeries::getName),
      INSTANCE, SERIES, LIGHT_RESOURCE
    );
    var series = createResource(getInstanceOrSeriesDoc(partOfSeries), getFirstValue(partOfSeries::getName), SERIES);
    var issnOpt = ofNullable(partOfSeries.getIssn())
      .filter(issn -> !issn.isEmpty())
      .map(issn -> createResource(getIssnDoc(partOfSeries), getFirstValue(partOfSeries::getIssn), IDENTIFIER, ID_ISSN));

    connectResources(work, series, IS_PART_OF);
    connectResources(instance, series, INSTANTIATES);

    issnOpt.ifPresent(issn -> {
      connectResources(series, issn, MAP);
      connectResources(instance, issn, MAP);
    });

    setResourceIds(issnOpt.orElse(null), series, work, instance);

    return work;
  }

  private Resource createResource(JsonNode doc, String label, ResourceTypeDictionary... types) {
    return new Resource()
      .addTypes(types)
      .setDoc(doc)
      .setLabel(label);
  }

  private void connectResources(Resource source, Resource target, PredicateDictionary connectionType) {
    var edge = new ResourceEdge(source, target, connectionType);
    source.addOutgoingEdge(edge);
    target.addIncomingEdge(edge);
  }

  private void setResourceIds(Resource... resources) {
    stream(resources)
      .filter(Objects::nonNull)
      .forEach(resource -> resource.setIdAndRefreshEdges(hashService.hash(resource)));
  }

  private JsonNode getIssnDoc(PartOfSeries dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getIssn());
    putProperty(map, LABEL, dto.getIssn());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

  private JsonNode getInstanceOrSeriesDoc(PartOfSeries dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getName());
    putProperty(map, ISSN, dto.getIssn());
    putProperty(map, LABEL, dto.getName());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

  private JsonNode getWorkSeriesDoc(PartOfSeries dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getName());
    putProperty(map, VOLUME, dto.getVolume());
    putProperty(map, LABEL, List.of(join(" ", getFirstValue(dto::getName), getFirstValue(dto::getVolume))));
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

  private Optional<Resource> getSeriesNode(Resource source) {
    return source.getOutgoingEdges().stream()
      .filter(e -> e.getPredicate().getUri().equals(IS_PART_OF.getUri()))
      .map(ResourceEdge::getTarget)
      .filter(r -> r.isOfType(SERIES))
      .findFirst();
  }
}
