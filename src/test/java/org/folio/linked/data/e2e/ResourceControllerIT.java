package org.folio.linked.data.e2e;

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
import static org.folio.ld.dictionary.PredicateDictionary.EDITOR;
import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.PredicateDictionary.GEOGRAPHIC_COVERAGE;
import static org.folio.ld.dictionary.PredicateDictionary.GOVERNMENT_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.IS_DEFINED_BY;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.MEDIA;
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
import static org.folio.ld.dictionary.PropertyDictionary.DESCRIPTION_SOURCE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.EAN_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION_STATEMENT;
import static org.folio.ld.dictionary.PropertyDictionary.EXHIBITIONS_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.EXTENT;
import static org.folio.ld.dictionary.PropertyDictionary.FUNDING_INFORMATION;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUANCE;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUANCE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUING_BODY;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LANGUAGE;
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
import static org.folio.ld.dictionary.PropertyDictionary.RESPONSIBILITY_STATEMENT;
import static org.folio.ld.dictionary.PropertyDictionary.SIMPLE_PLACE;
import static org.folio.ld.dictionary.PropertyDictionary.SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.PropertyDictionary.SUMMARY;
import static org.folio.ld.dictionary.PropertyDictionary.TABLE_OF_CONTENTS;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.ld.dictionary.PropertyDictionary.TYPE_OF_REPORT;
import static org.folio.ld.dictionary.PropertyDictionary.VARIANT_TYPE;
import static org.folio.ld.dictionary.PropertyDictionary.WITH_NOTE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.COPYRIGHT_EVENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LOCAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.model.ErrorCode.NOT_FOUND_ERROR;
import static org.folio.linked.data.model.entity.Resource.withInitializedSets;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.INSTANCE_WITH_WORK_REF_SAMPLE;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getSampleInstanceDtoMap;
import static org.folio.linked.data.test.TestUtil.getSampleWorkDtoMap;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;
import static org.folio.linked.data.util.Constants.TYPE;
import static org.folio.search.domain.dto.ResourceEventType.CREATE;
import static org.folio.search.domain.dto.ResourceEventType.DELETE;
import static org.folio.search.domain.dto.ResourceEventType.UPDATE;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.SneakyThrows;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.domain.dto.WorkField;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.service.KafkaSender;
import org.folio.linked.data.utils.ResourceTestService;
import org.folio.search.domain.dto.ResourceEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class ResourceControllerIT {

  private static final String RESOURCE_URL = "/resource";
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
  private static final String WORK_ID_PLACEHOLDER = "%WORK_ID%";
  private static final String INSTANCE_ID_PLACEHOLDER = "%INSTANCE_ID%";
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private Environment env;
  @SpyBean
  private KafkaSender kafkaSender;
  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  private ResourceTestService resourceTestService;
  private LookupResources lookupResources;

  @BeforeEach
  public void beforeEach() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "resource_edges", "resource_type_map", "resources");
    lookupResources = saveLookupResources();
  }

  @Test
  void createInstanceWithWorkRef_shouldSaveEntityCorrectly() throws Exception {
    // given
    var work = resourceTestService.saveGraph(getSampleWork(null));
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(INSTANCE_WITH_WORK_REF_SAMPLE.replaceAll(WORK_ID_PLACEHOLDER, work.getResourceHash().toString()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    validateInstanceResponse(resultActions, toInstance());

    var resourceResponse = objectMapper.readValue(response, ResourceDto.class);
    var id = ((InstanceField) resourceResponse.getResource()).getInstance().getId();
    var instanceResource = resourceTestService.getResourceById(id, 3);
    validateInstance(instanceResource, true);
    var workId = ((InstanceField) resourceResponse.getResource()).getInstance().getWorkReference().get(0).getId();
    checkKafkaMessage(Long.valueOf(workId), CREATE);
  }

  @Test
  void createWorkWithInstanceRef_shouldSaveEntityCorrectly() throws Exception {
    // given
    var instanceForReference = resourceTestService.saveGraph(getSampleInstanceResource(null, null));
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(loadResourceAsString("samples/work_and_instance_ref.json")
        .replaceAll(INSTANCE_ID_PLACEHOLDER, instanceForReference.getResourceHash().toString()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    validateWorkResponse(resultActions, toWork());

    var resourceResponse = objectMapper.readValue(response, ResourceDto.class);
    var id = ((WorkField) resourceResponse.getResource()).getWork().getId();
    var workResource = resourceTestService.getResourceById(id, 4);
    validateWork(workResource, true);
    checkKafkaMessage(workResource.getResourceHash(), CREATE);
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

    var updateRequest = put(RESOURCE_URL + "/" + originalInstance.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(
        OBJECT_MAPPER.writeValueAsString(updateDto).replaceAll(WORK_ID_PLACEHOLDER, work.getResourceHash().toString())
      );

    // when
    var resultActions = mockMvc.perform(updateRequest);

    // then
    assertFalse(resourceTestService.existsById(originalInstance.getResourceHash()));
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(toInstance(), notNullValue()))
      .andReturn().getResponse().getContentAsString();
    var resourceResponse = objectMapper.readValue(response, ResourceDto.class);
    var instanceId = ((InstanceField) resourceResponse.getResource()).getInstance().getId();
    var updatedInstance = resourceTestService.getResourceById(instanceId, 1);
    assertThat(updatedInstance.getResourceHash()).isNotNull();
    assertThat(updatedInstance.getLabel()).isEqualTo(originalInstance.getLabel());
    assertThat(updatedInstance.getTypes().iterator().next().getUri()).isEqualTo(INSTANCE.getUri());
    assertThat(updatedInstance.getInventoryId()).isEqualTo(originalInstance.getInventoryId());
    assertThat(updatedInstance.getSrsId()).isEqualTo(originalInstance.getSrsId());
    assertThat(updatedInstance.getDoc().get(DIMENSIONS.getValue()).get(0).asText()).isEqualTo("200 m");
    assertThat(updatedInstance.getOutgoingEdges()).hasSize(originalInstance.getOutgoingEdges().size());
    checkKafkaMessage(work.getResourceHash(), UPDATE);
  }

  @Test
  void update_shouldReturnCorrectlyUpdatedWorkWithInstanceRef_deleteOldOne_sendMessages() throws Exception {
    // given
    var instance = getSampleInstanceResource(null, null);
    var originalWork = resourceTestService.saveGraph(getSampleWork(instance));
    var updateDto = getSampleWorkDtoMap();
    var workMap = (LinkedHashMap) ((LinkedHashMap) updateDto.get("resource")).get(WORK.getUri());
    var newlyAddedSummary = "newly added summary";
    workMap.put(SUMMARY.getValue(), List.of(newlyAddedSummary));

    var updateRequest = put(RESOURCE_URL + "/" + originalWork.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(OBJECT_MAPPER.writeValueAsString(updateDto)
        .replaceAll(INSTANCE_ID_PLACEHOLDER, instance.getResourceHash().toString())
      );

    // when
    var resultActions = mockMvc.perform(updateRequest);

    // then
    assertFalse(resourceTestService.existsById(originalWork.getResourceHash()));
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(toWork(), notNullValue()))
      .andReturn().getResponse().getContentAsString();
    var resourceResponse = objectMapper.readValue(response, ResourceDto.class);
    var id = ((WorkField) resourceResponse.getResource()).getWork().getId();

    var updatedWork = resourceTestService.getResourceById(id, 1);
    assertThat(updatedWork.getResourceHash()).isNotNull();
    assertThat(updatedWork.getLabel()).isEqualTo(originalWork.getLabel());
    assertThat(updatedWork.getTypes().iterator().next().getUri()).isEqualTo(WORK.getUri());
    assertThat(updatedWork.getDoc().get(SUMMARY.getValue()).get(0).asText()).isEqualTo(newlyAddedSummary);
    assertThat(updatedWork.getOutgoingEdges()).hasSize(originalWork.getOutgoingEdges().size());
    assertThat(updatedWork.getIncomingEdges()).hasSize(originalWork.getIncomingEdges().size());
    checkKafkaMessage(Long.valueOf(id), UPDATE);
  }

  @Test
  void getInstanceById_shouldReturnInstanceWithWorkRef() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleInstanceResource());
    var requestBuilder = get(RESOURCE_URL + "/" + existed.getResourceHash())
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
    var requestBuilder = get(RESOURCE_URL + "/" + existed.getResourceHash())
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
      .andExpect(jsonPath("errors[0].message", equalTo(RESOURCE_WITH_GIVEN_ID
        + notExistedId + IS_NOT_FOUND)))
      .andExpect(jsonPath("errors[0].type", equalTo(NotFoundException.class.getSimpleName())))
      .andExpect(jsonPath("errors[0].code", equalTo(NOT_FOUND_ERROR.getValue())))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }

  @Test
  void getBibframeShortInfoPage_shouldReturnPageWithExistedEntities() throws Exception {
    // given
    var existed = Lists.newArrayList(
        resourceTestService.saveGraph(getSampleInstanceResource(100L)),
        resourceTestService.saveGraph(getSampleInstanceResource(200L)),
        resourceTestService.saveGraph(getSampleInstanceResource(300L))
      ).stream()
      .map(Resource::getResourceHash)
      .map(Object::toString)
      .toList();
    var requestBuilder = get(RESOURCE_URL)
      .param(TYPE, INSTANCE.getUri())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("number", equalTo(0)))
      .andExpect(jsonPath("total_pages", equalTo(1)))
      .andExpect(jsonPath("total_elements", equalTo(3)))
      .andExpect(jsonPath("content", hasSize(3)))
      .andExpect(jsonPath("content[*].id", containsInAnyOrder(existed.toArray())));
  }

  @Test
  void deleteResourceById_shouldDeleteRootInstanceAndRootEdges_reindexWork() throws Exception {
    // given
    var work = getSampleWork(null);
    var instance = resourceTestService.saveGraph(getSampleInstanceResource(null, work));
    assertThat(resourceTestService.findById(instance.getResourceHash())).isPresent();
    assertThat(resourceTestService.countResources()).isEqualTo(45);
    assertThat(resourceTestService.countEdges()).isEqualTo(47);
    var requestBuilder = delete(RESOURCE_URL + "/" + instance.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions.andExpect(status().isNoContent());
    assertThat(resourceTestService.existsById(instance.getResourceHash())).isFalse();
    assertThat(resourceTestService.countResources()).isEqualTo(44);
    assertThat(resourceTestService.findEdgeById(instance.getOutgoingEdges().iterator().next().getId())).isNotPresent();
    assertThat(resourceTestService.countEdges()).isEqualTo(29);
    checkKafkaMessage(work.getResourceHash(), UPDATE);
  }

  @Test
  void deleteResourceById_shouldDeleteRootWorkAndRootEdges() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleWork(getSampleInstanceResource(null, null)));
    assertThat(resourceTestService.findById(existed.getResourceHash())).isPresent();
    assertThat(resourceTestService.countResources()).isEqualTo(45);
    assertThat(resourceTestService.countEdges()).isEqualTo(47);
    var requestBuilder = delete(RESOURCE_URL + "/" + existed.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions.andExpect(status().isNoContent());
    assertThat(resourceTestService.existsById(existed.getResourceHash())).isFalse();
    assertThat(resourceTestService.countResources()).isEqualTo(44);
    assertThat(resourceTestService.findEdgeById(existed.getOutgoingEdges().iterator().next().getId())).isNotPresent();
    assertThat(resourceTestService.countEdges()).isEqualTo(25);
    checkKafkaMessage(existed.getResourceHash(), DELETE);
  }

  @Test
  void updateResource_shouldNot_deleteExistedResource_createNewResource_sendRelevantKafkaMessages_whenUpdateFailed()
    throws Exception {
    //given
    var existedResource = resourceTestService.saveGraph(getSampleInstanceResource(100L));
    var requestBuilder = put(RESOURCE_URL + "/" + existedResource.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content("{\"resource\": {\"id\": null}}");

    //when
    mockMvc.perform(requestBuilder);

    //then
    assertTrue(resourceTestService.existsById(existedResource.getResourceHash()));
    verify(kafkaSender, never()).sendResourceDeleted(existedResource.getResourceHash());
    verify(kafkaSender, never()).sendResourceCreated(any(), eq(true));
  }

  @Test
  void getResourceViewById_shouldReturnInstance() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleInstanceResource());
    var requestBuilder = get(RESOURCE_URL + "/" + existed.getResourceHash() + "/marc")
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
      .andExpect(jsonPath("id", equalTo(existed.getResourceHash().toString())))
      .andExpect(jsonPath("recordType", equalTo("MARC_BIB")))
      .andExpect(jsonPath("parsedRecord.content", notNullValue()));
  }

  protected void checkKafkaMessage(Long id, ResourceEventType eventType) {
    // nothing to check without Folio profile
  }

  private void validateInstanceResponse(ResultActions resultActions, String instanceBase) throws Exception {
    resultActions
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(instanceBase, notNullValue()))
      .andExpect(jsonPath(toId(instanceBase), notNullValue()))
      .andExpect(jsonPath(toCarrierCode(instanceBase), equalTo("carrier code")))
      .andExpect(jsonPath(toCarrierLink(instanceBase), equalTo("carrier link")))
      .andExpect(jsonPath(toCarrierTerm(instanceBase), equalTo("carrier term")))
      .andExpect(jsonPath(toInstanceTitlePartName(instanceBase), equalTo(List.of("Instance: partName"))))
      .andExpect(jsonPath(toInstanceTitlePartNumber(instanceBase), equalTo(List.of("Instance: partNumber"))))
      .andExpect(jsonPath(toInstanceTitleMain(instanceBase), equalTo(List.of("Instance: mainTitle"))))
      .andExpect(jsonPath(toInstanceTitleNonSortNum(instanceBase), equalTo(List.of("Instance: nonSortNum"))))
      .andExpect(jsonPath(toInstanceTitleSubtitle(instanceBase), equalTo(List.of("Instance: subTitle"))))
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
        .andExpect(jsonPath(toInventoryId(), equalTo("2165ef4b-001f-46b3-a60e-52bcdeb3d5a1")))
        .andExpect(jsonPath(toSrsId(), equalTo("43d58061-decf-4d74-9747-0e1c368e861b")))
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
        .andExpect(jsonPath(toMediaCode(), equalTo("media code")))
        .andExpect(jsonPath(toMediaLink(), equalTo("media link")))
        .andExpect(jsonPath(toMediaTerm(), equalTo("media term")))
        .andExpect(jsonPath(toOtherIdValue(), equalTo(List.of("otherId value"))))
        .andExpect(jsonPath(toOtherIdQualifier(), equalTo(List.of("otherId qualifier"))))
        .andExpect(jsonPath(toProviderEventDate(PE_PRODUCTION), equalTo("production date")))
        .andExpect(jsonPath(toProviderEventName(PE_PRODUCTION), equalTo("production name")))
        .andExpect(jsonPath(toProviderEventPlaceCode(PE_PRODUCTION), equalTo("production providerPlace code")))
        .andExpect(jsonPath(toProviderEventPlaceLabel(PE_PRODUCTION), equalTo("production providerPlace label")))
        .andExpect(jsonPath(toProviderEventPlaceLink(PE_PRODUCTION), equalTo("production providerPlace link")))
        .andExpect(jsonPath(toProviderEventProviderDate(PE_PRODUCTION), equalTo("production provider date")))
        .andExpect(jsonPath(toProviderEventSimplePlace(PE_PRODUCTION), equalTo("production simple place")))
        .andExpect(jsonPath(toProviderEventDate(PE_PUBLICATION), equalTo("publication date")))
        .andExpect(jsonPath(toProviderEventName(PE_PUBLICATION), equalTo("publication name")))
        .andExpect(jsonPath(toProviderEventPlaceCode(PE_PUBLICATION), equalTo("publication providerPlace code")))
        .andExpect(jsonPath(toProviderEventPlaceLabel(PE_PUBLICATION), equalTo("publication providerPlace label")))
        .andExpect(jsonPath(toProviderEventPlaceLink(PE_PUBLICATION), equalTo("publication providerPlace link")))
        .andExpect(jsonPath(toProviderEventProviderDate(PE_PUBLICATION), equalTo("publication provider date")))
        .andExpect(jsonPath(toProviderEventSimplePlace(PE_PUBLICATION), equalTo("publication simple place")))
        .andExpect(jsonPath(toProviderEventDate(PE_DISTRIBUTION), equalTo("distribution date")))
        .andExpect(jsonPath(toProviderEventName(PE_DISTRIBUTION), equalTo("distribution name")))
        .andExpect(jsonPath(toProviderEventPlaceCode(PE_DISTRIBUTION), equalTo("distribution providerPlace code")))
        .andExpect(jsonPath(toProviderEventPlaceLabel(PE_DISTRIBUTION), equalTo("distribution providerPlace label")))
        .andExpect(jsonPath(toProviderEventPlaceLink(PE_DISTRIBUTION), equalTo("distribution providerPlace link")))
        .andExpect(jsonPath(toProviderEventProviderDate(PE_DISTRIBUTION), equalTo("distribution provider date")))
        .andExpect(jsonPath(toProviderEventSimplePlace(PE_DISTRIBUTION), equalTo("distribution simple place")))
        .andExpect(jsonPath(toProviderEventDate(PE_MANUFACTURE), equalTo("manufacture date")))
        .andExpect(jsonPath(toProviderEventName(PE_MANUFACTURE), equalTo("manufacture name")))
        .andExpect(jsonPath(toProviderEventPlaceCode(PE_MANUFACTURE), equalTo("manufacture providerPlace code")))
        .andExpect(jsonPath(toProviderEventPlaceLabel(PE_MANUFACTURE), equalTo("manufacture providerPlace label")))
        .andExpect(jsonPath(toProviderEventPlaceLink(PE_MANUFACTURE), equalTo("manufacture providerPlace link")))
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
      .andExpect(jsonPath(toWorkLanguage(workBase), equalTo("eng")))
      .andExpect(jsonPath(toWorkDeweyCode(workBase), equalTo("709.83")))
      .andExpect(jsonPath(toWorkDeweySource(workBase), equalTo("ddc")))
      .andExpect(jsonPath(toWorkCreatorId(workBase), containsInAnyOrder("1001", "1002", "1003", "1004")))
      .andExpect(jsonPath(toWorkCreatorLabel(workBase), containsInAnyOrder("name-MEETING", "name-PERSON",
        "name-ORGANIZATION", "name-FAMILY")))
      .andExpect(jsonPath(toWorkCreatorType(workBase), containsInAnyOrder(MEETING.getUri(), PERSON.getUri(),
        ORGANIZATION.getUri(), FAMILY.getUri())))
      .andExpect(jsonPath(toWorkCreatorRoles(workBase), equalTo(List.of(List.of(AUTHOR.getUri())))))
      .andExpect(jsonPath(toWorkContributorId(workBase), containsInAnyOrder("1005", "1006", "1007", "1008")))
      .andExpect(jsonPath(toWorkContributorLabel(workBase), containsInAnyOrder("name-ORGANIZATION",
        "name-FAMILY", "name-PERSON", "name-MEETING")))
      .andExpect(jsonPath(toWorkContributorType(workBase), containsInAnyOrder(ORGANIZATION.getUri(), FAMILY.getUri(),
        PERSON.getUri(), MEETING.getUri())))
      .andExpect(jsonPath(toWorkContributorRoles(workBase), equalTo(List.of(List.of(EDITOR.getUri(),
        ASSIGNEE.getUri())))))
      .andExpect(jsonPath(toWorkContentLink(workBase), equalTo("http://id.loc.gov/vocabulary/contentTypes/txt")))
      .andExpect(jsonPath(toWorkContentCode(workBase), equalTo("txt")))
      .andExpect(jsonPath(toWorkContentTerm(workBase), equalTo("text")))
      .andExpect(jsonPath(toWorkSubjectLabel(workBase), equalTo(List.of("subject 1", "subject 2"))));
    if (workBase.equals(toWork())) {
      resultActions
        .andExpect(jsonPath(toWorkSummary(workBase), equalTo("summary text")))
        .andExpect(jsonPath(toWorkTableOfContents(workBase), equalTo("table of contents")))
        .andExpect(jsonPath(toWorkResponsibilityStatement(workBase), equalTo("statement of responsibility")))
        .andExpect(jsonPath(toWorkNotesValues(workBase),
          containsInAnyOrder("language note", "bibliography note", "note", "another note", "another note")))
        .andExpect(jsonPath(toWorkNotesTypes(workBase), containsInAnyOrder("http://bibfra.me/vocab/marc/languageNote",
          "http://bibfra.me/vocab/marc/languageNote", "http://bibfra.me/vocab/marc/bibliographyNote",
          "http://bibfra.me/vocab/lite/note", "http://bibfra.me/vocab/lite/note")))
        .andExpect(jsonPath(toInstanceReference(workBase), notNullValue()))
        .andExpect(jsonPath(toWorkGeographicCoverageLabel(workBase), equalTo(List.of("United States", "Europe"))))
        .andExpect(jsonPath(toWorkGenreLabel(workBase), equalTo(List.of("genre 1", "genre 2"))))
        .andExpect(jsonPath(toWorkDateStart(workBase), equalTo("2024")))
        .andExpect(jsonPath(toWorkDateEnd(workBase), equalTo("2025")))
        .andExpect(jsonPath(toWorkGovernmentPublicationCode(workBase), equalTo("a")))
        .andExpect(jsonPath(toWorkGovernmentPublicationTerm(workBase), equalTo("Autonomous")))
        .andExpect(jsonPath(toWorkGovernmentPublicationLink(workBase), equalTo("http://id.loc.gov/vocabulary/mgovtpubtype/a")))
        .andExpect(jsonPath(toWorkTargetAudienceCode(workBase), equalTo("b")))
        .andExpect(jsonPath(toWorkTargetAudienceTerm(workBase), equalTo("Primary")))
        .andExpect(jsonPath(toWorkTargetAudienceLink(workBase), equalTo("http://id.loc.gov/vocabulary/maudience/pri")));
      validateInstanceResponse(resultActions, toInstanceReference(workBase));
    }
  }

  private void validateInstance(Resource instance, boolean validateFullWork) {
    assertThat(instance.getResourceHash()).isNotNull();
    assertThat(instance.getLabel()).isEqualTo("Instance: mainTitle");
    assertThat(instance.getTypes().iterator().next().getUri()).isEqualTo(INSTANCE.getUri());
    assertThat(instance.getInventoryId()).hasToString("2165ef4b-001f-46b3-a60e-52bcdeb3d5a1");
    assertThat(instance.getSrsId()).hasToString("43d58061-decf-4d74-9747-0e1c368e861b");
    assertThat(instance.getDoc().size()).isEqualTo(19);
    validateLiteral(instance, DIMENSIONS.getValue(), "20 cm");
    validateLiteral(instance, EDITION_STATEMENT.getValue(), "edition statement");
    validateLiteral(instance, PROJECTED_PROVISION_DATE.getValue(), "projected provision date");
    validateLiteral(instance, ISSUANCE.getValue(), "single unit");
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
    validateCategory(edgeIterator.next(), instance, CARRIER);
    validateCategory(edgeIterator.next(), instance, MEDIA);
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
    validateProviderEvent(edgeIterator.next(), instance, PE_MANUFACTURE);
    validateProviderEvent(edgeIterator.next(), instance, PE_DISTRIBUTION);
    validateProviderEvent(edgeIterator.next(), instance, PE_PRODUCTION);
    validateProviderEvent(edgeIterator.next(), instance, PE_PUBLICATION);
    validateOtherId(edgeIterator.next(), instance);
    validateEan(edgeIterator.next(), instance);
    validateInstanceTitle(edgeIterator.next(), instance);
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

  private void validateInstanceTitle(ResourceEdge edge, Resource source) {
    validateSampleTitleBase(edge, source, ResourceTypeDictionary.TITLE, "Instance: ");
    var title = edge.getTarget();
    assertThat(title.getDoc().size()).isEqualTo(5);
    assertThat(title.getDoc().get(NON_SORT_NUM.getValue()).size()).isEqualTo(1);
    assertThat(title.getDoc().get(NON_SORT_NUM.getValue()).get(0).asText()).isEqualTo("Instance: nonSortNum");
    assertThat(title.getOutgoingEdges()).isEmpty();
  }

  private void validateParallelTitle(ResourceEdge edge, Resource source) {
    validateSampleTitleBase(edge, source, PARALLEL_TITLE, "Parallel: ");
    var title = edge.getTarget();
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
    assertThat(title.getResourceHash()).isNotNull();
    assertThat(title.getDoc().get(PART_NAME.getValue()).size()).isEqualTo(1);
    assertThat(title.getDoc().get(PART_NAME.getValue()).get(0).asText()).isEqualTo(prefix + "partName");
    assertThat(title.getDoc().get(PART_NUMBER.getValue()).size()).isEqualTo(1);
    assertThat(title.getDoc().get(PART_NUMBER.getValue()).get(0).asText()).isEqualTo(prefix + "partNumber");
    assertThat(title.getDoc().get(MAIN_TITLE.getValue()).size()).isEqualTo(1);
    assertThat(title.getDoc().get(MAIN_TITLE.getValue()).get(0).asText()).isEqualTo(prefix + "mainTitle");
    assertThat(title.getDoc().get(SUBTITLE.getValue()).size()).isEqualTo(1);
    assertThat(title.getDoc().get(SUBTITLE.getValue()).get(0).asText()).isEqualTo(prefix + "subTitle");
  }

  private void validateProviderEvent(ResourceEdge edge, Resource source, PredicateDictionary predicate) {
    var type = predicate.getUri().substring(predicate.getUri().indexOf("marc/") + 5);
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(predicate.getUri());
    var providerEvent = edge.getTarget();
    assertThat(providerEvent.getLabel()).isEqualTo(type + " name");
    assertThat(providerEvent.getTypes().iterator().next().getUri()).isEqualTo(PROVIDER_EVENT.getUri());
    assertThat(providerEvent.getResourceHash()).isNotNull();
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
    validateProviderPlace(providerEvent.getOutgoingEdges().iterator().next(), providerEvent, type);
  }

  private void validateProviderPlace(ResourceEdge edge, Resource source, String prefix) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(PROVIDER_PLACE.getUri());
    var place = edge.getTarget();
    assertThat(place.getLabel()).isEqualTo(prefix + " providerPlace label");
    assertThat(place.getTypes().iterator().next().getUri()).isEqualTo(PLACE.getUri());
    assertThat(place.getResourceHash()).isNotNull();
    assertThat(place.getDoc().size()).isEqualTo(3);
    assertThat(place.getDoc().get(CODE.getValue()).size()).isEqualTo(1);
    assertThat(place.getDoc().get(CODE.getValue()).get(0).asText()).isEqualTo(prefix + " providerPlace code");
    assertThat(place.getDoc().get(LABEL.getValue()).size()).isEqualTo(1);
    assertThat(place.getDoc().get(LABEL.getValue()).get(0).asText()).isEqualTo(prefix + " providerPlace label");
    assertThat(place.getDoc().get(LINK.getValue()).size()).isEqualTo(1);
    assertThat(place.getDoc().get(LINK.getValue()).get(0).asText()).isEqualTo(prefix + " providerPlace link");
    assertThat(place.getOutgoingEdges()).isEmpty();
  }

  private void validateLccn(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(MAP.getUri());
    var lccn = edge.getTarget();
    assertThat(lccn.getLabel()).isEqualTo("lccn value");
    assertThat(lccn.getTypes().iterator().next().getUri()).isEqualTo(ID_LCCN.getUri());
    assertThat(lccn.getResourceHash()).isNotNull();
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
    assertThat(isbn.getTypes().iterator().next().getUri()).isEqualTo(ID_ISBN.getUri());
    assertThat(isbn.getResourceHash()).isNotNull();
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
    assertThat(ean.getTypes().iterator().next().getUri()).isEqualTo(ID_EAN.getUri());
    assertThat(ean.getResourceHash()).isNotNull();
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
    assertThat(localId.getTypes().iterator().next().getUri()).isEqualTo(ID_LOCAL.getUri());
    assertThat(localId.getResourceHash()).isNotNull();
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
    assertThat(otherId.getTypes().iterator().next().getUri()).isEqualTo(ID_UNKNOWN.getUri());
    assertThat(otherId.getResourceHash()).isNotNull();
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
    assertThat(status.getResourceHash()).isNotNull();
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
    assertThat(supplementaryContent.getResourceHash()).isNotNull();

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
    assertThat(locator.getResourceHash()).isNotNull();
    assertThat(locator.getDoc().size()).isEqualTo(2);
    assertThat(locator.getDoc().get(LINK.getValue()).size()).isEqualTo(1);
    assertThat(locator.getDoc().get(LINK.getValue()).get(0).asText()).isEqualTo("accessLocation value");
    assertThat(locator.getDoc().get(NOTE.getValue()).size()).isEqualTo(1);
    assertThat(locator.getDoc().get(NOTE.getValue()).get(0).asText()).isEqualTo("accessLocation note");
    assertThat(locator.getOutgoingEdges()).isEmpty();
  }

  private void validateCategory(ResourceEdge edge, Resource source, PredicateDictionary pred) {
    var prefix = pred.getUri().substring(pred.getUri().lastIndexOf("/") + 1);
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(pred.getUri());
    var media = edge.getTarget();
    assertThat(media.getLabel()).isEqualTo(prefix + " term");
    assertThat(media.getTypes().iterator().next().getUri()).isEqualTo(CATEGORY.getUri());
    assertThat(media.getResourceHash()).isNotNull();
    assertThat(media.getDoc().size()).isEqualTo(4);
    validateLiteral(media, CODE.getValue(), prefix + " code");
    validateLiteral(media, TERM.getValue(), prefix + " term");
    validateLiteral(media, LINK.getValue(), prefix + " link");
    validateLiteral(media, SOURCE.getValue(), prefix + " source");
    assertThat(media.getOutgoingEdges()).isEmpty();
  }

  private void validateWork(Resource work, boolean validateFullInstance) {
    assertThat(work.getResourceHash()).isNotNull();
    assertThat(work.getTypes().iterator().next().getUri()).isEqualTo(WORK.getUri());
    assertThat(work.getDoc().size()).isEqualTo(9);
    validateLiterals(work, DATE_START.getValue(), List.of("2024"));
    validateLiterals(work, DATE_END.getValue(), List.of("2025"));
    validateLiteral(work, RESPONSIBILITY_STATEMENT.getValue(), "statement of responsibility");
    validateLiteral(work, SUMMARY.getValue(), "summary text");
    validateLiteral(work, LANGUAGE.getValue(), "eng");
    validateLiteral(work, TABLE_OF_CONTENTS.getValue(), "table of contents");
    validateLiteral(work, BIBLIOGRAPHY_NOTE.getValue(), "bibliography note");
    validateLiterals(work, LANGUAGE_NOTE.getValue(), List.of("language note", "another note"));
    validateLiterals(work, NOTE.getValue(), List.of("note", "another note"));
    var outgoingEdgeIterator = work.getOutgoingEdges().iterator();
    validateWorkContentType(outgoingEdgeIterator.next(), work);
    validateWorkTargetAudience(outgoingEdgeIterator.next(), work);
    validateWorkClassification(outgoingEdgeIterator.next(), work);
    validateWorkGovernmentPublication(outgoingEdgeIterator.next(), work);
    validateWorkContributor(outgoingEdgeIterator.next(), work, ORGANIZATION, CREATOR.getUri());
    validateWorkContributor(outgoingEdgeIterator.next(), work, ORGANIZATION, EDITOR.getUri());
    validateWorkContributor(outgoingEdgeIterator.next(), work, ORGANIZATION, CONTRIBUTOR.getUri());
    validateWorkContributor(outgoingEdgeIterator.next(), work, ORGANIZATION, ASSIGNEE.getUri());
    validateWorkContributor(outgoingEdgeIterator.next(), work, FAMILY, CREATOR.getUri());
    validateWorkContributor(outgoingEdgeIterator.next(), work, FAMILY, CONTRIBUTOR.getUri());
    validateWorkContributor(outgoingEdgeIterator.next(), work, PERSON, AUTHOR.getUri());
    validateWorkContributor(outgoingEdgeIterator.next(), work, PERSON, CREATOR.getUri());
    validateWorkContributor(outgoingEdgeIterator.next(), work, PERSON, CONTRIBUTOR.getUri());
    validateResourceEdge(outgoingEdgeIterator.next(), work, lookupResources.subjects().get(0), SUBJECT.getUri());
    validateResourceEdge(outgoingEdgeIterator.next(), work, lookupResources.subjects().get(1), SUBJECT.getUri());
    validateResourceEdge(outgoingEdgeIterator.next(), work, lookupResources.genres().get(0), GENRE.getUri());
    validateResourceEdge(outgoingEdgeIterator.next(), work, lookupResources.genres().get(1), GENRE.getUri());
    validateResourceEdge(outgoingEdgeIterator.next(), work, lookupResources.geographicCoverages().get(0),
      GEOGRAPHIC_COVERAGE.getUri());
    validateResourceEdge(outgoingEdgeIterator.next(), work, lookupResources.geographicCoverages().get(1),
      GEOGRAPHIC_COVERAGE.getUri());
    validateWorkContributor(outgoingEdgeIterator.next(), work, MEETING, CREATOR.getUri());
    validateWorkContributor(outgoingEdgeIterator.next(), work, MEETING, CONTRIBUTOR.getUri());
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

  private void validateWorkClassification(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(CLASSIFICATION.getUri());
    var classification = edge.getTarget();
    assertThat(classification.getDoc().size()).isEqualTo(2);
    assertThat(classification.getDoc().get(CODE.getValue()).size()).isEqualTo(1);
    assertThat(classification.getDoc().get(CODE.getValue()).get(0).asText()).isEqualTo("709.83");
    assertThat(classification.getDoc().get(SOURCE.getValue()).size()).isEqualTo(1);
    assertThat(classification.getDoc().get(SOURCE.getValue()).get(0).asText()).isEqualTo("ddc");
  }

  private void validateWorkContentType(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(CONTENT.getUri());
    var contentType = edge.getTarget();
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
    assertThat(contentType.getDoc().size()).isEqualTo(3);
    validateLiteral(contentType, CODE.getValue(), "b");
    validateLiteral(contentType, LINK.getValue(), "http://id.loc.gov/vocabulary/maudience/pri");
    validateLiteral(contentType, TERM.getValue(), "Primary");
    var resourceEdge = contentType.getOutgoingEdges().iterator().next();
    var categorySet = resourceEdge.getTarget();
    validateResourceEdge(resourceEdge, contentType, categorySet, IS_DEFINED_BY.getUri());
    assertThat(categorySet.getDoc().size()).isEqualTo(2);
    validateLiteral(categorySet, LINK.getValue(), "https://id.loc.gov/vocabulary/maudience");
    validateLiteral(categorySet, LABEL.getValue(), "Target audience");
    assertThat(categorySet.getLabel()).isEqualTo("Target audience");
  }

  private void validateWorkContributor(ResourceEdge edge, Resource source, ResourceTypeDictionary type,
                                       String predicateUri) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(predicateUri);
    var creator = edge.getTarget();
    var types = creator.getTypes().stream().map(ResourceTypeEntity::getUri).toList();
    assertThat(types).contains(type.getUri());
    assertThat(creator.getDoc().size()).isEqualTo(2);
    assertThat(creator.getDoc().get(NAME.getValue()).size()).isEqualTo(1);
    assertThat(creator.getDoc().get(NAME.getValue()).get(0).asText()).isEqualTo("name-" + type);
    assertThat(creator.getDoc().get(LCNAF_ID.getValue()).size()).isEqualTo(1);
    assertThat(creator.getDoc().get(LCNAF_ID.getValue()).get(0).asText()).isEqualTo("2002801801-" + type);
    assertThat(creator.getLabel()).isEqualTo("name-" + type);
  }

  private void validateWorkGovernmentPublication(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(GOVERNMENT_PUBLICATION.getUri());
    var governmentPublication = edge.getTarget();
    var types = governmentPublication.getTypes().stream().map(ResourceTypeEntity::getUri).toList();
    assertThat(types).contains(CATEGORY.getUri());
    assertThat(governmentPublication.getLabel()).isEqualTo("Autonomous");
    assertThat(governmentPublication.getDoc().size()).isEqualTo(3);
    validateLiterals(governmentPublication, CODE.getValue(), List.of("a"));
    validateLiterals(governmentPublication, TERM.getValue(), List.of("Autonomous"));
    validateLiterals(governmentPublication, LINK.getValue(), List.of("http://id.loc.gov/vocabulary/mgovtpubtype/a"));
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
    assertThat(copyrightEvent.getResourceHash()).isNotNull();
    assertThat(copyrightEvent.getDoc().size()).isEqualTo(1);
    assertThat(copyrightEvent.getDoc().get(DATE.getValue()).size()).isEqualTo(1);
    assertThat(copyrightEvent.getDoc().get(DATE.getValue()).get(0).asText()).isEqualTo("copyright date value");
    assertThat(copyrightEvent.getOutgoingEdges()).isEmpty();
  }

  private LookupResources saveLookupResources() {
    var subject1 = saveResource(1L, "subject 1", CONCEPT, "{}");
    var subject2 = saveResource(2L, "subject 2", CONCEPT, "{}");
    var unitedStates = saveResource(101L, "United States", PLACE, "{}");
    var europe = saveResource(102L, "Europe", PLACE, "{}");
    var genre1 = saveResource(201L, "genre 1", FORM, "{}");
    var genre2 = saveResource(202L, "genre 2", FORM, "{}");
    var creatorMeeting = saveResource(1001L, "name-MEETING", MEETING,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-MEETING\"], \"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-MEETING\"]}");
    var creatorPerson = saveResource(1002L, "name-PERSON", PERSON,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-PERSON\"], \"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-PERSON\"]}");
    var creatorOrganization = saveResource(1003L, "name-ORGANIZATION", ORGANIZATION,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-ORGANIZATION\"], \"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-ORGANIZATION\"]}");
    var creatorFamily = saveResource(1004L, "name-FAMILY", FAMILY,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-FAMILY\"], \"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-FAMILY\"]}");
    var contributorMeeting = saveResource(1008L, "name-MEETING", MEETING,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-MEETING\"], \"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-MEETING\"]}");
    var contributorPerson = saveResource(1007L, "name-PERSON", PERSON,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-PERSON\"], \"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-PERSON\"]}");
    var contributorOrganization = saveResource(1005L, "name-ORGANIZATION", ORGANIZATION,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-ORGANIZATION\"], \"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-ORGANIZATION\"]}");
    var contributorFamily = saveResource(1006L, "name-FAMILY", FAMILY,
      "{\"http://bibfra.me/vocab/lite/name\": [\"name-FAMILY\"], \"http://bibfra.me/vocab/marc/lcnafId\": [\"2002801801-FAMILY\"]}");
    return new LookupResources(
      List.of(subject1, subject2),
      List.of(unitedStates, europe),
      List.of(genre1, genre2),
      List.of(creatorMeeting, creatorPerson, creatorOrganization, creatorFamily,
        contributorPerson, contributorMeeting, contributorOrganization, contributorFamily)
    );
  }

  @SneakyThrows
  private Resource saveResource(Long id, String label, ResourceTypeDictionary type, String doc) {
    var resource = withInitializedSets();
    resource.addType(new ResourceTypeEntity().setHash(type.getHash()).setUri(type.getUri()));
    resource.setLabel(label);
    resource.setDoc(OBJECT_MAPPER.readTree(doc));
    resource.setResourceHash(id);
    return resourceTestService.saveGraph(resource);
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

  private String toWorkReference() {
    return join(".", toInstance(), arrayPath(WORK_REF));
  }

  private String toInventoryId() {
    return join(".", toInstance(), path("inventoryId"));
  }

  private String toSrsId() {
    return join(".", toInstance(), path("srsId"));
  }

  private String toExtent() {
    return String.join(".", toInstance(), arrayPath(EXTENT.getValue()));
  }

  private String toDimensions() {
    return join(".", toInstance(), arrayPath(DIMENSIONS.getValue()));
  }

  private String toEditionStatement() {
    return join(".", toInstance(), arrayPath(EDITION_STATEMENT.getValue()));
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

  private String toInstanceTitlePartName(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(PART_NAME.getValue()));
  }

  private String toInstanceTitlePartNumber(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(PART_NUMBER.getValue()));
  }

  private String toInstanceTitleMain(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(MAIN_TITLE.getValue()));
  }

  private String toInstanceTitleNonSortNum(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(NON_SORT_NUM.getValue()));
  }

  private String toInstanceTitleSubtitle(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(SUBTITLE.getValue()));
  }

  private String toIssuance() {
    return join(".", toInstance(), arrayPath(ISSUANCE.getValue()));
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

  private String toWorkResponsibilityStatement(String workBase) {
    return join(".", workBase, arrayPath(RESPONSIBILITY_STATEMENT.getValue()));
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

  private String toWorkLanguage(String workBase) {
    return join(".", workBase, arrayPath(LANGUAGE.getValue()));
  }

  private String toWorkDeweySource(String workBase) {
    return join(".", workBase, arrayPath(CLASSIFICATION.getUri()), arrayPath(SOURCE.getValue()));
  }

  private String toWorkDeweyCode(String workBase) {
    return join(".", workBase, arrayPath(CLASSIFICATION.getUri()), arrayPath(CODE.getValue()));
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

  private String toWorkGovernmentPublicationCode(String workBase) {
    return join(".", workBase, arrayPath(GOVERNMENT_PUBLICATION.getUri()), arrayPath(CODE.getValue()));
  }

  private String toWorkGovernmentPublicationTerm(String workBase) {
    return join(".", workBase, arrayPath(GOVERNMENT_PUBLICATION.getUri()), arrayPath(TERM.getValue()));
  }

  private String toWorkGovernmentPublicationLink(String workBase) {
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

  private String toErrorType() {
    return join(".", arrayPath("errors"), path("type"));
  }

  private String toErrorCode() {
    return join(".", arrayPath("errors"), path("code"));
  }

  private String toErrorMessage() {
    return join(".", arrayPath("errors"), path("message"));
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

  private record LookupResources(
    List<Resource> subjects,
    List<Resource> geographicCoverages,
    List<Resource> genres,
    List<Resource> creators
  ) {
  }
}
