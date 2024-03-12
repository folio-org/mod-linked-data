package org.folio.linked.data.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Qualifier;
import org.mapstruct.TargetType;

@Mapper(componentModel = SPRING)
public abstract class ResourceModelMapper {

  @NotForGeneration
  public Resource toEntity(org.folio.ld.dictionary.model.Resource model) {
    return toEntity(model, new CyclicGraphContext());
  }

  protected abstract Resource toEntity(org.folio.ld.dictionary.model.Resource model,
                                       @Context CyclicGraphContext cycleContext);

  @NotForGeneration
  public org.folio.ld.dictionary.model.Resource toModel(Resource entity) {
    return toModel(entity, new CyclicGraphContext());
  }

  protected abstract org.folio.ld.dictionary.model.Resource toModel(Resource entity,
                                                                    @Context CyclicGraphContext cycleContext);

  public ResourceTypeDictionary map(ResourceTypeEntity typeEntity) {
    return ResourceTypeDictionary.fromUri(typeEntity.getUri()).orElse(null);
  }

  public PredicateDictionary map(PredicateEntity predicateEntity) {
    return PredicateDictionary.fromUri(predicateEntity.getUri()).orElse(null);
  }

  @Qualifier
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.CLASS)
  public @interface NotForGeneration {
  }

  public static class CyclicGraphContext {
    private final Map<Resource, org.folio.ld.dictionary.model.Resource> convertedModels = new HashMap<>();
    private final Map<org.folio.ld.dictionary.model.Resource, Resource> convertedEntities = new HashMap<>();

    @BeforeMapping
    public org.folio.ld.dictionary.model.Resource getMappedModel(
      Resource source, @TargetType Class<org.folio.ld.dictionary.model.Resource> target) {
      return convertedModels.get(source);
    }

    @BeforeMapping
    public void storeMappedModel(Resource source, @MappingTarget org.folio.ld.dictionary.model.Resource target) {
      convertedModels.put(source, target);
    }

    @BeforeMapping
    public Resource getMappedEntity(org.folio.ld.dictionary.model.Resource source, @TargetType Class<Resource> target) {
      return convertedEntities.get(source);
    }

    @BeforeMapping
    public void storeMappedEntity(org.folio.ld.dictionary.model.Resource source, @MappingTarget Resource target) {
      convertedEntities.put(source, target);
    }

  }
}
