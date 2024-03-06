package org.folio.linked.data.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Log4j2
@Mapper(componentModel = SPRING, imports = {Objects.class, Collectors.class})
public abstract class ResourceModelMapper {

  @Mapping(target = "outgoingEdges", expression = "java(modelResource.getOutgoingEdges().stream()"
    + ".map(modelEdge -> toOutgoingEdgeEntity(modelEdge, resource)).collect(Collectors.toCollection(LinkedHashSet::new)))")
  @Mapping(target = "incomingEdges", ignore = true)
  public abstract Resource toEntity(org.folio.ld.dictionary.model.Resource modelResource);

  @Mapping(target = "source", source = "source")
  public abstract ResourceEdge toOutgoingEdgeEntity(org.folio.ld.dictionary.model.ResourceEdge modelEdge,
                                                    Resource source);

  @Mapping(target = "outgoingEdges", expression = "java(entity.getOutgoingEdges().stream()"
    + ".map(entityEdge -> toOutgoingEdgeModel(entityEdge, resource)).collect(Collectors.toCollection(LinkedHashSet::new)))")
  @Mapping(target = "incomingEdges", ignore = true)
  public abstract org.folio.ld.dictionary.model.Resource toModel(Resource entity);

  public ResourceTypeDictionary map(ResourceTypeEntity typeEntity) {
    return ResourceTypeDictionary.fromUri(typeEntity.getUri()).orElse(null);
  }

  public PredicateDictionary map(PredicateEntity predicateEntity) {
    return PredicateDictionary.fromUri(predicateEntity.getUri()).orElse(null);
  }

  @Mapping(target = "source", source = "source")
  public abstract org.folio.ld.dictionary.model.ResourceEdge toOutgoingEdgeModel(
    ResourceEdge entityEdge, org.folio.ld.dictionary.model.Resource source);

}
