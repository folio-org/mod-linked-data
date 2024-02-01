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

  private final CoreMapper coreMapper;
  private final NoteMapper noteMapper;

  @Override
  public <T> T toDto(Resource source, T parentDto, Resource parentResource) {
    var instanceField = new InstanceField();
    coreMapper.mapToDtoWithEdges(source, instanceField::setInstance, Instance.class);
    instanceField.getInstance().setId(String.valueOf(source.getResourceHash()));
    instanceField.getInstance().setInventoryId(source.getInventoryId());
    instanceField.getInstance().setSrsId(source.getSrsId());

    ofNullable(source.getDoc())
      .ifPresent(doc -> instanceField.getInstance().setNotes(noteMapper.toNotes(doc, SUPPORTED_NOTES)));

    if (parentDto instanceof ResourceDto resourceDto) {
      resourceDto.resource(instanceField);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    Instance instanceDto = ((InstanceField) dto).getInstance();
    var instance = new Resource();
    instance.addType(INSTANCE);
    instance.setDoc(getDoc(instanceDto));
    instance.setLabel(getFirstValue(() -> getPossibleLabels(instanceDto)));
    instance.setInventoryId(instanceDto.getInventoryId());
    instance.setSrsId(instanceDto.getSrsId());
    coreMapper.toOutgoingEdges(instanceDto.getTitle(), instance, TITLE, Instance.class);
    coreMapper.toOutgoingEdges(instanceDto.getProduction(), instance, PE_PRODUCTION, Instance.class);
    coreMapper.toOutgoingEdges(instanceDto.getPublication(), instance, PE_PUBLICATION, Instance.class);
    coreMapper.toOutgoingEdges(instanceDto.getDistribution(), instance, PE_DISTRIBUTION, Instance.class);
    coreMapper.toOutgoingEdges(instanceDto.getManufacture(), instance, PE_MANUFACTURE, Instance.class);
    coreMapper.toOutgoingEdges(instanceDto.getSupplementaryContent(), instance, SUPPLEMENTARY_CONTENT, Instance.class);
    coreMapper.toOutgoingEdges(instanceDto.getAccessLocation(), instance, ACCESS_LOCATION, Instance.class);
    coreMapper.toOutgoingEdges(instanceDto.getMap(), instance, MAP, Instance.class);
    coreMapper.toOutgoingEdges(instanceDto.getMedia(), instance, MEDIA, Instance.class);
    coreMapper.toOutgoingEdges(instanceDto.getCarrier(), instance, CARRIER, Instance.class);
    coreMapper.toOutgoingEdges(instanceDto.getCopyright(), instance, COPYRIGHT, Instance.class);
    coreMapper.toOutgoingEdges(instanceDto.getWorkReference(), instance, INSTANTIATES, Instance.class);
    // DEPRECATED: to be removed, temporary support of current ui
    coreMapper.toOutgoingEdges(instanceDto.getInstantiates(), instance, INSTANTIATES, Instance.class);
    instance.setResourceHash(coreMapper.hash(instance));
    return instance;
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return Collections.singleton(ResourceDto.class);
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
