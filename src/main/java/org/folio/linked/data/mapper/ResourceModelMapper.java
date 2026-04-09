package org.folio.linked.data.mapper;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toCollection;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Qualifier;
import org.mapstruct.TargetType;
import tools.jackson.databind.JsonNode;

@Mapper(componentModel = SPRING)
public abstract class ResourceModelMapper {

  private static final int MAX_ENTITY_TO_MODEL_EDGE_DEPTH = 7;

  @NotForGeneration
  public Resource toEntity(org.folio.ld.dictionary.model.Resource model) {
    return toEntity(model, new CyclicGraphContext());
  }

  @Mapping(target = "folioMetadata",
    expression = "java(model.getFolioMetadata() != null ? mapFolioMetadata(model.getFolioMetadata(), resource) : null)")
  @Mapping(target = "id", ignore = true)
  protected abstract Resource toEntity(org.folio.ld.dictionary.model.Resource model,
                                       @Context CyclicGraphContext cycleContext);

  @AfterMapping
  protected void assignId(@MappingTarget Resource target, org.folio.ld.dictionary.model.Resource source) {
    if (nonNull(source.getId())) {
      target.setIdAndRefreshEdges(source.getId());
    }
  }

  @NotForGeneration
  public org.folio.ld.dictionary.model.Resource toModel(Resource entity) {
    return toModel(entity, MAX_ENTITY_TO_MODEL_EDGE_DEPTH);
  }

  @NotForGeneration
  public org.folio.ld.dictionary.model.Resource toModel(Resource entity, int maxEdgesDepth) {
    if (maxEdgesDepth > MAX_ENTITY_TO_MODEL_EDGE_DEPTH) {
      throw new IllegalArgumentException("Requested edges depth is too high: " + maxEdgesDepth
        + ". Maximum allowed is " + MAX_ENTITY_TO_MODEL_EDGE_DEPTH);
    }
    return toModel(entity, new CyclicGraphContext(), new DepthContext(maxEdgesDepth, maxEdgesDepth));
  }

  @Mapping(ignore = true, target = "outgoingEdges")
  @Mapping(ignore = true, target = "incomingEdges")
  protected abstract org.folio.ld.dictionary.model.Resource toModel(Resource entity,
                                                                    @Context CyclicGraphContext cycleContext,
                                                                    @Context DepthContext depthContext);

  @AfterMapping
  protected void mapOutgoingEdges(@MappingTarget org.folio.ld.dictionary.model.Resource target,
                                  Resource source,
                                  @Context CyclicGraphContext cycleContext,
                                  @Context DepthContext depthContext) {
    target.setOutgoingEdges(mapEdges(source.getOutgoingEdges(), depthContext.outgoingAllowsEdges(),
      depthContext::nextOutgoing, (edge, next) ->
        mapEdge(target, toModel(edge.getTarget(), cycleContext, next), edge.getPredicate())));
  }

  @AfterMapping
  protected void mapIncomingEdges(@MappingTarget org.folio.ld.dictionary.model.Resource target,
                                  Resource source,
                                  @Context CyclicGraphContext cycleContext,
                                  @Context DepthContext depthContext) {
    target.setIncomingEdges(mapEdges(source.getIncomingEdges(), depthContext.incomingAllowsEdges(),
      depthContext::nextIncoming, (edge, next) ->
        mapEdge(toModel(edge.getSource(), cycleContext, next), target, edge.getPredicate())));
  }

  private LinkedHashSet<org.folio.ld.dictionary.model.ResourceEdge> mapEdges(
    Set<ResourceEdge> edges,
    boolean allowsEdges,
    Supplier<DepthContext> nextDepth,
    BiFunction<ResourceEdge, DepthContext, org.folio.ld.dictionary.model.ResourceEdge> edgeMapper) {
    if (!allowsEdges) {
      return new LinkedHashSet<>();
    }
    var next = nextDepth.get();
    return edges.stream().map(edge -> edgeMapper.apply(edge, next)).collect(toCollection(LinkedHashSet::new));
  }

  private org.folio.ld.dictionary.model.ResourceEdge mapEdge(org.folio.ld.dictionary.model.Resource edgeSource,
                                                              org.folio.ld.dictionary.model.Resource edgeTarget,
                                                              PredicateEntity predicate) {
    return new org.folio.ld.dictionary.model.ResourceEdge(edgeSource, edgeTarget, map(predicate));
  }

  protected Set<ResourceTypeDictionary> map(Set<ResourceTypeEntity> typeEntities) {
    return typeEntities.stream()
      .map(typeEntity -> ResourceTypeDictionary.fromUri(typeEntity.getUri()).orElse(null))
      .filter(Objects::nonNull)
      .collect(toCollection(LinkedHashSet::new));
  }

  protected PredicateDictionary map(PredicateEntity predicateEntity) {
    return PredicateDictionary.fromUri(predicateEntity.getUri()).orElse(null);
  }

  protected JsonNode map(JsonNode jsonNode) {
    // JsonNode is immutable, so we can return the same instance
    return jsonNode;
  }

  @Mapping(source = "resource", target = "resource")
  protected abstract FolioMetadata mapFolioMetadata(org.folio.ld.dictionary.model.FolioMetadata folioMetadata,
                                                    Resource resource);

  @Qualifier
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.CLASS)
  protected @interface NotForGeneration {
  }

  protected static class CyclicGraphContext {
    private final Map<Resource, org.folio.ld.dictionary.model.Resource> convertedModels = new HashMap<>();
    private final Map<org.folio.ld.dictionary.model.Resource, Resource> convertedEntities = new HashMap<>();

    @BeforeMapping
    protected org.folio.ld.dictionary.model.Resource getMappedModel(
      Resource source, @TargetType Class<org.folio.ld.dictionary.model.Resource> target) {
      return convertedModels.get(source);
    }

    @BeforeMapping
    protected void storeMappedModel(Resource source, @MappingTarget org.folio.ld.dictionary.model.Resource target) {
      convertedModels.put(source, target);
    }

    @BeforeMapping
    protected Resource getMappedEntity(org.folio.ld.dictionary.model.Resource source,
                                       @TargetType Class<Resource> target) {
      return convertedEntities.get(source);
    }

    @BeforeMapping
    protected void storeMappedEntity(org.folio.ld.dictionary.model.Resource source, @MappingTarget Resource target) {
      convertedEntities.put(source, target);
    }
  }

  protected static final class DepthContext {
    private final int outgoingDepth;
    private final int incomingDepth;

    private DepthContext(int outgoingDepth, int incomingDepth) {
      this.outgoingDepth = Math.max(outgoingDepth, 0);
      this.incomingDepth = Math.max(incomingDepth, 0);
    }

    private boolean outgoingAllowsEdges() {
      return outgoingDepth > 0;
    }

    private boolean incomingAllowsEdges() {
      return incomingDepth > 0;
    }

    private DepthContext nextOutgoing() {
      return new DepthContext(outgoingDepth - 1, incomingDepth);
    }

    private DepthContext nextIncoming() {
      return new DepthContext(outgoingDepth, incomingDepth - 1);
    }
  }

}
