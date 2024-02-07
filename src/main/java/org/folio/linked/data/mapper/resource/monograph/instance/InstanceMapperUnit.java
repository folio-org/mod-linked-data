package org.folio.linked.data.mapper.resource.monograph.instance;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.ACCESS_LOCATION;
import static org.folio.ld.dictionary.PredicateDictionary.CARRIER;
import static org.folio.ld.dictionary.PredicateDictionary.COPYRIGHT;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.MEDIA;
import static org.folio.ld.dictionary.PredicateDictionary.PE_DISTRIBUTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_MANUFACTURE;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PRODUCTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.SUPPLEMENTARY_CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.ADDITIONAL_PHYSICAL_FORM;
import static org.folio.ld.dictionary.PropertyDictionary.COMPUTER_DATA_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DESCRIPTION_SOURCE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION_STATEMENT;
import static org.folio.ld.dictionary.PropertyDictionary.EXHIBITIONS_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.EXTENT;
import static org.folio.ld.dictionary.PropertyDictionary.FUNDING_INFORMATION;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUANCE;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUANCE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUING_BODY;
import static org.folio.ld.dictionary.PropertyDictionary.LOCATION_OF_OTHER_ARCHIVAL_MATERIAL;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.ORIGINAL_VERSION_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.PROJECTED_PROVISION_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.RELATED_PARTS;
import static org.folio.ld.dictionary.PropertyDictionary.REPRODUCTION_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.TYPE_OF_REPORT;
import static org.folio.ld.dictionary.PropertyDictionary.WITH_NOTE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.InstanceTitleField;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.SingleResourceMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.common.NoteMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = INSTANCE, dtoClass = InstanceField.class)
public class InstanceMapperUnit implements SingleResourceMapperUnit {

  private static final Set<PropertyDictionary> SUPPORTED_NOTES = Set.of(ADDITIONAL_PHYSICAL_FORM, COMPUTER_DATA_NOTE,
    DESCRIPTION_SOURCE_NOTE, EXHIBITIONS_NOTE, FUNDING_INFORMATION, ISSUANCE_NOTE, ISSUING_BODY,
    LOCATION_OF_OTHER_ARCHIVAL_MATERIAL, NOTE, ORIGINAL_VERSION_NOTE, RELATED_PARTS, REPRODUCTION_NOTE, TYPE_OF_REPORT,
    WITH_NOTE);
  private static final Set<Class<?>> SUPPORTED_PARENTS = Collections.singleton(ResourceDto.class);

  private final CoreMapper coreMapper;
  private final NoteMapper noteMapper;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof ResourceDto resourceDto) {
      var instance = coreMapper.toDtoWithEdges(source, Instance.class, false);
      instance.setId(String.valueOf(source.getResourceHash()));
      instance.setInventoryId(source.getInventoryId());
      instance.setSrsId(source.getSrsId());
      ofNullable(source.getDoc())
        .ifPresent(doc -> instance.setNotes(noteMapper.toNotes(doc, SUPPORTED_NOTES)));
      resourceDto.resource(new InstanceField().instance(instance));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var instanceDto = ((InstanceField) dto).getInstance();
    var instance = new Resource();
    instance.addType(INSTANCE);
    instance.setDoc(getDoc(instanceDto));
    instance.setLabel(getFirstValue(() -> getPossibleLabels(instanceDto)));
    instance.setInventoryId(instanceDto.getInventoryId());
    instance.setSrsId(instanceDto.getSrsId());
    coreMapper.addOutgoingEdges(instance, Instance.class, instanceDto.getTitle(), TITLE);
    coreMapper.addOutgoingEdges(instance, Instance.class, instanceDto.getProduction(), PE_PRODUCTION);
    coreMapper.addOutgoingEdges(instance, Instance.class, instanceDto.getPublication(), PE_PUBLICATION);
    coreMapper.addOutgoingEdges(instance, Instance.class, instanceDto.getDistribution(), PE_DISTRIBUTION);
    coreMapper.addOutgoingEdges(instance, Instance.class, instanceDto.getManufacture(), PE_MANUFACTURE);
    coreMapper.addOutgoingEdges(instance, Instance.class, instanceDto.getSupplementaryContent(), SUPPLEMENTARY_CONTENT);
    coreMapper.addOutgoingEdges(instance, Instance.class, instanceDto.getAccessLocation(), ACCESS_LOCATION);
    coreMapper.addOutgoingEdges(instance, Instance.class, instanceDto.getMap(), MAP);
    coreMapper.addOutgoingEdges(instance, Instance.class, instanceDto.getMedia(), MEDIA);
    coreMapper.addOutgoingEdges(instance, Instance.class, instanceDto.getCarrier(), CARRIER);
    coreMapper.addOutgoingEdges(instance, Instance.class, instanceDto.getCopyright(), COPYRIGHT);
    coreMapper.addOutgoingEdges(instance, Instance.class, instanceDto.getWorkReference(), INSTANTIATES);
    // DEPRECATED: to be removed, temporary support of current ui
    coreMapper.addOutgoingEdges(instance, Instance.class, instanceDto.getInstantiates(), INSTANTIATES);
    instance.setResourceHash(coreMapper.hash(instance));
    return instance;
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }

  private List<String> getPossibleLabels(Instance instance) {
    if (isNull(instance.getTitle())) {
      return new ArrayList<>();
    }
    return instance.getTitle().stream()
      .sorted(Comparator.comparing(o -> o.getClass().getSimpleName()))
      .map(t -> {
        if (t instanceof InstanceTitleField instanceTitleField) {
          var instanceTitle = instanceTitleField.getInstanceTitle();
          return getFirstValue(instanceTitle::getMainTitle);
        }
        if (t instanceof ParallelTitleField parallelTitleField) {
          var parallelTitle = parallelTitleField.getParallelTitle();
          return getFirstValue(parallelTitle::getMainTitle);
        }
        if (t instanceof VariantTitleField variantTitleField) {
          var variantTitle = variantTitleField.getVariantTitle();
          return getFirstValue(variantTitle::getMainTitle);
        }
        return "";
      }).toList();
  }

  private JsonNode getDoc(Instance dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, EXTENT, dto.getExtent());
    putProperty(map, DIMENSIONS, dto.getDimensions());
    putProperty(map, EDITION_STATEMENT, dto.getEdition());
    putProperty(map, PROJECTED_PROVISION_DATE, dto.getProjectProvisionDate());
    putProperty(map, ISSUANCE, dto.getIssuance());
    noteMapper.putNotes(dto.getNotes(), map);
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
