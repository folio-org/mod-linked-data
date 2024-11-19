package org.folio.linked.data.e2e.resource;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.ACCESS_LOCATION;
import static org.folio.ld.dictionary.PredicateDictionary.ASSIGNEE;
import static org.folio.ld.dictionary.PredicateDictionary.AUTHOR;
import static org.folio.ld.dictionary.PredicateDictionary.CARRIER;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.COPYRIGHT;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.DISSERTATION;
import static org.folio.ld.dictionary.PredicateDictionary.EDITOR;
import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.PredicateDictionary.GEOGRAPHIC_COVERAGE;
import static org.folio.ld.dictionary.PredicateDictionary.GOVERNMENT_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.IS_DEFINED_BY;
import static org.folio.ld.dictionary.PredicateDictionary.LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.MEDIA;
import static org.folio.ld.dictionary.PredicateDictionary.ORIGIN_PLACE;
import static org.folio.ld.dictionary.PredicateDictionary.PE_DISTRIBUTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_MANUFACTURE;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PRODUCTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.PredicateDictionary.STATUS;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PredicateDictionary.SUPPLEMENTARY_CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.TARGET_AUDIENCE;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.ADDITIONAL_PHYSICAL_FORM;
import static org.folio.ld.dictionary.PropertyDictionary.ASSIGNING_SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.BIBLIOGRAPHY_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.COMPUTER_DATA_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE_END;
import static org.folio.ld.dictionary.PropertyDictionary.DATE_START;
import static org.folio.ld.dictionary.PropertyDictionary.DEGREE;
import static org.folio.ld.dictionary.PropertyDictionary.DESCRIPTION_SOURCE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.DISSERTATION_ID;
import static org.folio.ld.dictionary.PropertyDictionary.DISSERTATION_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DISSERTATION_YEAR;
import static org.folio.ld.dictionary.PropertyDictionary.EAN_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.EXHIBITIONS_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.EXTENT;
import static org.folio.ld.dictionary.PropertyDictionary.FUNDING_INFORMATION;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUANCE;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUANCE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUING_BODY;
import static org.folio.ld.dictionary.PropertyDictionary.ITEM_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LANGUAGE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.LCNAF_ID;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.LOCAL_ID_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.LOCATION_OF_OTHER_ARCHIVAL_MATERIAL;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.NON_SORT_NUM;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.ORIGINAL_VERSION_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.PROJECTED_PROVISION_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.PROVIDER_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.ld.dictionary.PropertyDictionary.RELATED_PARTS;
import static org.folio.ld.dictionary.PropertyDictionary.REPRODUCTION_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.SIMPLE_PLACE;
import static org.folio.ld.dictionary.PropertyDictionary.SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.STATEMENT_OF_RESPONSIBILITY;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.PropertyDictionary.SUMMARY;
import static org.folio.ld.dictionary.PropertyDictionary.TABLE_OF_CONTENTS;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.ld.dictionary.PropertyDictionary.TYPE_OF_REPORT;
import static org.folio.ld.dictionary.PropertyDictionary.VARIANT_TYPE;
import static org.folio.ld.dictionary.PropertyDictionary.WITH_NOTE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY_SET;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.COPYRIGHT_EVENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LOCAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LANGUAGE_CATEGORY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.domain.dto.InstanceIngressEvent.EventTypeEnum;
import static org.folio.linked.data.domain.dto.InstanceIngressEvent.EventTypeEnum.CREATE_INSTANCE;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.DELETE;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.UPDATE;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.INSTANCE_WITH_WORK_REF_SAMPLE;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.SIMPLE_WORK_WITH_INSTANCE_REF_SAMPLE;
import static org.folio.linked.data.test.TestUtil.WORK_WITH_INSTANCE_REF_SAMPLE;
import static org.folio.linked.data.test.TestUtil.cleanResourceTables;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getSampleInstanceDtoMap;
import static org.folio.linked.data.test.TestUtil.getSampleWorkDtoMap;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.client.SrsClient;
import org.folio.linked.data.domain.dto.InstanceResponseField;
import org.folio.linked.data.domain.dto.ResourceIndexEventType;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.WorkResponseField;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.service.resource.hash.HashService;
import org.folio.linked.data.test.ResourceTestService;
import org.folio.linked.data.test.TestUtil;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

public abstract class ResourceControllerITBase {

  public static final String RESOURCE_URL = "/resource";
  private static final String ROLES_PROPERTY = "roles";
  private static final String NOTES_PROPERTY = "_notes";
  private static final String ID_PROPERTY = "id";
  private static final String LABEL_PROPERTY = "label";
  private static final String VALUE_PROPERTY = "value";
  private static final String TYPE_PROPERTY = "type";
  private static final String INSTANCE_REF = "_instanceReference";
  private static final String WORK_REF = "_workReference";
  private static final String CREATOR_REF = "_creatorReference";
  private static final String CONTRIBUTOR_REF = "_contributorReference";
  private static final String GEOGRAPHIC_COVERAGE_REF = "_geographicCoverageReference";
  private static final String GENRE_REF = "_genreReference";
  private static final String ASSIGNING_SOURCE_REF = "_assigningSourceReference";
  private static final String GRANTING_INSTITUTION_REF = "_grantingInstitutionReference";
  private static final String WORK_ID_PLACEHOLDER = "%WORK_ID%";
  private static final String INSTANCE_ID_PLACEHOLDER = "%INSTANCE_ID%";
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private Environment env;
  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  private ResourceTestService resourceTestService;
  private LookupResources lookupResources;
  @Autowired
  private HashService hashService;
  @MockBean
  private SrsClient srsClient;

  @BeforeEach
  public void beforeEach() {
    cleanResourceTables(jdbcTemplate);
    lookupResources = saveLookupResources();
  }

  @Test
  void createInstanceWithWorkRef_shouldSaveEntityCorrectly() throws Exception {
    // given
    var work = getSampleWork(null);
    setExistingResourcesIds(work);
    resourceTestService.saveGraph(work);
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(INSTANCE_WITH_WORK_REF_SAMPLE.replaceAll(WORK_ID_PLACEHOLDER, work.getId().toString()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    validateInstanceResponse(resultActions, toInstance());

    var resourceResponse = OBJECT_MAPPER.readValue(response, ResourceResponseDto.class);
    var instanceResponse = ((InstanceResponseField) resourceResponse.getResource()).getInstance();
    var instanceResource = resourceTestService.getResourceById(instanceResponse.getId(), 4);
    assertThat(instanceResource.getFolioMetadata().getSource()).isEqualTo(LINKED_DATA);
    validateInstance(instanceResource, true);
    var workId = instanceResponse.getWorkReference().get(0).getId();
    checkSearchIndexMessage(Long.valueOf(workId), UPDATE);
    checkIndexDate(workId);
    checkInventoryMessage(instanceResource.getId(), CREATE_INSTANCE);
  }

  @Test
  void createInstanceWithWorkRef_shouldReturn400_ifLccnIsInvalid() throws Exception {
    // given
    var work = getSampleWork(null);
    setExistingResourcesIds(work);
    resourceTestService.saveGraph(work);
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(INSTANCE_WITH_WORK_REF_SAMPLE
        .replaceAll(WORK_ID_PLACEHOLDER, work.getId().toString())
        .replace("lccn status link", "http://id.loc.gov/vocabulary/mstatus/current")
      );

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].code", equalTo("lccn_does_not_match_pattern")))
      .andExpect(jsonPath("errors[0].parameters", hasSize(2)))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }

  @Test
  void createWorkWithInstanceRef_shouldCreateAuthorityFromSrs() throws Exception {
    // given
    var instanceForReference = getSampleInstanceResource(null, null);
    setExistingResourcesIds(instanceForReference);
    resourceTestService.saveGraph(instanceForReference);
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(
        SIMPLE_WORK_WITH_INSTANCE_REF_SAMPLE
          .replaceAll(INSTANCE_ID_PLACEHOLDER, instanceForReference.getId().toString())
      );

    when(srsClient.getSourceStorageRecordBySrsId("4f2220d5-ddf6-410a-a459-cd4b5e1b5ddd"))
      .thenReturn(new ResponseEntity<>(createRecord(), HttpStatusCode.valueOf(200)));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(toCreatorReferenceId(), equalTo("8288857748391775847")));
  }

  @Test
  void createWorkWithInstanceRef_shouldReturn404_ifRecordNotFoundInSrs() throws Exception {
    // given
    var instanceForReference = getSampleInstanceResource(null, null);
    setExistingResourcesIds(instanceForReference);
    resourceTestService.saveGraph(instanceForReference);
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(
        SIMPLE_WORK_WITH_INSTANCE_REF_SAMPLE
          .replaceAll(INSTANCE_ID_PLACEHOLDER, instanceForReference.getId().toString())
      );

    when(srsClient.getSourceStorageRecordBySrsId("4f2220d5-ddf6-410a-a459-cd4b5e1b5ddd"))
      .thenReturn(new ResponseEntity<>(null, HttpStatusCode.valueOf(404)));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("errors[0].message",
        equalTo("Source Record not found by srsId: [4f2220d5-ddf6-410a-a459-cd4b5e1b5ddd] in Source Record storage")))
      .andExpect(jsonPath("errors[0].code", equalTo("not_found")))
      .andExpect(jsonPath("errors[0].parameters", hasSize(4)))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }

  private org.folio.rest.jaxrs.model.Record createRecord() {
    var content = TestUtil.loadResourceAsString("samples/marc2ld/marc_authority.jsonl");
    var parsedRecord = new ParsedRecord().withContent(content);
    return new Record().withParsedRecord(parsedRecord);
  }

  @Test
  void createWorkWithInstanceRef_shouldSaveEntityCorrectly() throws Exception {
    // given
    var instanceForReference = getSampleInstanceResource(null, null);
    setExistingResourcesIds(instanceForReference);
    resourceTestService.saveGraph(instanceForReference);
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(
        WORK_WITH_INSTANCE_REF_SAMPLE.replaceAll(INSTANCE_ID_PLACEHOLDER, instanceForReference.getId().toString())
      );

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    validateWorkResponse(resultActions, toWork());

    var resourceResponse = OBJECT_MAPPER.readValue(response, ResourceResponseDto.class);
    var id = ((WorkResponseField) resourceResponse.getResource()).getWork().getId();
    var workResource = resourceTestService.getResourceById(id, 4);
    validateWork(workResource, true);
    checkSearchIndexMessage(workResource.getId(), CREATE);
    checkIndexDate(workResource.getId().toString());
  }

  @Test
  void update_shouldReturnCorrectlyUpdatedInstanceWithWorkRef_deleteOldOne_sendMessages() throws Exception {
    // given
    var work = getSampleWork(null);
    var originalInstance = resourceTestService.saveGraph(getSampleInstanceResource(null, work));
    var updateDto = getSampleInstanceDtoMap();
    var instanceMap = (LinkedHashMap) ((LinkedHashMap) updateDto.get("resource")).get(INSTANCE.getUri());
    instanceMap.put(DIMENSIONS.getValue(), List.of("200 m"));
    instanceMap.remove("inventoryId");
    instanceMap.remove("srsId");

    var updateRequest = put(RESOURCE_URL + "/" + originalInstance.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(
        OBJECT_MAPPER.writeValueAsString(updateDto).replaceAll(WORK_ID_PLACEHOLDER, work.getId().toString())
      );

    // when
    var resultActions = mockMvc.perform(updateRequest);

    // then
    assertFalse(resourceTestService.existsById(originalInstance.getId()));
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(toInstance(), notNullValue()))
      .andReturn().getResponse().getContentAsString();
    var resourceResponse = OBJECT_MAPPER.readValue(response, ResourceResponseDto.class);
    var instanceDto = ((InstanceResponseField) resourceResponse.getResource()).getInstance();
    var updatedInstance = resourceTestService.getResourceById(instanceDto.getId(), 1);
    assertThat(updatedInstance.getId()).isNotNull();
    assertThat(updatedInstance.getLabel()).isEqualTo(originalInstance.getLabel());
    assertThat(updatedInstance.getTypes().iterator().next().getUri()).isEqualTo(INSTANCE.getUri());
    assertThat(updatedInstance.getDoc().get(DIMENSIONS.getValue()).get(0).asText()).isEqualTo("200 m");
    assertThat(updatedInstance.getOutgoingEdges()).hasSize(originalInstance.getOutgoingEdges().size());

    var updatedFolioMetadata = updatedInstance.getFolioMetadata();
    var originalFolioMetadata = originalInstance.getFolioMetadata();
    var folioMetadataDto = instanceDto.getFolioMetadata();
    assertThat(updatedFolioMetadata.getInventoryId())
      .isEqualTo(folioMetadataDto.getInventoryId())
      .isEqualTo(originalFolioMetadata.getInventoryId());
    assertThat(updatedFolioMetadata.getSrsId())
      .isEqualTo(folioMetadataDto.getSrsId())
      .isEqualTo(originalFolioMetadata.getSrsId());
    assertThat(updatedFolioMetadata.getSource().name())
      .isEqualTo(folioMetadataDto.getSource().name())
      .isEqualTo(LINKED_DATA.name());

    checkSearchIndexMessage(work.getId(), UPDATE);
    checkIndexDate(work.getId().toString());
  }

  @Test
  void update_shouldReturn400_ifLccnIsInvalid() throws Exception {
    // given
    var updateDto = getSampleInstanceDtoMap();
    var instance = (LinkedHashMap) ((LinkedHashMap) updateDto.get("resource")).get(INSTANCE.getUri());
    instance.remove("inventoryId");
    instance.remove("srsId");
    var status = getStatus(instance);
    ((LinkedHashMap) status.get(0)).put(LINK.getValue(), List.of("http://id.loc.gov/vocabulary/mstatus/current"));
    var work = getSampleWork(null);
    var originalInstance = resourceTestService.saveGraph(getSampleInstanceResource(null, work));

    var updateRequest = put(RESOURCE_URL + "/" + originalInstance.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(
        OBJECT_MAPPER.writeValueAsString(updateDto).replaceAll(WORK_ID_PLACEHOLDER, work.getId().toString())
      );

    // when
    var resultActions = mockMvc.perform(updateRequest);

    // then
    resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].code", equalTo("lccn_does_not_match_pattern")))
      .andExpect(jsonPath("errors[0].parameters", hasSize(2)))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }

  @Test
  void update_shouldReturnCorrectlyUpdatedWorkWithInstanceRef_deleteOldOne_sendMessages() throws Exception {
    // given
    var instance = getSampleInstanceResource(null, null);
    var originalWork = getSampleWork(instance);
    setExistingResourcesIds(instance);
    resourceTestService.saveGraph(originalWork);
    var updateDto = getSampleWorkDtoMap();
    var workMap = (LinkedHashMap) ((LinkedHashMap) updateDto.get("resource")).get(WORK.getUri());
    workMap.put(PropertyDictionary.LANGUAGE.getValue(),
      Map.of(
        LINK.getValue(), List.of("http://id.loc.gov/vocabulary/languages/rus"),
        TERM.getValue(), List.of("Russian")
      ));

    var updateRequest = put(RESOURCE_URL + "/" + originalWork.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(OBJECT_MAPPER.writeValueAsString(updateDto)
        .replaceAll(INSTANCE_ID_PLACEHOLDER, instance.getId().toString())
      );

    // when
    var resultActions = mockMvc.perform(updateRequest);

    // then
    assertFalse(resourceTestService.existsById(originalWork.getId()));
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(toWork(), notNullValue()))
      .andReturn().getResponse().getContentAsString();
    var resourceResponse = OBJECT_MAPPER.readValue(response, ResourceResponseDto.class);
    var id = ((WorkResponseField) resourceResponse.getResource()).getWork().getId();

    var updatedWork = resourceTestService.getResourceById(id, 1);
    assertThat(updatedWork.getId()).isNotNull();
    assertThat(updatedWork.getLabel()).isEqualTo(originalWork.getLabel());
    assertThat(updatedWork.getTypes().iterator().next().getUri()).isEqualTo(WORK.getUri());
    assertThat(
      updatedWork.getOutgoingEdges()
        .stream()
        .filter(resourceEdge -> LANGUAGE.getUri().equals(resourceEdge.getPredicate().getUri()))
        .map(ResourceEdge::getTarget)
        .findFirst()
        .map(Resource::getDoc)
        .map(jsonNode -> jsonNode.get(TERM.getValue()))
        .map(jsonNode -> jsonNode.get(0))
        .map(JsonNode::asText)
    ).contains("Russian");
    assertThat(updatedWork.getOutgoingEdges()).hasSize(originalWork.getOutgoingEdges().size());
    assertThat(updatedWork.getIncomingEdges()).hasSize(originalWork.getIncomingEdges().size());
    checkSearchIndexMessage(originalWork.getId(), DELETE);
    checkSearchIndexMessage(Long.valueOf(id), CREATE);
    checkIndexDate(id);
  }

  @Test
  void getInstanceById_shouldReturnInstanceWithWorkRef() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleInstanceResource());
    var requestBuilder = get(RESOURCE_URL + "/" + existed.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    validateInstanceResponse(resultActions, toInstance());
  }

  @Test
  void getWorkById_shouldReturnWorkWithInstanceRef() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleWork(getSampleInstanceResource(null, null)));
    var requestBuilder = get(RESOURCE_URL + "/" + existed.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    validateWorkResponse(resultActions, toWork());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "%s/%s",
    "%s/%s/marc"
  })
  void getResourceById_shouldReturn404_ifNoExistedEntity(String pattern) throws Exception {
    // given
    var notExistedId = randomLong();
    var path = format(pattern, RESOURCE_URL, notExistedId);
    var requestBuilder = get(path)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isNotFound())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].message",
        equalTo("Resource not found by id: [" + notExistedId + "] in Linked Data storage")))
      .andExpect(jsonPath("errors[0].code", equalTo("not_found")))
      .andExpect(jsonPath("errors[0].parameters", hasSize(4)))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }

  @Test
  void getResourceIdByResourceInventoryId_shouldReturnResourceId() throws Exception {
    //given
    var resource = resourceTestService.saveGraph(getSampleInstanceResource(null, null));
    var requestBuilder = get(RESOURCE_URL + "/metadata/" + resource.getFolioMetadata().getInventoryId() + "/id")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id", equalTo(String.valueOf(resource.getId()))));
  }

  @Test
  void getResourceIdByResourceInventoryId_shouldReturn404_ifNoEntityExistsWithGivenInventoryId() throws Exception {
    //given
    var inventoryId = UUID.randomUUID();
    var requestBuilder = get(RESOURCE_URL + "/metadata/" + inventoryId + "/id")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    resultActions
      .andExpect(status().isNotFound())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].message",
        equalTo("Resource not found by inventoryId: [" + inventoryId + "] in Linked Data storage")))
      .andExpect(jsonPath("errors[0].parameters", hasSize(4)))
      .andExpect(jsonPath("errors[0].code", equalTo("not_found")))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }

  @Test
  void deleteResourceById_shouldDeleteRootInstanceAndRootEdges_reindexWork() throws Exception {
    // given
    var work = getSampleWork(null);
    var instance = resourceTestService.saveGraph(getSampleInstanceResource(null, work));
    assertThat(resourceTestService.findById(instance.getId())).isPresent();
    assertThat(resourceTestService.countResources()).isEqualTo(57);
    assertThat(resourceTestService.countEdges()).isEqualTo(59);
    var requestBuilder = delete(RESOURCE_URL + "/" + instance.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions.andExpect(status().isNoContent());
    assertThat(resourceTestService.existsById(instance.getId())).isFalse();
    assertThat(resourceTestService.countResources()).isEqualTo(56);
    assertThat(resourceTestService.findEdgeById(instance.getOutgoingEdges().iterator().next().getId())).isNotPresent();
    assertThat(resourceTestService.countEdges()).isEqualTo(41);
    checkSearchIndexMessage(work.getId(), UPDATE);
    checkIndexDate(work.getId().toString());
  }

  @Test
  void deleteResourceById_shouldDeleteRootWorkAndRootEdges() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleWork(getSampleInstanceResource(null, null)));
    assertThat(resourceTestService.findById(existed.getId())).isPresent();
    assertThat(resourceTestService.countResources()).isEqualTo(57);
    assertThat(resourceTestService.countEdges()).isEqualTo(59);
    var requestBuilder = delete(RESOURCE_URL + "/" + existed.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions.andExpect(status().isNoContent());
    assertThat(resourceTestService.existsById(existed.getId())).isFalse();
    assertThat(resourceTestService.countResources()).isEqualTo(56);
    assertThat(resourceTestService.findEdgeById(existed.getOutgoingEdges().iterator().next().getId())).isNotPresent();
    assertThat(resourceTestService.countEdges()).isEqualTo(30);
    checkSearchIndexMessage(existed.getId(), DELETE);
  }

  @Test
  void updateResource_shouldNot_deleteExistedResource_createNewResource_sendRelevantKafkaMessages_whenUpdateFailed()
    throws Exception {
    //given
    var existedResource = resourceTestService.saveGraph(getSampleInstanceResource(100L));
    var requestBuilder = put(RESOURCE_URL + "/" + existedResource.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content("{\"resource\": {\"id\": null}}");

    //when
    mockMvc.perform(requestBuilder);

    //then
    assertTrue(resourceTestService.existsById(existedResource.getId()));
    checkRelevantIndexMessagesDuringUpdate(existedResource);
  }

  @Test
  void getResourceViewById_shouldReturnInstance() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleInstanceResource());
    var requestBuilder = get(RESOURCE_URL + "/" + existed.getId() + "/marc")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    resultActions
      .andExpect(jsonPath("id", equalTo(existed.getId().toString())))
      .andExpect(jsonPath("recordType", equalTo("MARC_BIB")))
      .andExpect(jsonPath("parsedRecord.content", notNullValue()));
  }

  protected void checkSearchIndexMessage(Long id, ResourceIndexEventType eventType) {
    // nothing to check without Folio profile
  }

  protected void checkInventoryMessage(Long id, EventTypeEnum eventType) {
    // nothing to check without Folio profile
  }

  protected void checkIndexDate(String id) {
    // nothing to check without Folio profile
  }

  protected void checkRelevantIndexMessagesDuringUpdate(Resource existedResource) {
    // nothing to check without Folio profile
  }

  private void setExistingResourcesIds(Resource resource) {
    resource.setId(hashService.hash(resource));
    resource.getOutgoingEdges()
      .stream()
      .map(ResourceEdge::getTarget)
      .forEach(this::setExistingResourcesIds);
  }

  private void validateInstanceResponse(ResultActions resultActions, String instanceBase) throws Exception {
    resultActions
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(instanceBase, notNullValue()))
      .andExpect(jsonPath(toId(instanceBase), notNullValue()))
      .andExpect(jsonPath(toCarrierCode(instanceBase), equalTo("ha")))
      .andExpect(jsonPath(toCarrierLink(instanceBase), equalTo("http://id.loc.gov/vocabulary/carriers/ha")))
      .andExpect(jsonPath(toCarrierTerm(instanceBase), equalTo("carrier term")))
      .andExpect(jsonPath(toPrimaryTitlePartName(instanceBase), equalTo(List.of("Primary: partName"))))
      .andExpect(jsonPath(toPrimaryTitlePartNumber(instanceBase), equalTo(List.of("Primary: partNumber"))))
      .andExpect(jsonPath(toPrimaryTitleMain(instanceBase), equalTo(List.of("Primary: mainTitle"))))
      .andExpect(jsonPath(toPrimaryTitleNonSortNum(instanceBase), equalTo(List.of("Primary: nonSortNum"))))
      .andExpect(jsonPath(toPrimaryTitleSubtitle(instanceBase), equalTo(List.of("Primary: subTitle"))))
      .andExpect(jsonPath(toParallelTitlePartName(instanceBase), equalTo(List.of("Parallel: partName"))))
      .andExpect(jsonPath(toParallelTitlePartNumber(instanceBase), equalTo(List.of("Parallel: partNumber"))))
      .andExpect(jsonPath(toParallelTitleMain(instanceBase), equalTo(List.of("Parallel: mainTitle"))))
      .andExpect(jsonPath(toParallelTitleNote(instanceBase), equalTo(List.of("Parallel: noteLabel"))))
      .andExpect(jsonPath(toParallelTitleDate(instanceBase), equalTo(List.of("Parallel: date"))))
      .andExpect(jsonPath(toParallelTitleSubtitle(instanceBase), equalTo(List.of("Parallel: subTitle"))))
      .andExpect(jsonPath(toVariantTitlePartName(instanceBase), equalTo(List.of("Variant: partName"))))
      .andExpect(jsonPath(toVariantTitlePartNumber(instanceBase), equalTo(List.of("Variant: partNumber"))))
      .andExpect(jsonPath(toVariantTitleMain(instanceBase), equalTo(List.of("Variant: mainTitle"))))
      .andExpect(jsonPath(toVariantTitleNote(instanceBase), equalTo(List.of("Variant: noteLabel"))))
      .andExpect(jsonPath(toVariantTitleDate(instanceBase), equalTo(List.of("Variant: date"))))
      .andExpect(jsonPath(toVariantTitleSubtitle(instanceBase), equalTo(List.of("Variant: subTitle"))))
      .andExpect(jsonPath(toVariantTitleType(instanceBase), equalTo(List.of("Variant: variantType"))));
    if (instanceBase.equals(toInstance())) {
      resultActions
        .andExpect(jsonPath(toSupplementaryContentLink(), equalTo("supplementaryContent link")))
        .andExpect(jsonPath(toSupplementaryContentName(), equalTo("supplementaryContent name")))
        .andExpect(jsonPath(toAccessLocationLink(), equalTo("accessLocation value")))
        .andExpect(jsonPath(toAccessLocationNote(), equalTo("accessLocation note")))
        .andExpect(jsonPath(toCopyrightDate(), equalTo("copyright date value")))
        .andExpect(jsonPath(toExtent(), equalTo("extent info")))
        .andExpect(jsonPath(toDimensions(), equalTo("20 cm")))
        .andExpect(jsonPath(toEanValue(), equalTo(List.of("ean value"))))
        .andExpect(jsonPath(toEanQualifier(), equalTo(List.of("ean qualifier"))))
        .andExpect(jsonPath(toEditionStatement(), equalTo("edition statement")))
        .andExpect(jsonPath(toIsbnValue(), equalTo(List.of("isbn value"))))
        .andExpect(jsonPath(toIsbnQualifier(), equalTo(List.of("isbn qualifier"))))
        .andExpect(jsonPath(toIsbnStatusValue(), equalTo(List.of("isbn status value"))))
        .andExpect(jsonPath(toIsbnStatusLink(), equalTo(List.of("isbn status link"))))
        .andExpect(jsonPath(toIssuance(), equalTo("single unit")))
        .andExpect(jsonPath(toStatementOfResponsibility(), equalTo("statement of responsibility")))
        .andExpect(
          jsonPath(toInstanceNotesValues(), containsInAnyOrder("additional physical form", "computer data note",
            "description source note", "exhibitions note", "funding information", "issuance note", "issuing body",
            "location of other archival material", "note", "original version note", "related parts",
            "reproduction note",
            "type of report", "with note")))
        .andExpect(jsonPath(toInstanceNotesTypes(), containsInAnyOrder("http://bibfra.me/vocab/lite/note",
          "http://bibfra.me/vocab/marc/withNote", "http://bibfra.me/vocab/marc/typeOfReport",
          "http://bibfra.me/vocab/marc/issuanceNote", "http://bibfra.me/vocab/marc/computerDataNote",
          "http://bibfra.me/vocab/marc/additionalPhysicalForm", "http://bibfra.me/vocab/marc/reproductionNote",
          "http://bibfra.me/vocab/marc/originalVersionNote", "http://bibfra.me/vocab/marc/relatedParts",
          "http://bibfra.me/vocab/marc/issuingBody", "http://bibfra.me/vocab/marc/locationOfOtherArchivalMaterial",
          "http://bibfra.me/vocab/marc/exhibitionsNote", "http://bibfra.me/vocab/marc/descriptionSourceNote",
          "http://bibfra.me/vocab/marc/fundingInformation")))
        .andExpect(jsonPath(toLccnValue(), equalTo(List.of("lccn value"))))
        .andExpect(jsonPath(toLccnStatusValue(), equalTo(List.of("lccn status value"))))
        .andExpect(jsonPath(toLccnStatusLink(), equalTo(List.of("lccn status link"))))
        .andExpect(jsonPath(toLocalIdValue(), equalTo(List.of("localId value"))))
        .andExpect(jsonPath(toLocalIdAssigner(), equalTo(List.of("localId assigner"))))
        .andExpect(jsonPath(toMediaCode(), equalTo("s")))
        .andExpect(jsonPath(toMediaLink(), equalTo("http://id.loc.gov/vocabulary/mediaTypes/s")))
        .andExpect(jsonPath(toMediaTerm(), equalTo("media term")))
        .andExpect(jsonPath(toOtherIdValue(), equalTo(List.of("otherId value"))))
        .andExpect(jsonPath(toOtherIdQualifier(), equalTo(List.of("otherId qualifier"))))
        .andExpect(jsonPath(toProviderEventDate(PE_PRODUCTION), equalTo("production date")))
        .andExpect(jsonPath(toProviderEventName(PE_PRODUCTION), equalTo("production name")))
        .andExpect(jsonPath(toProviderEventPlaceCode(PE_PRODUCTION), equalTo("af")))
        .andExpect(jsonPath(toProviderEventPlaceLabel(PE_PRODUCTION), equalTo("Afghanistan")))
        .andExpect(
          jsonPath(toProviderEventPlaceLink(PE_PRODUCTION), equalTo("http://id.loc.gov/vocabulary/countries/af")))
        .andExpect(jsonPath(toProviderEventProviderDate(PE_PRODUCTION), equalTo("production provider date")))
        .andExpect(jsonPath(toProviderEventSimplePlace(PE_PRODUCTION), equalTo("production simple place")))
        .andExpect(jsonPath(toProviderEventDate(PE_PUBLICATION), equalTo("publication date")))
        .andExpect(jsonPath(toProviderEventName(PE_PUBLICATION), equalTo("publication name")))
        .andExpect(jsonPath(toProviderEventPlaceCode(PE_PUBLICATION), equalTo("al")))
        .andExpect(jsonPath(toProviderEventPlaceLabel(PE_PUBLICATION), equalTo("Albania")))
        .andExpect(
          jsonPath(toProviderEventPlaceLink(PE_PUBLICATION), equalTo("http://id.loc.gov/vocabulary/countries/al")))
        .andExpect(jsonPath(toProviderEventProviderDate(PE_PUBLICATION), equalTo("publication provider date")))
        .andExpect(jsonPath(toProviderEventSimplePlace(PE_PUBLICATION), equalTo("publication simple place")))
        .andExpect(jsonPath(toProviderEventDate(PE_DISTRIBUTION), equalTo("distribution date")))
        .andExpect(jsonPath(toProviderEventName(PE_DISTRIBUTION), equalTo("distribution name")))
        .andExpect(jsonPath(toProviderEventPlaceCode(PE_DISTRIBUTION), equalTo("dz")))
        .andExpect(jsonPath(toProviderEventPlaceLabel(PE_DISTRIBUTION), equalTo("Algeria")))
        .andExpect(
          jsonPath(toProviderEventPlaceLink(PE_DISTRIBUTION), equalTo("http://id.loc.gov/vocabulary/countries/dz")))
        .andExpect(jsonPath(toProviderEventProviderDate(PE_DISTRIBUTION), equalTo("distribution provider date")))
        .andExpect(jsonPath(toProviderEventSimplePlace(PE_DISTRIBUTION), equalTo("distribution simple place")))
        .andExpect(jsonPath(toProviderEventDate(PE_MANUFACTURE), equalTo("manufacture date")))
        .andExpect(jsonPath(toProviderEventName(PE_MANUFACTURE), equalTo("manufacture name")))
        .andExpect(jsonPath(toProviderEventPlaceCode(PE_MANUFACTURE), equalTo("as")))
        .andExpect(jsonPath(toProviderEventPlaceLabel(PE_MANUFACTURE), equalTo("American Samoa")))
        .andExpect(
          jsonPath(toProviderEventPlaceLink(PE_MANUFACTURE), equalTo("http://id.loc.gov/vocabulary/countries/as")))
        .andExpect(jsonPath(toProviderEventProviderDate(PE_MANUFACTURE), equalTo("manufacture provider date")))
        .andExpect(jsonPath(toProviderEventSimplePlace(PE_MANUFACTURE), equalTo("manufacture simple place")))
        .andExpect(jsonPath(toProjectedProvisionDate(), equalTo("projected provision date")))
        .andExpect(jsonPath(toWorkReference(), notNullValue()));
      validateWorkResponse(resultActions, toWorkReference());
    }
  }

  private void validateWorkResponse(ResultActions resultActions, String workBase) throws Exception {
    resultActions
      .andExpect(jsonPath(toId(workBase), notNullValue()))
      .andExpect(jsonPath(toPrimaryTitlePartName(workBase), equalTo(List.of("Primary: partName"))))
      .andExpect(jsonPath(toPrimaryTitlePartNumber(workBase), equalTo(List.of("Primary: partNumber"))))
      .andExpect(jsonPath(toPrimaryTitleMain(workBase), equalTo(List.of("Primary: mainTitle"))))
      .andExpect(jsonPath(toPrimaryTitleNonSortNum(workBase), equalTo(List.of("Primary: nonSortNum"))))
      .andExpect(jsonPath(toPrimaryTitleSubtitle(workBase), equalTo(List.of("Primary: subTitle"))))
      .andExpect(jsonPath(toLanguageCode(workBase), equalTo("eng")))
      .andExpect(jsonPath(toLanguageTerm(workBase), equalTo("English")))
      .andExpect(jsonPath(toLanguageLink(workBase), equalTo("http://id.loc.gov/vocabulary/languages/eng")))
      .andExpect(jsonPath(toClassificationCodes(workBase), containsInAnyOrder("ddc code", "lc code")))
      .andExpect(jsonPath(toClassificationSources(workBase), containsInAnyOrder("ddc", "lc")))
      .andExpect(jsonPath(toClassificationItemNumbers(workBase), containsInAnyOrder("ddc item number",
        "lc item number")))
      .andExpect(jsonPath(toWorkDeweyEditionNumber(workBase), equalTo(List.of("edition number"))))
      .andExpect(jsonPath(toWorkDeweyEdition(workBase), equalTo(List.of("edition"))))
      .andExpect(jsonPath(toLcStatusValue(workBase), equalTo(List.of("lc status value"))))
      .andExpect(jsonPath(toLcStatusLink(workBase), equalTo(List.of("lc status link"))))
      .andExpect(jsonPath(toClassificationAssigningSourceIds(workBase), containsInAnyOrder("4932783899755316479",
        "8752404686183471966")))
      .andExpect(jsonPath(toClassificationAssigningSourceLabels(workBase), containsInAnyOrder("assigning agency",
        "United States, Library of Congress")))
      .andExpect(jsonPath(toDissertationLabel(workBase), equalTo("label")))
      .andExpect(jsonPath(toDissertationDegree(workBase), equalTo("degree")))
      .andExpect(jsonPath(toDissertationYear(workBase), equalTo("dissertation year")))
      .andExpect(jsonPath(toDissertationNote(workBase), equalTo("dissertation note")))
      .andExpect(jsonPath(toDissertationId(workBase), equalTo("dissertation id")))
      .andExpect(jsonPath(toDissertationGrantingInstitutionIds(workBase), containsInAnyOrder("5481852630377445080",
        "-6468470931408362304")))
      .andExpect(jsonPath(toDissertationGrantingInstitutionLabels(workBase),
        containsInAnyOrder("granting institution 1", "granting institution 2")))
      .andExpect(jsonPath(toWorkCreatorId(workBase), containsInAnyOrder("-603031702996824854", "4359679744172518150",
        "-466724080127664871", "8296435493593701280")))
      .andExpect(jsonPath(toWorkCreatorLabel(workBase), containsInAnyOrder("name-CREATOR-MEETING",
        "name-CREATOR-PERSON", "name-CREATOR-ORGANIZATION", "name-CREATOR-FAMILY")))
      .andExpect(jsonPath(toWorkCreatorType(workBase), containsInAnyOrder(MEETING.getUri(), PERSON.getUri(),
        ORGANIZATION.getUri(), FAMILY.getUri())))
      .andExpect(jsonPath(toWorkCreatorRoles(workBase), equalTo(List.of(List.of(AUTHOR.getUri())))))
      .andExpect(jsonPath(toWorkContributorId(workBase), containsInAnyOrder("-6054989039809126250",
        "-7286109411186266518", "-4246830624125472784", "3094995075578514480")))
      .andExpect(jsonPath(toWorkContributorLabel(workBase), containsInAnyOrder("name-CONTRIBUTOR-ORGANIZATION",
        "name-CONTRIBUTOR-FAMILY", "name-CONTRIBUTOR-PERSON", "name-CONTRIBUTOR-MEETING")))
      .andExpect(jsonPath(toWorkContributorType(workBase), containsInAnyOrder(ORGANIZATION.getUri(), FAMILY.getUri(),
        PERSON.getUri(), MEETING.getUri())))
      .andExpect(jsonPath(toWorkContributorRoles(workBase), equalTo(List.of(List.of(EDITOR.getUri(),
        ASSIGNEE.getUri())))))
      .andExpect(jsonPath(toWorkContentLink(workBase), equalTo("http://id.loc.gov/vocabulary/contentTypes/txt")))
      .andExpect(jsonPath(toWorkContentCode(workBase), equalTo("txt")))
      .andExpect(jsonPath(toWorkContentTerm(workBase), equalTo("text")))
      .andExpect(jsonPath(toWorkSubjectLabel(workBase), equalTo(List.of("subject 1", "subject 2"))))
      .andExpect(jsonPath(toWorkSummary(workBase), equalTo("summary text")))
      .andExpect(jsonPath(toWorkTableOfContents(workBase), equalTo("table of contents")))
      .andExpect(jsonPath(toWorkNotesValues(workBase),
        containsInAnyOrder("language note", "bibliography note", "note", "another note", "another note")))
      .andExpect(jsonPath(toWorkNotesTypes(workBase), containsInAnyOrder("http://bibfra.me/vocab/marc/languageNote",
        "http://bibfra.me/vocab/marc/languageNote", "http://bibfra.me/vocab/marc/bibliographyNote",
        "http://bibfra.me/vocab/lite/note", "http://bibfra.me/vocab/lite/note")))
      .andExpect(jsonPath(toWorkGeographicCoverageLabel(workBase), containsInAnyOrder("United States", "Europe")))
      .andExpect(jsonPath(toWorkGenreLabel(workBase), equalTo(List.of("genre 1", "genre 2"))))
      .andExpect(jsonPath(toWorkDateStart(workBase), equalTo("2024")))
      .andExpect(jsonPath(toWorkDateEnd(workBase), equalTo("2025")))
      .andExpect(jsonPath(toWorkGovPublicationCode(workBase), equalTo("a")))
      .andExpect(jsonPath(toWorkGovPublicationTerm(workBase), equalTo("Autonomous")))
      .andExpect(jsonPath(toWorkGovPublicationLink(workBase), equalTo("http://id.loc.gov/vocabulary/mgovtpubtype/a")))
      .andExpect(jsonPath(toWorkTargetAudienceCode(workBase), equalTo("b")))
      .andExpect(jsonPath(toWorkTargetAudienceTerm(workBase), equalTo("Primary")))
      .andExpect(jsonPath(toWorkTargetAudienceLink(workBase), equalTo("http://id.loc.gov/vocabulary/maudience/pri")));
    if (workBase.equals(toWork())) {
      resultActions.andExpect(jsonPath(toInstanceReference(workBase), notNullValue()));
      validateInstanceResponse(resultActions, toInstanceReference(workBase));
    }
  }

  private void validateInstance(Resource instance, boolean validateFullWork) {
    assertThat(instance.getId()).isEqualTo(hashService.hash(instance));
    assertThat(instance.getLabel()).isEqualTo("Primary: mainTitle");
    assertThat(instance.getTypes().iterator().next().getUri()).isEqualTo(INSTANCE.getUri());
    assertThat(instance.getDoc().size()).isEqualTo(20);
    validateLiteral(instance, DIMENSIONS.getValue(), "20 cm");
    validateLiteral(instance, EDITION.getValue(), "edition statement");
    validateLiteral(instance, PROJECTED_PROVISION_DATE.getValue(), "projected provision date");
    validateLiteral(instance, ISSUANCE.getValue(), "single unit");
    validateLiteral(instance, STATEMENT_OF_RESPONSIBILITY.getValue(), "statement of responsibility");
    validateLiteral(instance, ADDITIONAL_PHYSICAL_FORM.getValue(), "additional physical form");
    validateLiteral(instance, COMPUTER_DATA_NOTE.getValue(), "computer data note");
    validateLiteral(instance, DESCRIPTION_SOURCE_NOTE.getValue(), "description source note");
    validateLiteral(instance, EXHIBITIONS_NOTE.getValue(), "exhibitions note");
    validateLiteral(instance, FUNDING_INFORMATION.getValue(), "funding information");
    validateLiteral(instance, ISSUANCE_NOTE.getValue(), "issuance note");
    validateLiteral(instance, ISSUING_BODY.getValue(), "issuing body");
    validateLiteral(instance, LOCATION_OF_OTHER_ARCHIVAL_MATERIAL.getValue(), "location of other archival material");
    validateLiteral(instance, NOTE.getValue(), "note");
    validateLiteral(instance, ORIGINAL_VERSION_NOTE.getValue(), "original version note");
    validateLiteral(instance, RELATED_PARTS.getValue(), "related parts");
    validateLiteral(instance, REPRODUCTION_NOTE.getValue(), "reproduction note");
    validateLiteral(instance, TYPE_OF_REPORT.getValue(), "type of report");
    validateLiteral(instance, WITH_NOTE.getValue(), "with note");
    assertThat(instance.getOutgoingEdges()).hasSize(18);

    var edgeIterator = instance.getOutgoingEdges().iterator();
    validateParallelTitle(edgeIterator.next(), instance);
    validateCategory(edgeIterator.next(), instance, CARRIER, "http://id.loc.gov/vocabulary/carriers/ha", "ha");
    validateCategory(edgeIterator.next(), instance, MEDIA, "http://id.loc.gov/vocabulary/mediaTypes/s", "s");
    validateLccn(edgeIterator.next(), instance);
    var edge = edgeIterator.next();
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(instance);
    assertThat(edge.getPredicate().getUri()).isEqualTo(INSTANTIATES.getUri());
    var work = edge.getTarget();
    if (validateFullWork) {
      validateWork(work, false);
    }
    validateAccessLocation(edgeIterator.next(), instance);
    validateProviderEvent(edgeIterator.next(), instance, PE_MANUFACTURE, "as", "American Samoa");
    validateProviderEvent(edgeIterator.next(), instance, PE_DISTRIBUTION, "dz", "Algeria");
    validateProviderEvent(edgeIterator.next(), instance, PE_PRODUCTION, "af", "Afghanistan");
    validateProviderEvent(edgeIterator.next(), instance, PE_PUBLICATION, "al", "Albania");
    validateOtherId(edgeIterator.next(), instance);
    validateEan(edgeIterator.next(), instance);
    validatePrimaryTitle(edgeIterator.next(), instance);
    validateSupplementaryContent(edgeIterator.next(), instance);
    validateIsbn(edgeIterator.next(), instance);
    validateLocalId(edgeIterator.next(), instance);
    validateVariantTitle(edgeIterator.next(), instance);
    validateCopyrightDate(edgeIterator.next(), instance);
    assertThat(edgeIterator.hasNext()).isFalse();
  }

  private void validateLiteral(Resource resource, String field, String value) {
    assertThat(resource.getDoc().get(field).size()).isEqualTo(1);
    assertThat(resource.getDoc().get(field).get(0).asText()).isEqualTo(value);
  }

  private void validateLiterals(Resource resource, String field, List<String> expectedValues) {
    var actualValues = resource.getDoc().get(field);
    assertThat(actualValues.size()).isEqualTo(expectedValues.size());
    assertThat(stream(spliteratorUnknownSize(actualValues.iterator(), ORDERED), false).map(JsonNode::asText).toList())
      .hasSameElementsAs(expectedValues);
  }

  private void validatePrimaryTitle(ResourceEdge edge, Resource source) {
    validateSampleTitleBase(edge, source, ResourceTypeDictionary.TITLE, "Primary: ");
    var title = edge.getTarget();
    assertThat(title.getId()).isEqualTo(hashService.hash(title));
    assertThat(title.getDoc().size()).isEqualTo(5);
    assertThat(title.getDoc().get(NON_SORT_NUM.getValue()).size()).isEqualTo(1);
    assertThat(title.getDoc().get(NON_SORT_NUM.getValue()).get(0).asText()).isEqualTo("Primary: nonSortNum");
    assertThat(title.getOutgoingEdges()).isEmpty();
  }

  private void validateParallelTitle(ResourceEdge edge, Resource source) {
    validateSampleTitleBase(edge, source, PARALLEL_TITLE, "Parallel: ");
    var title = edge.getTarget();
    assertThat(title.getId()).isEqualTo(hashService.hash(title));
    assertThat(title.getDoc().size()).isEqualTo(6);
    assertThat(title.getDoc().get(DATE.getValue()).size()).isEqualTo(1);
    assertThat(title.getDoc().get(DATE.getValue()).get(0).asText()).isEqualTo("Parallel: date");
    assertThat(title.getDoc().get(NOTE.getValue()).size()).isEqualTo(1);
    assertThat(title.getDoc().get(NOTE.getValue()).get(0).asText()).isEqualTo("Parallel: noteLabel");
    assertThat(title.getOutgoingEdges()).isEmpty();
  }

  private void validateVariantTitle(ResourceEdge edge, Resource source) {
    validateSampleTitleBase(edge, source, VARIANT_TITLE, "Variant: ");
    var title = edge.getTarget();
    assertThat(title.getId()).isEqualTo(hashService.hash(title));
    assertThat(title.getDoc().size()).isEqualTo(7);
    assertThat(title.getDoc().get(DATE.getValue()).size()).isEqualTo(1);
    assertThat(title.getDoc().get(DATE.getValue()).get(0).asText()).isEqualTo("Variant: date");
    assertThat(title.getDoc().get(VARIANT_TYPE.getValue()).size()).isEqualTo(1);
    assertThat(title.getDoc().get(VARIANT_TYPE.getValue()).get(0).asText()).isEqualTo("Variant: variantType");
    assertThat(title.getDoc().get(NOTE.getValue()).size()).isEqualTo(1);
    assertThat(title.getDoc().get(NOTE.getValue()).get(0).asText()).isEqualTo("Variant: noteLabel");
    assertThat(title.getOutgoingEdges()).isEmpty();
  }

  private void validateSampleTitleBase(ResourceEdge edge, Resource source, ResourceTypeDictionary type, String prefix) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(TITLE.getUri());
    var title = edge.getTarget();
    assertThat(title.getLabel()).isEqualTo(prefix + "mainTitle");
    assertThat(title.getTypes().iterator().next().getUri()).isEqualTo(type.getUri());
    assertThat(title.getId()).isEqualTo(hashService.hash(title));
    assertThat(title.getDoc().get(PART_NAME.getValue()).size()).isEqualTo(1);
    assertThat(title.getDoc().get(PART_NAME.getValue()).get(0).asText()).isEqualTo(prefix + "partName");
    assertThat(title.getDoc().get(PART_NUMBER.getValue()).size()).isEqualTo(1);
    assertThat(title.getDoc().get(PART_NUMBER.getValue()).get(0).asText()).isEqualTo(prefix + "partNumber");
    assertThat(title.getDoc().get(MAIN_TITLE.getValue()).size()).isEqualTo(1);
    assertThat(title.getDoc().get(MAIN_TITLE.getValue()).get(0).asText()).isEqualTo(prefix + "mainTitle");
    assertThat(title.getDoc().get(SUBTITLE.getValue()).size()).isEqualTo(1);
    assertThat(title.getDoc().get(SUBTITLE.getValue()).get(0).asText()).isEqualTo(prefix + "subTitle");
  }

  private void validateProviderEvent(ResourceEdge edge, Resource source, PredicateDictionary predicate,
                                     String expectedCode, String expectedLabel) {
    var type = predicate.getUri().substring(predicate.getUri().indexOf("marc/") + 5);
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(predicate.getUri());
    var providerEvent = edge.getTarget();
    assertThat(providerEvent.getLabel()).isEqualTo(type + " name");
    assertThat(providerEvent.getTypes().iterator().next().getUri()).isEqualTo(PROVIDER_EVENT.getUri());
    assertThat(providerEvent.getId()).isEqualTo(hashService.hash(providerEvent));
    assertThat(providerEvent.getDoc().size()).isEqualTo(4);
    assertThat(providerEvent.getDoc().get(DATE.getValue()).size()).isEqualTo(1);
    assertThat(providerEvent.getDoc().get(DATE.getValue()).get(0).asText()).isEqualTo(type + " date");
    assertThat(providerEvent.getDoc().get(NAME.getValue()).size()).isEqualTo(1);
    assertThat(providerEvent.getDoc().get(NAME.getValue()).get(0).asText()).isEqualTo(type + " name");
    assertThat(providerEvent.getDoc().get(PROVIDER_DATE.getValue()).size()).isEqualTo(1);
    assertThat(providerEvent.getDoc().get(PROVIDER_DATE.getValue()).get(0).asText()).isEqualTo(type + " provider date");
    assertThat(providerEvent.getDoc().get(SIMPLE_PLACE.getValue()).size()).isEqualTo(1);
    assertThat(providerEvent.getDoc().get(SIMPLE_PLACE.getValue()).get(0).asText()).isEqualTo(type + " simple place");
    assertThat(providerEvent.getOutgoingEdges()).hasSize(1);
    validateProviderPlace(providerEvent.getOutgoingEdges().iterator().next(), providerEvent, expectedCode,
      expectedLabel);
  }

  private void validateProviderPlace(ResourceEdge edge, Resource source, String expectedCode, String expectedLabel) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(PROVIDER_PLACE.getUri());
    var place = edge.getTarget();
    assertThat(place.getLabel()).isEqualTo(expectedLabel);
    assertThat(place.getTypes().iterator().next().getUri()).isEqualTo(PLACE.getUri());
    assertThat(place.getId()).isEqualTo(hashService.hash(place));
    assertThat(place.getDoc().size()).isEqualTo(3);
    assertThat(place.getDoc().get(CODE.getValue()).size()).isEqualTo(1);
    assertThat(place.getDoc().get(CODE.getValue()).get(0).asText()).isEqualTo(expectedCode);
    assertThat(place.getDoc().get(LABEL.getValue()).size()).isEqualTo(1);
    assertThat(place.getDoc().get(LABEL.getValue()).get(0).asText()).isEqualTo(expectedLabel);
    assertThat(place.getDoc().get(LINK.getValue()).size()).isEqualTo(1);
    assertThat(place.getDoc().get(LINK.getValue()).get(0).asText()).isEqualTo(
      "http://id.loc.gov/vocabulary/countries/" + expectedCode);
    assertThat(place.getOutgoingEdges()).isEmpty();
  }

  private void validateLccn(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(MAP.getUri());
    var lccn = edge.getTarget();
    assertThat(lccn.getLabel()).isEqualTo("lccn value");
    var typesIterator = lccn.getTypes().iterator();
    assertThat(typesIterator.next().getUri()).isEqualTo(ID_LCCN.getUri());
    assertThat(typesIterator.next().getUri()).isEqualTo(IDENTIFIER.getUri());
    assertThat(lccn.getId()).isEqualTo(hashService.hash(lccn));
    assertThat(lccn.getDoc().size()).isEqualTo(1);
    assertThat(lccn.getDoc().get(NAME.getValue()).size()).isEqualTo(1);
    assertThat(lccn.getDoc().get(NAME.getValue()).get(0).asText()).isEqualTo("lccn value");
    assertThat(lccn.getOutgoingEdges()).hasSize(1);
    validateStatus(lccn.getOutgoingEdges().iterator().next(), lccn, "lccn");
  }

  private void validateIsbn(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(MAP.getUri());
    var isbn = edge.getTarget();
    assertThat(isbn.getLabel()).isEqualTo("isbn value");
    var typesIterator = isbn.getTypes().iterator();
    assertThat(typesIterator.next().getUri()).isEqualTo(ID_ISBN.getUri());
    assertThat(typesIterator.next().getUri()).isEqualTo(IDENTIFIER.getUri());
    assertThat(typesIterator.hasNext()).isFalse();
    assertThat(isbn.getId()).isEqualTo(hashService.hash(isbn));
    assertThat(isbn.getDoc().size()).isEqualTo(2);
    assertThat(isbn.getDoc().get(NAME.getValue()).size()).isEqualTo(1);
    assertThat(isbn.getDoc().get(NAME.getValue()).get(0).asText()).isEqualTo("isbn value");
    assertThat(isbn.getDoc().get(QUALIFIER.getValue()).size()).isEqualTo(1);
    assertThat(isbn.getDoc().get(QUALIFIER.getValue()).get(0).asText()).isEqualTo("isbn qualifier");
    assertThat(isbn.getOutgoingEdges()).hasSize(1);
    validateStatus(isbn.getOutgoingEdges().iterator().next(), isbn, "isbn");
  }

  private void validateEan(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(MAP.getUri());
    var ean = edge.getTarget();
    assertThat(ean.getLabel()).isEqualTo("ean value");
    var typesIterator = ean.getTypes().iterator();
    assertThat(typesIterator.next().getUri()).isEqualTo(ID_EAN.getUri());
    assertThat(typesIterator.next().getUri()).isEqualTo(IDENTIFIER.getUri());
    assertThat(typesIterator.hasNext()).isFalse();
    assertThat(ean.getId()).isEqualTo(hashService.hash(ean));
    assertThat(ean.getDoc().size()).isEqualTo(2);
    assertThat(ean.getDoc().get(EAN_VALUE.getValue()).size()).isEqualTo(1);
    assertThat(ean.getDoc().get(EAN_VALUE.getValue()).get(0).asText()).isEqualTo("ean value");
    assertThat(ean.getDoc().get(QUALIFIER.getValue()).size()).isEqualTo(1);
    assertThat(ean.getDoc().get(QUALIFIER.getValue()).get(0).asText()).isEqualTo("ean qualifier");
    assertThat(ean.getOutgoingEdges()).isEmpty();
  }

  private void validateLocalId(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(MAP.getUri());
    var localId = edge.getTarget();
    assertThat(localId.getLabel()).isEqualTo("localId value");
    var typesIterator = localId.getTypes().iterator();
    assertThat(typesIterator.next().getUri()).isEqualTo(ID_LOCAL.getUri());
    assertThat(typesIterator.next().getUri()).isEqualTo(IDENTIFIER.getUri());
    assertThat(typesIterator.hasNext()).isFalse();
    assertThat(localId.getId()).isEqualTo(hashService.hash(localId));
    assertThat(localId.getDoc().size()).isEqualTo(2);
    assertThat(localId.getDoc().get(LOCAL_ID_VALUE.getValue()).size()).isEqualTo(1);
    assertThat(localId.getDoc().get(LOCAL_ID_VALUE.getValue()).get(0).asText()).isEqualTo("localId value");
    assertThat(localId.getDoc().get(ASSIGNING_SOURCE.getValue()).size()).isEqualTo(1);
    assertThat(localId.getDoc().get(ASSIGNING_SOURCE.getValue()).get(0).asText()).isEqualTo("localId assigner");
    assertThat(localId.getOutgoingEdges()).isEmpty();
  }

  private void validateOtherId(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(MAP.getUri());
    var otherId = edge.getTarget();
    assertThat(otherId.getLabel()).isEqualTo("otherId value");
    var typesIterator = otherId.getTypes().iterator();
    assertThat(typesIterator.next().getUri()).isEqualTo(ID_UNKNOWN.getUri());
    assertThat(typesIterator.next().getUri()).isEqualTo(IDENTIFIER.getUri());
    assertThat(typesIterator.hasNext()).isFalse();
    assertThat(otherId.getId()).isEqualTo(hashService.hash(otherId));
    assertThat(otherId.getDoc().size()).isEqualTo(2);
    assertThat(otherId.getDoc().get(NAME.getValue()).size()).isEqualTo(1);
    assertThat(otherId.getDoc().get(NAME.getValue()).get(0).asText()).isEqualTo("otherId value");
    assertThat(otherId.getDoc().get(QUALIFIER.getValue()).size()).isEqualTo(1);
    assertThat(otherId.getDoc().get(QUALIFIER.getValue()).get(0).asText()).isEqualTo("otherId qualifier");
    assertThat(otherId.getOutgoingEdges()).isEmpty();
  }

  private void validateStatus(ResourceEdge edge, Resource source, String prefix) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(STATUS.getUri());
    var status = edge.getTarget();
    assertThat(status.getLabel()).isEqualTo(prefix + " status value");
    assertThat(status.getTypes().iterator().next().getUri()).isEqualTo(ResourceTypeDictionary.STATUS.getUri());
    assertThat(status.getId()).isEqualTo(hashService.hash(status));
    assertThat(status.getDoc().size()).isEqualTo(2);
    assertThat(status.getDoc().get(LINK.getValue()).size()).isEqualTo(1);
    assertThat(status.getDoc().get(LINK.getValue()).get(0).asText()).isEqualTo(prefix + " status link");
    assertThat(status.getDoc().get(LABEL.getValue()).size()).isEqualTo(1);
    assertThat(status.getDoc().get(LABEL.getValue()).get(0).asText()).isEqualTo(prefix + " status value");
    assertThat(status.getOutgoingEdges()).isEmpty();
  }

  private void validateSupplementaryContent(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(SUPPLEMENTARY_CONTENT.getUri());

    var supplementaryContent = edge.getTarget();

    assertThat(supplementaryContent.getLabel()).isEqualTo("supplementaryContent name");
    assertThat(supplementaryContent.getTypes().iterator().next().getUri())
      .isEqualTo(ResourceTypeDictionary.SUPPLEMENTARY_CONTENT.getUri());
    assertThat(supplementaryContent.getId()).isEqualTo(hashService.hash(supplementaryContent));

    var doc = supplementaryContent.getDoc();

    assertThat(doc.size()).isEqualTo(2);
    validateLiteral(supplementaryContent, LINK.getValue(), "supplementaryContent link");
    validateLiteral(supplementaryContent, NAME.getValue(), "supplementaryContent name");
    assertThat(supplementaryContent.getOutgoingEdges()).isEmpty();
  }

  private void validateAccessLocation(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(ACCESS_LOCATION.getUri());
    var locator = edge.getTarget();
    assertThat(locator.getLabel()).isEqualTo("accessLocation value");
    assertThat(locator.getTypes().iterator().next().getUri()).isEqualTo(ANNOTATION.getUri());
    assertThat(locator.getId()).isEqualTo(hashService.hash(locator));
    assertThat(locator.getDoc().size()).isEqualTo(2);
    assertThat(locator.getDoc().get(LINK.getValue()).size()).isEqualTo(1);
    assertThat(locator.getDoc().get(LINK.getValue()).get(0).asText()).isEqualTo("accessLocation value");
    assertThat(locator.getDoc().get(NOTE.getValue()).size()).isEqualTo(1);
    assertThat(locator.getDoc().get(NOTE.getValue()).get(0).asText()).isEqualTo("accessLocation note");
    assertThat(locator.getOutgoingEdges()).isEmpty();
  }

  private void validateCategory(ResourceEdge edge, Resource source, PredicateDictionary pred,
                                String expectedLink, String expectedCode) {
    var prefix = pred.getUri().substring(pred.getUri().lastIndexOf("/") + 1);
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(pred.getUri());
    var category = edge.getTarget();
    assertThat(category.getLabel()).isEqualTo(prefix + " term");
    assertThat(category.getTypes().iterator().next().getUri()).isEqualTo(CATEGORY.getUri());
    assertThat(category.getId()).isEqualTo(hashService.hash(category));
    assertThat(category.getDoc().size()).isEqualTo(4);
    validateLiteral(category, CODE.getValue(), expectedCode);
    validateLiteral(category, TERM.getValue(), prefix + " term");
    validateLiteral(category, LINK.getValue(), expectedLink);
    validateLiteral(category, SOURCE.getValue(), prefix + " source");
    if (category.getOutgoingEdges().isEmpty()) {
      return;
    }
    assertThat(category.getOutgoingEdges())
      .extracting(ResourceEdge::getPredicate)
      .extracting(PredicateEntity::getUri)
      .containsOnly(IS_DEFINED_BY.getUri());
    assertThat(category.getOutgoingEdges())
      .extracting(ResourceEdge::getTarget)
      .flatExtracting(Resource::getTypes)
      .extracting(ResourceTypeEntity::getUri)
      .containsOnly(CATEGORY_SET.getUri());
  }

  private void validateWork(Resource work, boolean validateFullInstance) {
    assertThat(work.getId()).isEqualTo(hashService.hash(work));
    assertThat(work.getLabel()).isEqualTo("Primary: mainTitle");
    assertThat(work.getTypes().iterator().next().getUri()).isEqualTo(WORK.getUri());
    assertThat(work.getDoc().size()).isEqualTo(7);
    validateLiterals(work, DATE_START.getValue(), List.of("2024"));
    validateLiterals(work, DATE_END.getValue(), List.of("2025"));
    validateLiteral(work, SUMMARY.getValue(), "summary text");
    validateLiteral(work, TABLE_OF_CONTENTS.getValue(), "table of contents");
    validateLiteral(work, BIBLIOGRAPHY_NOTE.getValue(), "bibliography note");
    validateLiterals(work, LANGUAGE_NOTE.getValue(), List.of("language note", "another note"));
    validateLiterals(work, NOTE.getValue(), List.of("note", "another note"));
    var outgoingEdgeIterator = work.getOutgoingEdges().iterator();
    validateParallelTitle(outgoingEdgeIterator.next(), work);
    validateWorkContentType(outgoingEdgeIterator.next(), work);
    validateWorkTargetAudience(outgoingEdgeIterator.next(), work);
    validateWorkGovernmentPublication(outgoingEdgeIterator.next(), work);
    validateLanguage(outgoingEdgeIterator.next(), work);
    validateDissertation(outgoingEdgeIterator.next(), work);
    validateWorkContributor(outgoingEdgeIterator.next(), work, ORGANIZATION, CREATOR);
    var editorEdge = outgoingEdgeIterator.next();
    validateResourceEdge(editorEdge, work, editorEdge.getTarget(), EDITOR.getUri());
    validateWorkContributor(outgoingEdgeIterator.next(), work, ORGANIZATION, CONTRIBUTOR);
    var assigneeEdge = outgoingEdgeIterator.next();
    validateResourceEdge(assigneeEdge, work, assigneeEdge.getTarget(), ASSIGNEE.getUri());
    validateWorkContributor(outgoingEdgeIterator.next(), work, FAMILY, CREATOR);
    validateWorkContributor(outgoingEdgeIterator.next(), work, FAMILY, CONTRIBUTOR);
    if (!validateFullInstance) {
      outgoingEdgeIterator.next();
      validateDdcClassification(outgoingEdgeIterator.next(), work);
    } else {
      validateLcClassification(outgoingEdgeIterator.next(), work);
      validateDdcClassification(outgoingEdgeIterator.next(), work);
    }
    validatePrimaryTitle(outgoingEdgeIterator.next(), work);
    var authorEdge = outgoingEdgeIterator.next();
    validateResourceEdge(authorEdge, work, authorEdge.getTarget(), AUTHOR.getUri());
    validateWorkContributor(outgoingEdgeIterator.next(), work, PERSON, CREATOR);
    validateWorkContributor(outgoingEdgeIterator.next(), work, PERSON, CONTRIBUTOR);
    validateVariantTitle(outgoingEdgeIterator.next(), work);
    validateResourceEdge(outgoingEdgeIterator.next(), work, lookupResources.subjects().get(0), SUBJECT.getUri());
    validateResourceEdge(outgoingEdgeIterator.next(), work, lookupResources.subjects().get(1), SUBJECT.getUri());
    validateResourceEdge(outgoingEdgeIterator.next(), work, lookupResources.genres().get(0), GENRE.getUri());
    validateResourceEdge(outgoingEdgeIterator.next(), work, lookupResources.genres().get(1), GENRE.getUri());
    validateOriginPlace(outgoingEdgeIterator.next(), work);
    validateResourceEdge(outgoingEdgeIterator.next(), work, lookupResources.geographicCoverages().get(1),
      GEOGRAPHIC_COVERAGE.getUri());
    validateResourceEdge(outgoingEdgeIterator.next(), work, lookupResources.geographicCoverages().get(0),
      GEOGRAPHIC_COVERAGE.getUri());
    validateWorkContributor(outgoingEdgeIterator.next(), work, MEETING, CREATOR);
    validateWorkContributor(outgoingEdgeIterator.next(), work, MEETING, CONTRIBUTOR);
    assertThat(outgoingEdgeIterator.hasNext()).isFalse();
    if (validateFullInstance) {
      var incomingEdgeIterator = work.getIncomingEdges().iterator();
      var edge = incomingEdgeIterator.next();
      assertThat(edge.getId()).isNotNull();
      assertThat(edge.getTarget()).isEqualTo(work);
      assertThat(edge.getPredicate().getUri()).isEqualTo(INSTANTIATES.getUri());
      validateInstance(edge.getSource(), false);
      assertThat(incomingEdgeIterator.hasNext()).isFalse();
    }
  }

  private void validateDdcClassification(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(CLASSIFICATION.getUri());
    var classification = edge.getTarget();
    assertThat(classification.getId()).isEqualTo(hashService.hash(classification));
    var types = classification.getTypes().stream().map(ResourceTypeEntity::getUri).toList();
    assertThat(types).contains(ResourceTypeDictionary.CLASSIFICATION.getUri());
    assertThat(classification.getDoc().size()).isEqualTo(5);
    validateLiteral(classification, CODE.getValue(), "ddc code");
    validateLiteral(classification, SOURCE.getValue(), "ddc");
    validateLiteral(classification, ITEM_NUMBER.getValue(), "ddc item number");
    validateLiteral(classification, EDITION_NUMBER.getValue(), "edition number");
    validateLiteral(classification, EDITION.getValue(), "edition");
    var resourceEdge = classification.getOutgoingEdges().iterator().next();
    var assigningSource = resourceEdge.getTarget();
    validateResourceEdge(resourceEdge, classification, assigningSource, PredicateDictionary.ASSIGNING_SOURCE.getUri());
    validateLiteral(assigningSource, NAME.getValue(), "assigning agency");
    assertThat(assigningSource.getLabel()).isEqualTo("assigning agency");
  }

  private void validateLcClassification(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(CLASSIFICATION.getUri());
    var classification = edge.getTarget();
    assertThat(classification.getId()).isEqualTo(hashService.hash(classification));
    var types = classification.getTypes().stream().map(ResourceTypeEntity::getUri).toList();
    assertThat(types).contains(ResourceTypeDictionary.CLASSIFICATION.getUri());
    assertThat(classification.getDoc().size()).isEqualTo(3);
    validateLiteral(classification, CODE.getValue(), "lc code");
    validateLiteral(classification, SOURCE.getValue(), "lc");
    validateLiteral(classification, ITEM_NUMBER.getValue(), "lc item number");
    var iterator = classification.getOutgoingEdges().iterator();
    validateStatus(iterator.next(), classification, "lc");
    var assigningSourceEdge = iterator.next();
    validateResourceEdge(assigningSourceEdge, classification, assigningSourceEdge.getTarget(),
      PredicateDictionary.ASSIGNING_SOURCE.getUri());
  }

  private void validateWorkContentType(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(CONTENT.getUri());
    var contentType = edge.getTarget();
    assertThat(contentType.getId()).isEqualTo(hashService.hash(contentType));
    assertThat(contentType.getDoc().size()).isEqualTo(4);
    validateLiteral(contentType, CODE.getValue(), "txt");
    validateLiteral(contentType, LINK.getValue(), "http://id.loc.gov/vocabulary/contentTypes/txt");
    validateLiteral(contentType, TERM.getValue(), "text");
    validateLiteral(contentType, SOURCE.getValue(), "content source");
    var resourceEdge = contentType.getOutgoingEdges().iterator().next();
    var categorySet = resourceEdge.getTarget();
    validateResourceEdge(resourceEdge, contentType, categorySet, IS_DEFINED_BY.getUri());
    assertThat(categorySet.getDoc().size()).isEqualTo(2);
    validateLiteral(categorySet, LINK.getValue(), "http://id.loc.gov/vocabulary/genreFormSchemes/rdacontent");
    validateLiteral(categorySet, LABEL.getValue(), "rdacontent");
    assertThat(categorySet.getLabel()).isEqualTo("rdacontent");
  }

  private void validateWorkTargetAudience(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(TARGET_AUDIENCE.getUri());
    var contentType = edge.getTarget();
    assertThat(contentType.getId()).isEqualTo(hashService.hash(contentType));
    assertThat(contentType.getDoc().size()).isEqualTo(3);
    validateLiteral(contentType, CODE.getValue(), "b");
    validateLiteral(contentType, LINK.getValue(), "http://id.loc.gov/vocabulary/maudience/pri");
    validateLiteral(contentType, TERM.getValue(), "Primary");
    var resourceEdge = contentType.getOutgoingEdges().iterator().next();
    var categorySet = resourceEdge.getTarget();
    validateResourceEdge(resourceEdge, contentType, categorySet, IS_DEFINED_BY.getUri());
    assertThat(categorySet.getDoc().size()).isEqualTo(2);
    validateLiteral(categorySet, LINK.getValue(), "http://id.loc.gov/vocabulary/maudience");
    validateLiteral(categorySet, LABEL.getValue(), "Target audience");
    assertThat(categorySet.getLabel()).isEqualTo("Target audience");
  }

  private void validateWorkContributor(ResourceEdge edge, Resource source, ResourceTypeDictionary type,
                                       PredicateDictionary predicate) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(predicate.getUri());
    var creator = edge.getTarget();
    assertThat(creator.getId()).isEqualTo(hashService.hash(creator));
    var types = creator.getTypes().stream().map(ResourceTypeEntity::getUri).toList();
    assertThat(types).contains(type.getUri());
    assertThat(creator.getDoc().size()).isEqualTo(2);
    assertThat(creator.getDoc().get(NAME.getValue()).size()).isEqualTo(1);
    assertThat(creator.getDoc().get(NAME.getValue()).get(0).asText()).isEqualTo("name-" + predicate + "-" + type);
    assertThat(creator.getDoc().get(LCNAF_ID.getValue()).size()).isEqualTo(1);
    assertThat(creator.getDoc().get(LCNAF_ID.getValue()).get(0).asText()).isEqualTo("2002801801-" + type);
    assertThat(creator.getLabel()).isEqualTo("name-" + predicate + "-" + type);
  }

  private void validateWorkGovernmentPublication(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(GOVERNMENT_PUBLICATION.getUri());
    var governmentPublication = edge.getTarget();
    assertThat(governmentPublication.getId()).isEqualTo(hashService.hash(governmentPublication));
    var types = governmentPublication.getTypes().stream().map(ResourceTypeEntity::getUri).toList();
    assertThat(types).contains(CATEGORY.getUri());
    assertThat(governmentPublication.getLabel()).isEqualTo("Autonomous");
    assertThat(governmentPublication.getDoc().size()).isEqualTo(3);
    validateLiterals(governmentPublication, CODE.getValue(), List.of("a"));
    validateLiterals(governmentPublication, TERM.getValue(), List.of("Autonomous"));
    validateLiterals(governmentPublication, LINK.getValue(), List.of("http://id.loc.gov/vocabulary/mgovtpubtype/a"));
  }

  private void validateDissertation(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(DISSERTATION.getUri());
    var dissertation = edge.getTarget();
    assertThat(dissertation.getId()).isEqualTo(hashService.hash(dissertation));
    var types = dissertation.getTypes().stream().map(ResourceTypeEntity::getUri).toList();
    assertThat(types).contains(ResourceTypeDictionary.DISSERTATION.getUri());
    assertThat(dissertation.getDoc().size()).isEqualTo(5);
    validateLiteral(dissertation, LABEL.getValue(), "label");
    validateLiteral(dissertation, DEGREE.getValue(), "degree");
    validateLiteral(dissertation, DISSERTATION_YEAR.getValue(), "dissertation year");
    validateLiteral(dissertation, DISSERTATION_NOTE.getValue(), "dissertation note");
    validateLiteral(dissertation, DISSERTATION_ID.getValue(), "dissertation id");
    var iterator = dissertation.getOutgoingEdges().iterator();
    var grantingInstitutionEdge = iterator.next();
    validateResourceEdge(grantingInstitutionEdge, dissertation, grantingInstitutionEdge.getTarget(),
      PredicateDictionary.GRANTING_INSTITUTION.getUri());
  }

  private void validateLanguage(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(LANGUAGE.getUri());
    var language = edge.getTarget();
    assertThat(language.getId()).isEqualTo(hashService.hash(language));
    var types = language.getTypes().stream().map(ResourceTypeEntity::getUri).toList();
    assertThat(types).contains(LANGUAGE_CATEGORY.getUri());
    assertThat(language.getDoc().size()).isEqualTo(3);
    validateLiteral(language, CODE.getValue(), "eng");
    validateLiteral(language, LINK.getValue(), "http://id.loc.gov/vocabulary/languages/eng");
    validateLiteral(language, TERM.getValue(), "English");
  }

  private void validateOriginPlace(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(ORIGIN_PLACE.getUri());
    var originPlace = edge.getTarget();
    assertThat(originPlace.getId()).isEqualTo(hashService.hash(originPlace));
    var types = originPlace.getTypes().stream().map(ResourceTypeEntity::getUri).toList();
    assertThat(types).contains(PLACE.getUri());
    assertThat(originPlace.getLabel()).isEqualTo("France");
    assertThat(originPlace.getDoc().size()).isEqualTo(3);
    validateLiterals(originPlace, NAME.getValue(), List.of("France"));
    validateLiterals(originPlace, CODE.getValue(), List.of("fr"));
    validateLiterals(originPlace, LINK.getValue(), List.of("http://id.loc.gov/vocabulary/countries/fr"));
  }

  private void validateResourceEdge(ResourceEdge edge, Resource source, Resource target, String predicate) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(predicate);
    assertThat(edge.getTarget()).isEqualTo(target);
  }

  private void validateCopyrightDate(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(COPYRIGHT.getUri());
    var copyrightEvent = edge.getTarget();
    assertThat(copyrightEvent.getLabel()).isEqualTo("copyright date value");
    assertThat(copyrightEvent.getTypes().iterator().next().getUri()).isEqualTo(COPYRIGHT_EVENT.getUri());
    assertThat(copyrightEvent.getId()).isEqualTo(hashService.hash(copyrightEvent));
    assertThat(copyrightEvent.getDoc().size()).isEqualTo(1);
    assertThat(copyrightEvent.getDoc().get(DATE.getValue()).size()).isEqualTo(1);
    assertThat(copyrightEvent.getDoc().get(DATE.getValue()).get(0).asText()).isEqualTo("copyright date value");
    assertThat(copyrightEvent.getOutgoingEdges()).isEmpty();
  }

  private LookupResources saveLookupResources() {
    var subject1 = saveResource(-2609581195837993519L, "subject 1", CONCEPT,
      "{\"http://bibfra.me/vocab/lite/name\": [\"Subject 1\"]}");
    var subject2 = saveResource(-643516859818423084L, "subject 2", CONCEPT,
      "{\"http://bibfra.me/vocab/lite/name\": [\"Subject 2\"]}");
    var unitedStates = saveResource(7109832602847218134L, "United States", PLACE,
      "{\"http://bibfra.me/vocab/lite/name\": [\"United States\"], "
        + "\"http://bibfra.me/vocab/marc/geographicAreaCode\": [\"n-us\"], "
        + "\"http://bibfra.me/vocab/marc/geographicCoverage\": [\"https://id.loc.gov/vocabulary/geographicAreas/n-us\"]}");
    var europe = saveResource(-4654600487710655316L, "Europe", PLACE,
      "{\"http://bibfra.me/vocab/lite/name\": [\"Europe\"], "
        + "\"http://bibfra.me/vocab/marc/geographicAreaCode\": [\"e\"], "
        + "\"http://bibfra.me/vocab/marc/geographicCoverage\": [\"https://id.loc.gov/vocabulary/geographicAreas/e\"]}");
    var genre1 = saveResource(-9064822434663187463L, "genre 1", FORM,
      "{\"http://bibfra.me/vocab/lite/name\": [\"genre 1\"]}", "8138e88f-4278-45ba-838c-816b80544f82");
    var genre2 = saveResource(-4816872480602594231L, "genre 2", FORM,
      "{\"http://bibfra.me/vocab/lite/name\": [\"genre 2\"]}");
    var creatorMeeting = saveResource(-603031702996824854L, "name-CREATOR-MEETING", MEETING,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-CREATOR-MEETING\"], "
        + "\"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-MEETING\"]}", "5f2220d5-ddf6-410a-a459-cd4b5e1b5ddb");
    var creatorPerson = saveResource(4359679744172518150L, "name-CREATOR-PERSON", PERSON,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-CREATOR-PERSON\"], "
        + "\"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-PERSON\"]}");
    var creatorOrganization = saveResource(-466724080127664871L, "name-CREATOR-ORGANIZATION", ORGANIZATION,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-CREATOR-ORGANIZATION\"], "
        + "\"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-ORGANIZATION\"]}");
    var creatorFamily = saveResource(8296435493593701280L, "name-CREATOR-FAMILY", FAMILY,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-CREATOR-FAMILY\"], "
        + "\"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-FAMILY\"]}");
    var contributorMeeting = saveResource(-7286109411186266518L, "name-CONTRIBUTOR-MEETING", MEETING,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-CONTRIBUTOR-MEETING\"], "
        + "\"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-MEETING\"]}");
    var contributorPerson = saveResource(-6054989039809126250L, "name-CONTRIBUTOR-PERSON", PERSON,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-CONTRIBUTOR-PERSON\"], "
        + "\"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-PERSON\"]}");
    var contributorOrganization = saveResource(-4246830624125472784L, "name-CONTRIBUTOR-ORGANIZATION", ORGANIZATION,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-CONTRIBUTOR-ORGANIZATION\"], "
        + "\"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-ORGANIZATION\"]}", "dad61944-17d2-4ade-afc5-ad4ce318a70b");
    var contributorFamily = saveResource(3094995075578514480L, "name-CONTRIBUTOR-FAMILY", FAMILY,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-CONTRIBUTOR-FAMILY\"], "
        + "\"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-FAMILY\"]}");
    var assigningAgency = saveResource(4932783899755316479L, "assigning agency", ORGANIZATION,
      "{\"http://bibfra.me/vocab/lite/name\": [\"assigning agency\"]}");
    var libraryOfCongress = saveResource(8752404686183471966L, "United States, Library of Congress", ORGANIZATION,
      "{\"http://bibfra.me/vocab/lite/name\": [\"United States, Library of Congress\"]}");
    var grantingInstitution1 = saveResource(5481852630377445080L, "granting institution 1", ORGANIZATION,
      "{\"http://bibfra.me/vocab/lite/name\": [\"granting institution 1\"]}");
    var grantingInstitution2 = saveResource(-6468470931408362304L, "granting institution 2", ORGANIZATION,
      "{\"http://bibfra.me/vocab/lite/name\": [\"granting institution 2\"]}");
    return new LookupResources(
      List.of(subject1, subject2),
      List.of(unitedStates, europe),
      List.of(genre1, genre2),
      List.of(creatorMeeting, creatorPerson, creatorOrganization, creatorFamily,
        contributorPerson, contributorMeeting, contributorOrganization, contributorFamily),
      List.of(assigningAgency, libraryOfCongress),
      List.of(grantingInstitution1, grantingInstitution2)
    );
  }

  private Resource saveResource(Long id, String label, ResourceTypeDictionary type, String doc) {
    return resourceTestService.saveGraph(createResource(id, label, type, doc));
  }

  private Resource saveResource(Long id, String label, ResourceTypeDictionary type, String doc, String srsId) {
    var resource = createResource(id, label, type, doc);
    resource.setFolioMetadata(new FolioMetadata(resource).setSrsId(srsId));
    return resourceTestService.saveGraph(resource);
  }

  @SneakyThrows
  private Resource createResource(Long id, String label, ResourceTypeDictionary type, String doc) {
    var resource = new Resource();
    resource.addType(new ResourceTypeEntity().setHash(type.getHash()).setUri(type.getUri()));
    resource.setLabel(label);
    resource.setDoc(OBJECT_MAPPER.readTree(doc));
    resource.setId(id);
    return resource;
  }

  private String toInstance() {
    return join(".", "$", path("resource"), path(INSTANCE.getUri()));
  }

  private String toInstanceReference(String workBase) {
    return join(".", workBase, arrayPath(INSTANCE_REF));
  }

  private String toWork() {
    return join(".", "$", path("resource"), path(WORK.getUri()));
  }

  private String toCreatorReferenceId() {
    return join(".", toWork(), "_creatorReference[0]", "id");
  }

  private String toWorkReference() {
    return join(".", toInstance(), arrayPath(WORK_REF));
  }

  private String toExtent() {
    return String.join(".", toInstance(), arrayPath(EXTENT.getValue()));
  }

  private String toDimensions() {
    return join(".", toInstance(), arrayPath(DIMENSIONS.getValue()));
  }

  private String toEditionStatement() {
    return join(".", toInstance(), arrayPath(EDITION.getValue()));
  }

  private String toSupplementaryContentLink() {
    return join(".", toInstance(), arrayPath(SUPPLEMENTARY_CONTENT.getUri()), arrayPath(LINK.getValue()));
  }

  private String toSupplementaryContentName() {
    return join(".", toInstance(), arrayPath(SUPPLEMENTARY_CONTENT.getUri()), arrayPath(NAME.getValue()));
  }

  private String toAccessLocationLink() {
    return join(".", toInstance(), arrayPath(ACCESS_LOCATION.getUri()), arrayPath(LINK.getValue()));
  }

  private String toAccessLocationNote() {
    return join(".", toInstance(), arrayPath(ACCESS_LOCATION.getUri()), arrayPath(NOTE.getValue()));
  }

  private String toProjectedProvisionDate() {
    return join(".", toInstance(), arrayPath(PROJECTED_PROVISION_DATE.getValue()));
  }

  private String toPrimaryTitlePartName(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(PART_NAME.getValue()));
  }

  private String toPrimaryTitlePartNumber(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(PART_NUMBER.getValue()));
  }

  private String toPrimaryTitleMain(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(MAIN_TITLE.getValue()));
  }

  private String toPrimaryTitleNonSortNum(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(NON_SORT_NUM.getValue()));
  }

  private String toPrimaryTitleSubtitle(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(SUBTITLE.getValue()));
  }

  private String toIssuance() {
    return join(".", toInstance(), arrayPath(ISSUANCE.getValue()));
  }

  private String toStatementOfResponsibility() {
    return join(".", toInstance(), arrayPath(STATEMENT_OF_RESPONSIBILITY.getValue()));
  }

  private String toInstanceNotesValues() {
    return join(".", toInstance(), dynamicArrayPath(NOTES_PROPERTY), arrayPath(VALUE_PROPERTY));
  }

  private String toInstanceNotesTypes() {
    return join(".", toInstance(), dynamicArrayPath(NOTES_PROPERTY), arrayPath(TYPE_PROPERTY));
  }

  private String toParallelTitlePartName(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(PART_NAME.getValue()));
  }

  private String toParallelTitlePartNumber(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(PART_NUMBER.getValue()));
  }

  private String toParallelTitleMain(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(MAIN_TITLE.getValue()));
  }

  private String toParallelTitleDate(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(DATE.getValue()));
  }

  private String toParallelTitleSubtitle(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(SUBTITLE.getValue()));
  }

  private String toParallelTitleNote(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(NOTE.getValue()));
  }

  private String toVariantTitlePartName(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(PART_NAME.getValue()));
  }

  private String toVariantTitlePartNumber(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(PART_NUMBER.getValue()));
  }

  private String toVariantTitleMain(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(MAIN_TITLE.getValue()));
  }

  private String toVariantTitleDate(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(DATE.getValue()));
  }

  private String toVariantTitleSubtitle(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(SUBTITLE.getValue()));
  }

  private String toVariantTitleType(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(VARIANT_TYPE.getValue()));
  }

  private String toVariantTitleNote(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(NOTE.getValue()));
  }

  private String toProviderEventDate(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(DATE.getValue()));
  }

  private String toProviderEventName(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(NAME.getValue()));
  }

  private String toProviderEventPlaceCode(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(PROVIDER_PLACE.getUri()),
      arrayPath(CODE.getValue()));
  }

  private String toProviderEventPlaceLabel(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(PROVIDER_PLACE.getUri()),
      arrayPath(LABEL.getValue()));
  }

  private String toProviderEventPlaceLink(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(PROVIDER_PLACE.getUri()),
      arrayPath(LINK.getValue()));
  }

  private String toProviderEventProviderDate(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(PROVIDER_DATE.getValue()));
  }

  private String toProviderEventSimplePlace(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(SIMPLE_PLACE.getValue()));
  }

  private String toLccnValue() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_LCCN.getUri()), arrayPath(NAME.getValue()));
  }

  private String toLccnStatusValue() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_LCCN.getUri()),
      arrayPath(STATUS.getUri()), arrayPath(LABEL.getValue()));
  }

  private String toLccnStatusLink() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_LCCN.getUri()), arrayPath(STATUS.getUri()),
      arrayPath(LINK.getValue()));
  }

  private String toIsbnValue() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_ISBN.getUri()), arrayPath(NAME.getValue()));
  }

  private String toIsbnQualifier() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_ISBN.getUri()),
      arrayPath(QUALIFIER.getValue()));
  }

  private String toIsbnStatusValue() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_ISBN.getUri()),
      arrayPath(STATUS.getUri()), arrayPath(LABEL.getValue()));
  }

  private String toIsbnStatusLink() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_ISBN.getUri()),
      arrayPath(STATUS.getUri()), arrayPath(LINK.getValue()));
  }

  private String toEanValue() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_EAN.getUri()),
      arrayPath(EAN_VALUE.getValue()));
  }

  private String toEanQualifier() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_EAN.getUri()),
      arrayPath(QUALIFIER.getValue()));
  }

  private String toLocalIdValue() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_LOCAL.getUri()),
      arrayPath(LOCAL_ID_VALUE.getValue()));
  }

  private String toLocalIdAssigner() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_LOCAL.getUri()),
      arrayPath(ASSIGNING_SOURCE.getValue()));
  }

  private String toOtherIdValue() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_UNKNOWN.getUri()),
      arrayPath(NAME.getValue()));
  }

  private String toOtherIdQualifier() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_UNKNOWN.getUri()),
      arrayPath(QUALIFIER.getValue()));
  }

  private String toCarrierCode(String instanceBase) {
    return join(".", instanceBase, arrayPath(CARRIER.getUri()), arrayPath(CODE.getValue()));
  }

  private String toCarrierLink(String instanceBase) {
    return join(".", instanceBase, arrayPath(CARRIER.getUri()), arrayPath(LINK.getValue()));
  }

  private String toCarrierTerm(String instanceBase) {
    return join(".", instanceBase, arrayPath(CARRIER.getUri()), arrayPath(TERM.getValue()));
  }

  private String toCopyrightDate() {
    return join(".", toInstance(), arrayPath(COPYRIGHT.getUri()), arrayPath(DATE.getValue()));
  }

  private String toMediaCode() {
    return join(".", toInstance(), arrayPath(MEDIA.getUri()), arrayPath(CODE.getValue()));
  }

  private String toMediaLink() {
    return join(".", toInstance(), arrayPath(MEDIA.getUri()), arrayPath(LINK.getValue()));
  }

  private String toMediaTerm() {
    return join(".", toInstance(), arrayPath(MEDIA.getUri()), arrayPath(TERM.getValue()));
  }

  private String toWorkNotesValues(String workBase) {
    return join(".", workBase, dynamicArrayPath(NOTES_PROPERTY), arrayPath(VALUE_PROPERTY));
  }

  private String toWorkNotesTypes(String workBase) {
    return join(".", workBase, dynamicArrayPath(NOTES_PROPERTY), arrayPath(TYPE_PROPERTY));
  }

  private String toWorkTableOfContents(String workBase) {
    return join(".", workBase, arrayPath(TABLE_OF_CONTENTS.getValue()));
  }

  private String toWorkSummary(String workBase) {
    return join(".", workBase, arrayPath(SUMMARY.getValue()));
  }

  private String toLanguageCode(String workBase) {
    return join(".", workBase, arrayPath(LANGUAGE.getUri()), arrayPath(CODE.getValue()));
  }

  private String toLanguageTerm(String workBase) {
    return join(".", workBase, arrayPath(LANGUAGE.getUri()), arrayPath(TERM.getValue()));
  }

  private String toLanguageLink(String workBase) {
    return join(".", workBase, arrayPath(LANGUAGE.getUri()), arrayPath(LINK.getValue()));
  }

  private String toClassificationSources(String workBase) {
    return join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri()), arrayPath(SOURCE.getValue()));
  }

  private String toClassificationCodes(String workBase) {
    return join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri()), arrayPath(CODE.getValue()));
  }

  private String toClassificationItemNumbers(String workBase) {
    return join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri()), arrayPath(ITEM_NUMBER.getValue()));
  }

  private String toWorkDeweyEditionNumber(String workBase) {
    return join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri()), arrayPath(EDITION_NUMBER.getValue()));
  }

  private String toWorkDeweyEdition(String workBase) {
    return join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri()), arrayPath(EDITION.getValue()));
  }

  private String toLcStatusValue(String workBase) {
    return join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri()), arrayPath(STATUS.getUri()),
      arrayPath(LABEL.getValue()));
  }

  private String toLcStatusLink(String workBase) {
    return join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri()), arrayPath(STATUS.getUri()),
      arrayPath(LINK.getValue()));
  }

  private String toClassificationAssigningSourceIds(String workBase) {
    return join(".", join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri())),
      dynamicArrayPath(ASSIGNING_SOURCE_REF), path(ID_PROPERTY));
  }

  private String toClassificationAssigningSourceLabels(String workBase) {
    return join(".", join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri())),
      dynamicArrayPath(ASSIGNING_SOURCE_REF), path(LABEL_PROPERTY));
  }

  private String toDissertationLabel(String workBase) {
    return join(".", workBase, arrayPath(DISSERTATION.getUri()), arrayPath(LABEL.getValue()));
  }

  private String toDissertationDegree(String workBase) {
    return join(".", workBase, arrayPath(DISSERTATION.getUri()), arrayPath(DEGREE.getValue()));
  }

  private String toDissertationYear(String workBase) {
    return join(".", workBase, arrayPath(DISSERTATION.getUri()), arrayPath(DISSERTATION_YEAR.getValue()));
  }

  private String toDissertationNote(String workBase) {
    return join(".", workBase, arrayPath(DISSERTATION.getUri()), arrayPath(DISSERTATION_NOTE.getValue()));
  }

  private String toDissertationId(String workBase) {
    return join(".", workBase, arrayPath(DISSERTATION.getUri()), arrayPath(DISSERTATION_ID.getValue()));
  }

  private String toDissertationGrantingInstitutionIds(String workBase) {
    return join(".", join(".", workBase, dynamicArrayPath(DISSERTATION.getUri())),
      dynamicArrayPath(GRANTING_INSTITUTION_REF), path(ID_PROPERTY));
  }

  private String toDissertationGrantingInstitutionLabels(String workBase) {
    return join(".", join(".", workBase, dynamicArrayPath(DISSERTATION.getUri())),
      dynamicArrayPath(GRANTING_INSTITUTION_REF), path(LABEL_PROPERTY));
  }

  private String toWorkCreatorId(String workBase) {
    return join(".", workBase, dynamicArrayPath(CREATOR_REF), path(ID_PROPERTY));
  }

  private String toWorkCreatorLabel(String workBase) {
    return join(".", workBase, dynamicArrayPath(CREATOR_REF), path(LABEL_PROPERTY));
  }

  private String toWorkCreatorType(String workBase) {
    return join(".", workBase, dynamicArrayPath(CREATOR_REF), path(TYPE_PROPERTY));
  }

  private String toWorkCreatorRoles(String workBase) {
    return join(".", workBase, dynamicArrayPath(CREATOR_REF), path(ROLES_PROPERTY));
  }

  private String toWorkContributorId(String workBase) {
    return join(".", workBase, dynamicArrayPath(CONTRIBUTOR_REF), path(ID_PROPERTY));
  }

  private String toWorkContributorLabel(String workBase) {
    return join(".", workBase, dynamicArrayPath(CONTRIBUTOR_REF), path(LABEL_PROPERTY));
  }

  private String toWorkContributorType(String workBase) {
    return join(".", workBase, dynamicArrayPath(CONTRIBUTOR_REF), path(TYPE_PROPERTY));
  }

  private String toWorkContributorRoles(String workBase) {
    return join(".", workBase, dynamicArrayPath(CONTRIBUTOR_REF), path("roles"));
  }

  private String toWorkContentTerm(String workBase) {
    return join(".", workBase, arrayPath(CONTENT.getUri()), arrayPath(TERM.getValue()));
  }

  private String toWorkSubjectLabel(String workBase) {
    return join(".", workBase, dynamicArrayPath(SUBJECT.getUri()), path("label"));
  }

  private String toWorkGeographicCoverageLabel(String workBase) {
    return join(".", workBase, dynamicArrayPath(GEOGRAPHIC_COVERAGE_REF), path("label"));
  }

  private String toWorkGenreLabel(String workBase) {
    return join(".", workBase, dynamicArrayPath(GENRE_REF), path("label"));
  }

  private String toWorkDateStart(String workBase) {
    return join(".", workBase, arrayPath(DATE_START.getValue()));
  }

  private String toWorkDateEnd(String workBase) {
    return join(".", workBase, arrayPath(DATE_END.getValue()));
  }

  private String toWorkGovPublicationCode(String workBase) {
    return join(".", workBase, arrayPath(GOVERNMENT_PUBLICATION.getUri()), arrayPath(CODE.getValue()));
  }

  private String toWorkGovPublicationTerm(String workBase) {
    return join(".", workBase, arrayPath(GOVERNMENT_PUBLICATION.getUri()), arrayPath(TERM.getValue()));
  }

  private String toWorkGovPublicationLink(String workBase) {
    return join(".", workBase, arrayPath(GOVERNMENT_PUBLICATION.getUri()), arrayPath(LINK.getValue()));
  }

  private String toWorkTargetAudienceCode(String workBase) {
    return join(".", workBase, arrayPath(TARGET_AUDIENCE.getUri()), arrayPath(CODE.getValue()));
  }

  private String toWorkTargetAudienceTerm(String workBase) {
    return join(".", workBase, arrayPath(TARGET_AUDIENCE.getUri()), arrayPath(TERM.getValue()));
  }

  private String toWorkTargetAudienceLink(String workBase) {
    return join(".", workBase, arrayPath(TARGET_AUDIENCE.getUri()), arrayPath(LINK.getValue()));
  }

  private String toWorkContentCode(String workBase) {
    return join(".", workBase, arrayPath(CONTENT.getUri()), arrayPath(CODE.getValue()));
  }

  private String toWorkContentLink(String workBase) {
    return join(".", workBase, arrayPath(CONTENT.getUri()), arrayPath(LINK.getValue()));
  }

  private String toId(String base) {
    return join(".", base, path("id"));
  }

  private String path(String path) {
    return format("['%s']", path);
  }

  private String arrayPath(String path) {
    return format("['%s'][0]", path);
  }

  private String dynamicArrayPath(String path) {
    return format("['%s'][*]", path);
  }

  private ArrayList getStatus(LinkedHashMap instance) {
    var map = (ArrayList) instance.get(MAP.getUri());
    var lccn = (LinkedHashMap) ((LinkedHashMap) map.get(0)).get(ID_LCCN.getUri());
    return (ArrayList) lccn.get(STATUS.getUri());
  }

  private record LookupResources(
    List<Resource> subjects,
    List<Resource> geographicCoverages,
    List<Resource> genres,
    List<Resource> creators,
    List<Resource> assigningSources,
    List<Resource> grantingInstitutions
  ) {
  }
}
