package org.folio.linked.data.mapper.dto.monograph.instance;

import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.ACCESS_LOCATION;
import static org.folio.ld.dictionary.PredicateDictionary.CARRIER;
import static org.folio.ld.dictionary.PredicateDictionary.COPYRIGHT;
import static org.folio.ld.dictionary.PredicateDictionary.EXTENT;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.MEDIA;
import static org.folio.ld.dictionary.PredicateDictionary.PE_DISTRIBUTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_MANUFACTURE;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PRODUCTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.SUPPLEMENTARY_CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.ACCOMPANYING_MATERIAL;
import static org.folio.ld.dictionary.PropertyDictionary.ADDITIONAL_PHYSICAL_FORM;
import static org.folio.ld.dictionary.PropertyDictionary.BIOGRAPHICAL_DATA;
import static org.folio.ld.dictionary.PropertyDictionary.COMPUTER_DATA_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DESCRIPTION_SOURCE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION;
import static org.folio.ld.dictionary.PropertyDictionary.EXHIBITIONS_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.FUNDING_INFORMATION;
import static org.folio.ld.dictionary.PropertyDictionary.HISTORICAL_DATA;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUANCE;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUANCE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUING_BODY;
import static org.folio.ld.dictionary.PropertyDictionary.LOCATION_OF_OTHER_ARCHIVAL_MATERIAL;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.ORIGINAL_VERSION_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.PHYSICAL_DESCRIPTION;
import static org.folio.ld.dictionary.PropertyDictionary.PROJECTED_PROVISION_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.RELATED_PARTS;
import static org.folio.ld.dictionary.PropertyDictionary.REPRODUCTION_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.STATEMENT_OF_RESPONSIBILITY;
import static org.folio.ld.dictionary.PropertyDictionary.TYPE_OF_REPORT;
import static org.folio.ld.dictionary.PropertyDictionary.WITH_NOTE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.getPrimaryMainTitles;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.InstanceRequest;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.InstanceResponseField;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.mapper.dto.FolioMetadataMapper;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.TopResourceMapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.NoteMapper;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = INSTANCE, requestDto = InstanceField.class)
public class InstanceMapperUnit extends TopResourceMapperUnit {

  public static final Set<PropertyDictionary> SUPPORTED_NOTES = Set.of(ADDITIONAL_PHYSICAL_FORM, COMPUTER_DATA_NOTE,
    DESCRIPTION_SOURCE_NOTE, EXHIBITIONS_NOTE, FUNDING_INFORMATION, ISSUANCE_NOTE, ISSUING_BODY,
    LOCATION_OF_OTHER_ARCHIVAL_MATERIAL, NOTE, ORIGINAL_VERSION_NOTE, RELATED_PARTS, REPRODUCTION_NOTE, TYPE_OF_REPORT,
    WITH_NOTE);

  private final CoreMapper coreMapper;
  private final NoteMapper noteMapper;
  private final FolioMetadataMapper folioMetadataMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof ResourceResponseDto resourceDto) {
      var instance = coreMapper.toDtoWithEdges(source, InstanceResponse.class, false);
      instance.setId(String.valueOf(source.getId()));
      ofNullable(source.getFolioMetadata())
        .map(folioMetadataMapper::toDto)
        .ifPresent(instance::setFolioMetadata);
      ofNullable(source.getDoc())
        .ifPresent(doc -> instance.setNotes(noteMapper.toNotes(doc, SUPPORTED_NOTES)));
      resourceDto.resource(new InstanceResponseField().instance(instance));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var instanceDto = ((InstanceField) dto).getInstance();
    var instance = new Resource();
    instance.addTypes(INSTANCE);
    instance.setDoc(getDoc(instanceDto));
    instance.setLabel(getFirstValue(() -> getPrimaryMainTitles(instanceDto.getTitle())));
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getTitle(), TITLE);
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getProduction(), PE_PRODUCTION);
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getPublication(), PE_PUBLICATION);
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getDistribution(), PE_DISTRIBUTION);
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getManufacture(), PE_MANUFACTURE);
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getSupplementaryContent(),
      SUPPLEMENTARY_CONTENT);
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getAccessLocation(), ACCESS_LOCATION);
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getMap(), MAP);
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getMedia(), MEDIA);
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getCarrier(), CARRIER);
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getCopyright(), COPYRIGHT);
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getWorkReference(), INSTANTIATES);
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getExtentV2(), EXTENT);
    instance.setFolioMetadata(new FolioMetadata(instance).setSource(LINKED_DATA));
    instance.setId(hashService.hash(instance));
    return instance;
  }

  private JsonNode getDoc(InstanceRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, PropertyDictionary.EXTENT, dto.getExtent());
    putProperty(map, DIMENSIONS, dto.getDimensions());
    putProperty(map, EDITION, dto.getEdition());
    putProperty(map, PROJECTED_PROVISION_DATE, dto.getProjectProvisionDate());
    putProperty(map, ISSUANCE, dto.getIssuance());
    putProperty(map, STATEMENT_OF_RESPONSIBILITY, dto.getStatementOfResponsibility());
    putProperty(map, HISTORICAL_DATA, dto.getAdminhist());
    putProperty(map, BIOGRAPHICAL_DATA, dto.getBiogdata());
    putProperty(map, PHYSICAL_DESCRIPTION, dto.getPhysicalDescription());
    putProperty(map, ACCOMPANYING_MATERIAL, dto.getAccompanyingMaterial());
    noteMapper.putNotes(dto.getNotes(), map);
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
