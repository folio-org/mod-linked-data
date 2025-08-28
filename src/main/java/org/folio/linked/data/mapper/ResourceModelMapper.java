package org.folio.linked.data.mapper;

import static java.util.Objects.isNull;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Qualifier;
import org.mapstruct.TargetType;

@Mapper(componentModel = SPRING)
public abstract class ResourceModelMapper {

  @NotForGeneration
  public Resource toEntity(org.folio.ld.dictionary.model.Resource model) {
    return toEntity(model, new ModelToEntityCyclicGraphContext());
  }

  @Mapping(target = "folioMetadata",
    expression = "java(model.getFolioMetadata() != null ? mapFolioMetadata(model.getFolioMetadata(), resource) : null)")
  protected abstract Resource toEntity(org.folio.ld.dictionary.model.Resource model,
                                       @Context ModelToEntityCyclicGraphContext cycleContext);

  @NotForGeneration
  public org.folio.ld.dictionary.model.Resource toModel(Resource entity) {
    return toModel(entity, new EntityToModelCyclicGraphContext());
  }

  @Mapping(ignore = true, target = "incomingEdges")
  protected abstract org.folio.ld.dictionary.model.Resource toModel(Resource entity,
                                                                    @Context EntityToModelCyclicGraphContext context);

  protected Set<ResourceTypeDictionary> map(Set<ResourceTypeEntity> typeEntities) {
    return typeEntities.stream()
      .map(typeEntity -> ResourceTypeDictionary.fromUri(typeEntity.getUri()).orElse(null))
      .filter(Objects::nonNull)
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  protected PredicateDictionary map(PredicateEntity predicateEntity) {
    return PredicateDictionary.fromUri(predicateEntity.getUri()).orElse(null);
  }

  @Mapping(source = "resource", target = "resource")
  protected abstract FolioMetadata mapFolioMetadata(org.folio.ld.dictionary.model.FolioMetadata folioMetadata,
                                                    Resource resource);

  @Qualifier
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.CLASS)
  protected @interface NotForGeneration {
  }

  protected static class EntityToModelCyclicGraphContext {
    private final Map<Resource, org.folio.ld.dictionary.model.Resource> modelsNoId = new IdentityHashMap<>();
    private final Map<Resource, org.folio.ld.dictionary.model.Resource> modelsWithId = new HashMap<>();

    @BeforeMapping
    @SuppressWarnings("java:S2583")
    protected org.folio.ld.dictionary.model.Resource getMappedModel(
      Resource source, @TargetType Class<org.folio.ld.dictionary.model.Resource> target) {
      return isNull(source.getId()) ? modelsNoId.get(source) : modelsWithId.get(source);
    }

    @BeforeMapping
    @SuppressWarnings("java:S2583")
    protected void storeMappedModel(Resource source, @MappingTarget org.folio.ld.dictionary.model.Resource target) {
      (isNull(source.getId()) ? modelsNoId : modelsWithId).put(source, target);
    }
  }

  protected static class ModelToEntityCyclicGraphContext {
    private final Map<org.folio.ld.dictionary.model.Resource, Resource> entitiesNoId = new IdentityHashMap<>();
    private final Map<org.folio.ld.dictionary.model.Resource, Resource> entitiesWithId = new HashMap<>();

    @BeforeMapping
    protected Resource getMappedEntity(org.folio.ld.dictionary.model.Resource source,
                                       @TargetType Class<Resource> target) {
      return isNull(source.getId()) ? entitiesNoId.get(source) : entitiesWithId.get(source);
    }

    @BeforeMapping
    protected void storeMappedEntity(org.folio.ld.dictionary.model.Resource source, @MappingTarget Resource target) {
      (isNull(source.getId()) ? entitiesNoId : entitiesWithId).put(source, target);
    }
  }
}
