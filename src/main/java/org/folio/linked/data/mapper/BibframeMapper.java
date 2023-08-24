package org.folio.linked.data.mapper;

import static org.apache.commons.lang3.StringUtils.firstNonEmpty;
import static org.folio.linked.data.util.BibframeConstants.DATE_URL;
import static org.folio.linked.data.util.BibframeConstants.EDITION_STATEMENT_URL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_URL;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.VALUE_PRED;
import static org.folio.search.domain.dto.BibframeIdentifiersInner.TypeEnum;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import org.folio.linked.data.domain.dto.Bibframe2Request;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.domain.dto.BibframeShort;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.ProfiledMapper;
import org.folio.linked.data.model.ResourceShortInfo;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.search.domain.dto.BibframeContributorsInner;
import org.folio.search.domain.dto.BibframeIdentifiersInner;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.BibframePublicationsInner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

@Mapper(componentModel = SPRING)
public abstract class BibframeMapper {

  @Autowired
  private ProfiledMapper profiledMapper;

  @Mapping(target = "id", source = "resourceHash")
  @Mapping(target = "profile", expression = "java(resourceShortInfo.getType().getSimpleLabel())")
  public abstract BibframeShort map(ResourceShortInfo resourceShortInfo);

  public abstract BibframeShortInfoPage map(Page<BibframeShort> page);

  public Resource map(Bibframe2Request dto) {
    var resource = profiledMapper.toEntity(dto);
    setEdgesId(resource);
    return resource;
  }

  public Bibframe2Response map(Resource resource) {
    return profiledMapper.toDto(resource);
  }

  public BibframeIndex mapToIndex(@NonNull Resource resource) {
    var instance = extractInstance(resource);
    var bibframeIndex = new BibframeIndex(resource.getResourceHash().toString());
    bibframeIndex.setTitle(instance.getLabel());
    bibframeIndex.setIdentifiers(extractIdentifiers(instance));
    bibframeIndex.setContributors(extractContributors(instance));
    bibframeIndex.setPublications(extractPublications(instance));
    bibframeIndex.setEditionStatement(getValue(instance.getDoc(), EDITION_STATEMENT_URL));
    return bibframeIndex;
  }

  private Resource extractInstance(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> INSTANCE_URL.equals(re.getPredicate().getLabel()))
      .map(ResourceEdge::getTarget)
      .findFirst()
      .orElseThrow(() -> new NotSupportedException("Only Monograph.Instance bibframe is supported for now, and there "
        + "is no Instance found"));
  }

  private List<BibframeIdentifiersInner> extractIdentifiers(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> IDENTIFIED_BY_PRED.equals(re.getPredicate().getLabel()))
      .map(ResourceEdge::getTarget)
      .map(ir -> new BibframeIdentifiersInner()
        .value(getValue(ir.getDoc(), VALUE_PRED))
        .type(TypeEnum.fromValue(ir.getType().getSimpleLabel().replace("lc:RT:bf2:Identifiers:", ""))))
      .toList();
  }

  private List<BibframeContributorsInner> extractContributors(Resource resource) {
    return new ArrayList<>(); // Lookup fields are not supported at the moment
  }

  private List<BibframePublicationsInner> extractPublications(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> PROVISION_ACTIVITY_PRED.equals(re.getPredicate().getLabel()))
      .map(ResourceEdge::getTarget)
      .filter(r -> PUBLICATION.equals(r.getType().getSimpleLabel()))
      .map(Resource::getDoc)
      .map(doc -> new BibframePublicationsInner()
        .dateOfPublication(firstNonEmpty(getValue(doc, SIMPLE_DATE_PRED), getValue(doc, DATE_URL)))
        .publisher(firstNonEmpty(getValue(doc, SIMPLE_AGENT_PRED), getValue(doc, SIMPLE_PLACE_PRED)))
      )
      .toList();
  }

  private String getValue(JsonNode doc, String value) {
    return doc.has(value)
      ? doc.get(value).get(0).textValue()
      : null;
  }

  private void setEdgesId(Resource resource) {
    resource.getOutgoingEdges().forEach(edge -> {
      edge.getId().setSourceHash(edge.getSource().getResourceHash());
      edge.getId().setTargetHash(edge.getTarget().getResourceHash());
      edge.getId().setPredicateHash(edge.getPredicate().getPredicateHash());
      setEdgesId(edge.getTarget());
    });
  }

}
