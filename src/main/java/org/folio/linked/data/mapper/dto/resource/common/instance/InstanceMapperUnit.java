package org.folio.linked.data.mapper.dto.resource.common.instance;

import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.ACCESS_LOCATION;
import static org.folio.ld.dictionary.PredicateDictionary.ADMIN_METADATA;
import static org.folio.ld.dictionary.PredicateDictionary.BOOK_FORMAT;
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
import static org.folio.ld.dictionary.PredicateDictionary.PUBLICATION_FREQUENCY;
import static org.folio.ld.dictionary.PredicateDictionary.SUPPLEMENTARY_CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.ACCOMPANYING_MATERIAL;
import static org.folio.ld.dictionary.PropertyDictionary.ADDITIONAL_PHYSICAL_FORM;
import static org.folio.ld.dictionary.PropertyDictionary.BIOGRAPHICAL_DATA;
import static org.folio.ld.dictionary.PropertyDictionary.COMPUTER_DATA_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DATES_OF_PUBLICATION_NOTE;
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
import static org.springframework.util.ObjectUtils.isEmpty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.InstanceRequest;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.InstanceResponseField;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.mapper.dto.FolioMetadataMapper;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.mapper.dto.resource.common.NoteMapper;
import org.folio.linked.data.mapper.dto.resource.common.TopResourceMapperUnit;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.profile.ResourceProfileLinkingService;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = INSTANCE, requestDto = InstanceField.class)
public class InstanceMapperUnit extends TopResourceMapperUnit {

  public static final Set<PropertyDictionary> SUPPORTED_NOTES = Set.of(ACCOMPANYING_MATERIAL, ADDITIONAL_PHYSICAL_FORM,
    BIOGRAPHICAL_DATA, COMPUTER_DATA_NOTE, DATES_OF_PUBLICATION_NOTE, DESCRIPTION_SOURCE_NOTE,
    EXHIBITIONS_NOTE, FUNDING_INFORMATION, HISTORICAL_DATA, ISSUANCE_NOTE, ISSUING_BODY,
    LOCATION_OF_OTHER_ARCHIVAL_MATERIAL, NOTE, ORIGINAL_VERSION_NOTE, PHYSICAL_DESCRIPTION, RELATED_PARTS,
    REPRODUCTION_NOTE, TYPE_OF_REPORT, WITH_NOTE);

  private final CoreMapper coreMapper;
  private final NoteMapper noteMapper;
  private final FolioMetadataMapper folioMetadataMapper;
  private final HashService hashService;
  private final ResourceProfileLinkingService resourceProfileService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof ResourceResponseDto resourceDto) {
      var instance = coreMapper.toDtoWithEdges(resourceToConvert, InstanceResponse.class, false);
      instance.setId(String.valueOf(resourceToConvert.getId()));
      ofNullable(resourceToConvert.getFolioMetadata())
        .map(folioMetadataMapper::toDto)
        .ifPresent(instance::setFolioMetadata);
      ofNullable(resourceToConvert.getDoc())
        .ifPresent(doc -> instance.setNotes(noteMapper.toNotes(doc, SUPPORTED_NOTES)));
      instance.setProfileId(resourceProfileService.resolveProfileId(resourceToConvert));
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
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getExtent(), EXTENT);
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, standardBookFormats(instanceDto), BOOK_FORMAT);
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getPublicationFrequency(),
      PUBLICATION_FREQUENCY);
    coreMapper.addOutgoingEdges(instance, InstanceRequest.class, instanceDto.getAdminMetadata(), ADMIN_METADATA);
    instance.setFolioMetadata(new FolioMetadata(instance).setSource(LINKED_DATA));
    instance.setIdAndRefreshEdges(hashService.hash(instance));
    return instance;
  }

  private JsonNode getDoc(InstanceRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, DIMENSIONS, dto.getDimensions());
    putProperty(map, EDITION, dto.getEdition());
    putProperty(map, PROJECTED_PROVISION_DATE, dto.getProjectProvisionDate());
    putProperty(map, ISSUANCE, dto.getIssuance());
    putProperty(map, STATEMENT_OF_RESPONSIBILITY, dto.getStatementOfResponsibility());
    putProperty(map, PropertyDictionary.BOOK_FORMAT, nonStandardBookFormats(dto));
    noteMapper.putNotes(dto.getNotes(), map);
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

  private List<Category> standardBookFormats(InstanceRequest instanceDto) {
    return instanceDto.getBookFormat().stream()
      .filter(category -> !isEmpty(category.getLink()))
      .toList();
  }

  List<String> nonStandardBookFormats(InstanceRequest instanceDto) {
    return instanceDto.getBookFormat().stream()
      .filter(category -> isEmpty(category.getLink()))
      .flatMap(format -> format.getTerm().stream())
      .toList();
  }
}
