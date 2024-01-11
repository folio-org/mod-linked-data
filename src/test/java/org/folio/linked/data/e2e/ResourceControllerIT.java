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
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.MEDIA;
import static org.folio.ld.dictionary.PredicateDictionary.PE_DISTRIBUTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_MANUFACTURE;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PRODUCTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.PredicateDictionary.STATUS;
import static org.folio.ld.dictionary.PredicateDictionary.SUPPLEMENTARY_CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.ADDITIONAL_PHYSICAL_FORM;
import static org.folio.ld.dictionary.PropertyDictionary.ASSIGNING_SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.BIBLIOGRAPHY_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.COMPUTER_DATA_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
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
import static org.folio.ld.dictionary.PropertyDictionary.TARGET_AUDIENCE;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.ld.dictionary.PropertyDictionary.TYPE_OF_REPORT;
import static org.folio.ld.dictionary.PropertyDictionary.VARIANT_TYPE;
import static org.folio.ld.dictionary.PropertyDictionary.WITH_NOTE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.COPYRIGHT_EVENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
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
import static org.folio.linked.data.model.ErrorCode.NOT_FOUND_ERROR;
import static org.folio.linked.data.model.ErrorCode.VALIDATION_ERROR;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getSampleBibframeDtoMap;
import static org.folio.linked.data.test.TestUtil.getSampleInstanceString;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;
import static org.folio.linked.data.util.Constants.TYPE;
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
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
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
import net.minidev.json.JSONArray;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.KafkaSender;
import org.folio.linked.data.test.ResourceEdgeRepository;
import org.folio.linked.data.test.TestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
public class ResourceControllerIT {

  private static final String BIBFRAME_URL = "/resource";
  private static final String ROLES_PROPERTY = "_roles";
  private static final String NOTES_PROPERTY = "_notes";
  private static final String VALUE_PROPERTY = "value";
  private static final String TYPE_PROPERTY = "type";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ResourceRepository resourceRepo;
  @Autowired
  private ResourceEdgeRepository resourceEdgeRepository;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private Environment env;
  @SpyBean
  private KafkaSender kafkaSender;
  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  public void clean() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "resource_edges", "resource_type_map", "resources");
  }

  @Test
  void createMonographInstanceBibframe_shouldSaveEntityCorrectly() throws Exception {
    // given
    var requestBuilder = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(getSampleInstanceString());

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = validateInstanceResourceResponse(resultActions)
      .andReturn().getResponse().getContentAsString();
    validateWorkResourceResponse(resultActions);

    var resourceResponse = objectMapper.readValue(response, ResourceDto.class);
    var id = ((InstanceField) resourceResponse.getResource()).getInstance().getId();
    var persistedOptional = resourceRepo.findById(Long.parseLong(id));
    assertThat(persistedOptional).isPresent();
    var bibframe = persistedOptional.get();
    validateInstance(bibframe);
    checkKafkaMessageCreatedSentAndMarkedAsIndexed(bibframe.getResourceHash());
  }

  @Test
  void createEmptyInstance_shouldSaveEmptyEntityAndNotSentIndexRequest() throws Exception {
    // given
    var requestBuilder = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(loadResourceAsString("samples/bibframe-empty.json"));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(toInstance(), notNullValue()))
      .andReturn().getResponse().getContentAsString();

    var resourceResponse = objectMapper.readValue(response, ResourceDto.class);
    var id = ((InstanceField) resourceResponse.getResource()).getInstance().getId();
    var persistedOptional = resourceRepo.findById(Long.parseLong(id));
    assertThat(persistedOptional).isPresent();
    var instance = persistedOptional.get();
    assertThat(instance.getResourceHash()).isNotNull();
    assertThat(instance.getLabel()).isEmpty();
    assertThat(instance.getTypes().iterator().next().getUri()).isEqualTo(INSTANCE.getUri());
    assertThat(instance.getInventoryId()).isNull();
    assertThat(instance.getSrsId()).isNull();
    assertThat(instance.getDoc()).isNull();
    assertThat(instance.getOutgoingEdges()).isEmpty();
    verify(kafkaSender, never()).sendResourceCreated(any(), eq(true));
  }

  @Test
  void createNoValuesInstance_shouldSaveEmptyEntityAndNotSentIndexRequest() throws Exception {
    // given
    var requestBuilder = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(loadResourceAsString("samples/bibframe-no-values.json"));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(toInstance(), notNullValue()))
      .andReturn().getResponse().getContentAsString();

    var resourceResponse = objectMapper.readValue(response, ResourceDto.class);
    var id = ((InstanceField) resourceResponse.getResource()).getInstance().getId();
    var persistedOptional = resourceRepo.findById(Long.parseLong(id));
    assertThat(persistedOptional).isPresent();
    var instance = persistedOptional.get();
    assertThat(instance.getResourceHash()).isNotNull();
    assertThat(instance.getLabel()).isEmpty();
    assertThat(instance.getTypes().iterator().next().getUri()).isEqualTo(INSTANCE.getUri());
    assertThat(instance.getInventoryId()).isNull();
    assertThat(instance.getSrsId()).isNull();
    assertThat(instance.getDoc()).isNull();
    assertThat(instance.getOutgoingEdges()).isEmpty();
    verify(kafkaSender, never()).sendResourceCreated(any(), eq(true));
  }

  @Test
  void createPartialValuesInstance_shouldSaveCorrectEntityAndSentIndexRequest() throws Exception {
    // given
    var requestBuilder = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(loadResourceAsString("samples/bibframe-partial-objects.json"));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(toInstance(), notNullValue()))
      .andReturn().getResponse().getContentAsString();

    var resourceResponse = objectMapper.readValue(response, ResourceDto.class);
    var id = ((InstanceField) resourceResponse.getResource()).getInstance().getId();
    var persistedOptional = resourceRepo.findById(Long.parseLong(id));
    assertThat(persistedOptional).isPresent();
    var instance = persistedOptional.get();
    assertThat(instance.getResourceHash()).isNotNull();
    assertThat(instance.getLabel()).isEmpty();
    assertThat(instance.getTypes().iterator().next().getUri()).isEqualTo(INSTANCE.getUri());
    assertThat(instance.getInventoryId()).isNull();
    assertThat(instance.getSrsId()).isNull();
    assertThat(instance.getDoc()).isNull();
    assertThat(instance.getOutgoingEdges()).hasSize(42);
    checkKafkaMessageCreatedSentAndMarkedAsIndexed(instance.getResourceHash());
  }

  @Test
  void createTwoMonographInstancesWithSharedResources_shouldSaveBothCorrectly() throws Exception {
    // given
    var requestBuilder1 = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(getSampleInstanceString());
    var resultActions1 = mockMvc.perform(requestBuilder1);
    var response1 = resultActions1.andReturn().getResponse().getContentAsString();
    var resourceResponse1 = objectMapper.readValue(response1, ResourceDto.class);
    var id1 = ((InstanceField) resourceResponse1.getResource()).getInstance().getId();
    var persistedOptional1 = resourceRepo.findById(Long.parseLong(id1));
    assertThat(persistedOptional1).isPresent();
    var requestBuilder2 = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(getSampleInstanceString().replace("Instance: partName", "Instance: partName2"));

    // when
    var response2 = mockMvc.perform(requestBuilder2);

    // then
    response2
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(toInstance(), notNullValue()));
  }

  @Test
  void createMonographInstanceWithNotCorrectStructure_shouldReturnValidationError() throws Exception {
    // given
    var wrongValue = "http://TitleWrong";
    var requestBuilder = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(getSampleInstanceString().replace("http://bibfra.me/vocab/marc/Title", wrongValue));

    // when
    var resultActions = mockMvc.perform(requestBuilder);


    // then
    resultActions.andExpect(status().is(UNPROCESSABLE_ENTITY.value()))
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors", notNullValue()))
      .andExpect(jsonPath("$." + toErrorType(), equalTo(HttpMessageNotReadableException.class.getSimpleName())))
      .andExpect(jsonPath("$." + toErrorCode(), equalTo(VALIDATION_ERROR.getValue())))
      .andExpect(jsonPath("$." + toErrorMessage(), equalTo("JSON parse error: InstanceAllOfTitleInner dto"
        + " class deserialization error: Unknown sub-element http://TitleWrong")));
  }

  @Test
  void update_shouldReturnCorrectlyUpdatedEntity() throws Exception {
    // given
    var originalInstance = resourceRepo.save(getSampleInstanceResource().setLabel("Instance: mainTitle"));
    var updateDto = getSampleBibframeDtoMap();
    var instanceMap = (LinkedHashMap) ((LinkedHashMap) updateDto.get("resource")).get(INSTANCE.getUri());
    instanceMap.put(DIMENSIONS.getValue(), List.of("200 m"));
    instanceMap.remove("inventoryId");
    instanceMap.remove("srsId");

    var updateRequest = put(BIBFRAME_URL + "/" + originalInstance.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(OBJECT_MAPPER.writeValueAsString(updateDto));

    // when
    var resultActions = mockMvc.perform(updateRequest);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(toInstance(), notNullValue()))
      .andReturn().getResponse().getContentAsString();
    var resourceResponse = objectMapper.readValue(response, ResourceDto.class);
    var id = ((InstanceField) resourceResponse.getResource()).getInstance().getId();
    var persistedOptional = resourceRepo.findById(Long.parseLong(id));
    assertThat(persistedOptional).isPresent();
    var updatedInstance = persistedOptional.get();
    assertThat(updatedInstance.getResourceHash()).isNotNull();
    assertThat(updatedInstance.getLabel()).isEqualTo(originalInstance.getLabel());
    assertThat(updatedInstance.getTypes().iterator().next().getUri()).isEqualTo(INSTANCE.getUri());
    assertThat(updatedInstance.getInventoryId()).isEqualTo(originalInstance.getInventoryId());
    assertThat(updatedInstance.getSrsId()).isEqualTo(originalInstance.getSrsId());
    assertThat(updatedInstance.getDoc().get(DIMENSIONS.getValue()).get(0).asText()).isEqualTo("200 m");
    assertThat(updatedInstance.getOutgoingEdges()).hasSize(originalInstance.getOutgoingEdges().size());
  }

  @Test
  void getBibframeById_shouldReturnExistedEntity() throws Exception {
    // given
    var existed = resourceRepo.save(getSampleInstanceResource());
    var requestBuilder = get(BIBFRAME_URL + "/" + existed.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    validateInstanceResourceResponse(resultActions);
    validateWorkResourceResponse(resultActions);
  }

  @Test
  void getBibframeById_shouldReturn404_ifNoExistedEntity() throws Exception {
    // given
    var notExistedId = randomLong();
    var requestBuilder = get(BIBFRAME_URL + "/" + notExistedId)
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
        resourceRepo.save(TestUtil.getSampleInstanceResource(1L, INSTANCE)),
        resourceRepo.save(TestUtil.getSampleInstanceResource(2L, INSTANCE)),
        resourceRepo.save(TestUtil.getSampleInstanceResource(3L, INSTANCE))
      ).stream()
      .map(Resource::getResourceHash)
      .map(Object::toString)
      .toList();
    var requestBuilder = get(BIBFRAME_URL)
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
  void deleteBibframeById_shouldDeleteRootResourceAndRootEdge() throws Exception {
    // given
    var existed = resourceRepo.save(getSampleInstanceResource());
    assertThat(resourceRepo.findById(existed.getResourceHash())).isPresent();
    assertThat(resourceRepo.count()).isEqualTo(31);
    assertThat(resourceEdgeRepository.count()).isEqualTo(37);
    var requestBuilder = delete(BIBFRAME_URL + "/" + existed.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    mockMvc.perform(requestBuilder);

    // then
    assertThat(resourceRepo.findById(existed.getResourceHash())).isNotPresent();
    assertThat(resourceRepo.count()).isEqualTo(30);
    assertThat(resourceEdgeRepository.findById(existed.getOutgoingEdges().iterator().next().getId())).isNotPresent();
    assertThat(resourceEdgeRepository.count()).isEqualTo(19);
    checkKafkaMessageDeletedSent(existed.getResourceHash());
  }

  @Test
  void updateResource_should_deleteExistedResource_createNewResource_sendRelevantKafkaMessages_whenUpdateSucceeded()
    throws Exception {
    //given
    var existedResource = resourceRepo.save(TestUtil.getSampleInstanceResource(1L, INSTANCE));
    var requestBuilder = put(BIBFRAME_URL + "/" + existedResource.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(loadResourceAsString("samples/bibframe-partial-objects.json"));

    //when
    var response = mockMvc.perform(requestBuilder).andReturn().getResponse().getContentAsString();

    //then
    assertFalse(resourceRepo.existsById(existedResource.getResourceHash()));
    var resourceResponse = objectMapper.readValue(response, ResourceDto.class);
    var updatedId = Long.valueOf(((InstanceField) resourceResponse.getResource()).getInstance().getId());
    assertTrue(resourceRepo.existsById(updatedId));
    checkKafkaMessageDeletedSent(existedResource.getResourceHash());
    checkKafkaMessageCreatedSentAndMarkedAsIndexed(updatedId);
  }

  @Test
  void updateResource_shouldNot_deleteExistedResource_createNewResource_sendRelevantKafkaMessages_whenUpdateFailed()
    throws Exception {
    //given
    var existedResource = resourceRepo.save(TestUtil.getSampleInstanceResource(1L, INSTANCE));
    var requestBuilder = put(BIBFRAME_URL + "/" + existedResource.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content("{\"resource\": {\"id\": null}}");

    //when
    mockMvc.perform(requestBuilder);

    //then
    assertTrue(resourceRepo.existsById(existedResource.getResourceHash()));
    verify(kafkaSender, never()).sendResourceDeleted(existedResource.getResourceHash());
    verify(kafkaSender, never()).sendResourceCreated(any(), eq(true));
  }

  protected void checkKafkaMessageCreatedSentAndMarkedAsIndexed(Long id) {
    // nothing to check without Folio profile
  }

  protected void checkKafkaMessageDeletedSent(Long id) {
    // nothing to check without Folio profile
  }

  @NotNull
  private ResultActions validateInstanceResourceResponse(ResultActions resultActions) throws Exception {
    return resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(toInstance(), notNullValue()))
      .andExpect(jsonPath(toInventoryId(), equalTo("2165ef4b-001f-46b3-a60e-52bcdeb3d5a1")))
      .andExpect(jsonPath(toSrsId(), equalTo("43d58061-decf-4d74-9747-0e1c368e861b")))
      .andExpect(jsonPath(toSupplementaryContentLink(), equalTo("supplementaryContent link")))
      .andExpect(jsonPath(toSupplementaryContentName(), equalTo("supplementaryContent name")))
      .andExpect(jsonPath(toAccessLocationLink(), equalTo("accessLocation value")))
      .andExpect(jsonPath(toAccessLocationNote(), equalTo("accessLocation note")))
      .andExpect(jsonPath(toCarrierCode(), equalTo("carrier code")))
      .andExpect(jsonPath(toCarrierLink(), equalTo("carrier link")))
      .andExpect(jsonPath(toCarrierTerm(), equalTo("carrier term")))
      .andExpect(jsonPath(toCopyrightDate(), equalTo("copyright date value")))
      .andExpect(jsonPath(toExtent(), equalTo("extent info")))
      .andExpect(jsonPath(toDimensions(), equalTo("20 cm")))
      .andExpect(jsonPath(toEanValue(), equalTo(new JSONArray().appendElement("ean value"))))
      .andExpect(jsonPath(toEanQualifier(), equalTo(new JSONArray().appendElement("ean qualifier"))))
      .andExpect(jsonPath(toEditionStatement(), equalTo("edition statement")))
      .andExpect(jsonPath(toInstanceTitlePartName(), equalTo(new JSONArray().appendElement("Instance: partName"))))
      .andExpect(jsonPath(toInstanceTitlePartNumber(), equalTo(new JSONArray().appendElement("Instance: partNumber"))))
      .andExpect(jsonPath(toInstanceTitleMain(), equalTo(new JSONArray().appendElement("Instance: mainTitle"))))
      .andExpect(jsonPath(toInstanceTitleNonSortNum(), equalTo(new JSONArray().appendElement("Instance: nonSortNum"))))
      .andExpect(jsonPath(toInstanceTitleSubtitle(), equalTo(new JSONArray().appendElement("Instance: subTitle"))))
      .andExpect(jsonPath(toIsbnValue(), equalTo(new JSONArray().appendElement("isbn value"))))
      .andExpect(jsonPath(toIsbnQualifier(), equalTo(new JSONArray().appendElement("isbn qualifier"))))
      .andExpect(jsonPath(toIsbnStatusValue(), equalTo(new JSONArray().appendElement("isbn status value"))))
      .andExpect(jsonPath(toIsbnStatusLink(), equalTo(new JSONArray().appendElement("isbn status link"))))
      .andExpect(jsonPath(toIssuance(), equalTo("single unit")))
      .andExpect(jsonPath(toInstanceNotesValues(), containsInAnyOrder("additional physical form", "computer data note",
        "description source note", "exhibitions note", "funding information", "issuance note", "issuing body",
        "location of other archival material", "note", "original version note", "related parts", "reproduction note",
        "type of report", "with note")))
      .andExpect(jsonPath(toInstanceNotesTypes(), containsInAnyOrder("http://bibfra.me/vocab/lite/note",
        "http://bibfra.me/vocab/marc/withNote", "http://bibfra.me/vocab/marc/typeOfReport",
        "http://bibfra.me/vocab/marc/issuanceNote", "http://bibfra.me/vocab/marc/computerDataNote",
        "http://bibfra.me/vocab/marc/additionalPhysicalForm", "http://bibfra.me/vocab/marc/reproductionNote",
        "http://bibfra.me/vocab/marc/originalVersionNote", "http://bibfra.me/vocab/marc/relatedParts",
        "http://bibfra.me/vocab/marc/issuingBody", "http://bibfra.me/vocab/marc/locationOfOtherArchivalMaterial",
        "http://bibfra.me/vocab/marc/exhibitionsNote", "http://bibfra.me/vocab/marc/descriptionSourceNote",
        "http://bibfra.me/vocab/marc/fundingInformation")))
      .andExpect(jsonPath(toLccnValue(), equalTo(new JSONArray().appendElement("lccn value"))))
      .andExpect(jsonPath(toLccnStatusValue(), equalTo(new JSONArray().appendElement("lccn status value"))))
      .andExpect(jsonPath(toLccnStatusLink(), equalTo(new JSONArray().appendElement("lccn status link"))))
      .andExpect(jsonPath(toLocalIdValue(), equalTo(new JSONArray().appendElement("localId value"))))
      .andExpect(jsonPath(toLocalIdAssigner(), equalTo(new JSONArray().appendElement("localId assigner"))))
      .andExpect(jsonPath(toMediaCode(), equalTo("media code")))
      .andExpect(jsonPath(toMediaLink(), equalTo("media link")))
      .andExpect(jsonPath(toMediaTerm(), equalTo("media term")))
      .andExpect(jsonPath(toOtherIdValue(), equalTo(new JSONArray().appendElement("otherId value"))))
      .andExpect(jsonPath(toOtherIdQualifier(), equalTo(new JSONArray().appendElement("otherId qualifier"))))
      .andExpect(jsonPath(toParallelTitlePartName(), equalTo(new JSONArray().appendElement("Parallel: partName"))))
      .andExpect(jsonPath(toParallelTitlePartNumber(), equalTo(new JSONArray().appendElement("Parallel: partNumber"))))
      .andExpect(jsonPath(toParallelTitleMain(), equalTo(new JSONArray().appendElement("Parallel: mainTitle"))))
      .andExpect(jsonPath(toParallelTitleNote(), equalTo(new JSONArray().appendElement("Parallel: noteLabel"))))
      .andExpect(jsonPath(toParallelTitleDate(), equalTo(new JSONArray().appendElement("Parallel: date"))))
      .andExpect(jsonPath(toParallelTitleSubtitle(), equalTo(new JSONArray().appendElement("Parallel: subTitle"))))
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
      .andExpect(jsonPath(toVariantTitlePartName(), equalTo(new JSONArray().appendElement("Variant: partName"))))
      .andExpect(jsonPath(toVariantTitlePartNumber(), equalTo(new JSONArray().appendElement("Variant: partNumber"))))
      .andExpect(jsonPath(toVariantTitleMain(), equalTo(new JSONArray().appendElement("Variant: mainTitle"))))
      .andExpect(jsonPath(toVariantTitleNote(), equalTo(new JSONArray().appendElement("Variant: noteLabel"))))
      .andExpect(jsonPath(toVariantTitleDate(), equalTo(new JSONArray().appendElement("Variant: date"))))
      .andExpect(jsonPath(toVariantTitleSubtitle(), equalTo(new JSONArray().appendElement("Variant: subTitle"))))
      .andExpect(jsonPath(toVariantTitleType(), equalTo(new JSONArray().appendElement("Variant: variantType"))));
  }

  @NotNull
  private ResultActions validateWorkResourceResponse(ResultActions resultActions) throws Exception {
    return resultActions
      .andExpect(status().isOk())
      .andExpect(jsonPath(toWorkTargetAudience(), equalTo("target audience")))
      .andExpect(jsonPath(toWorkLanguage(), equalTo("eng")))
      .andExpect(jsonPath(toWorkSummary(), equalTo("summary text")))
      .andExpect(jsonPath(toWorkTableOfContents(), equalTo("table of contents")))
      .andExpect(jsonPath(toWorkResponsibilityStatement(), equalTo("statement of responsibility")))
      .andExpect(jsonPath(toWorkNotesValues(), containsInAnyOrder("language note", "bibliography note", "note",
        "another note", "another note")))
      .andExpect(jsonPath(toWorkNotesTypes(), containsInAnyOrder("http://bibfra.me/vocab/marc/languageNote",
        "http://bibfra.me/vocab/marc/languageNote", "http://bibfra.me/vocab/marc/bibliographyNote",
        "http://bibfra.me/vocab/lite/note", "http://bibfra.me/vocab/lite/note")))
      .andExpect(jsonPath(toWorkDeweyCode(), equalTo("709.83")))
      .andExpect(jsonPath(toWorkDeweySource(), equalTo("ddc")))
      .andExpect(jsonPath(toWorkCreatorPersonName(), equalTo(new JSONArray().appendElement("name-PERSON"))))
      .andExpect(jsonPath(toWorkCreatorPersonRole(), equalTo(new JSONArray().appendElement(AUTHOR.getUri()))))
      .andExpect(jsonPath(toWorkCreatorPersonLcnafId(), equalTo(new JSONArray().appendElement("2002801801-PERSON"))))
      .andExpect(jsonPath(toWorkCreatorMeetingName(), equalTo(new JSONArray().appendElement("name-MEETING"))))
      .andExpect(jsonPath(toWorkCreatorMeetingLcnafId(), equalTo(new JSONArray().appendElement("2002801801-MEETING"))))
      .andExpect(jsonPath(toWorkCreatorOrganizationName(), equalTo(new JSONArray().appendElement("name-ORGANIZATION"))))
      .andExpect(jsonPath(toWorkCreatorOrganizationLcnafId(), equalTo(
        new JSONArray().appendElement("2002801801-ORGANIZATION"))))
      .andExpect(jsonPath(toWorkCreatorFamilyName(), equalTo(new JSONArray().appendElement("name-FAMILY"))))
      .andExpect(jsonPath(toWorkCreatorFamilyLcnafId(), equalTo(new JSONArray().appendElement("2002801801-FAMILY"))))
      .andExpect(jsonPath(toWorkContributorPersonName(), equalTo(new JSONArray().appendElement("name-PERSON"))))
      .andExpect(jsonPath(toWorkContributorPersonLcnafId(), equalTo(
        new JSONArray().appendElement("2002801801-PERSON"))))
      .andExpect(jsonPath(toWorkContributorMeetingName(), equalTo(new JSONArray().appendElement("name-MEETING"))))
      .andExpect(jsonPath(toWorkContributorMeetingLcnafId(), equalTo(
        new JSONArray().appendElement("2002801801-MEETING"))))
      .andExpect(jsonPath(toWorkContributorOrgName(), equalTo(new JSONArray().appendElement("name-ORGANIZATION"))))
      .andExpect(jsonPath(toWorkContributorOrgLcnafId(), equalTo(
        new JSONArray().appendElement("2002801801-ORGANIZATION"))))
      .andExpect(jsonPath(toWorkContributorOrgRoles(), equalTo(
        new JSONArray().appendElement(EDITOR.getUri()).appendElement(ASSIGNEE.getUri()))))
      .andExpect(jsonPath(toWorkContributorFamilyName(), equalTo(new JSONArray().appendElement("name-FAMILY"))))
      .andExpect(jsonPath(toWorkContributorFamilyLcnafId(), equalTo(
        new JSONArray().appendElement("2002801801-FAMILY"))))
      .andExpect(jsonPath(toWorkContentLink(), equalTo("http://id.loc.gov/vocabulary/contentTypes/txt")))
      .andExpect(jsonPath(toWorkContentCode(), equalTo("txt")))
      .andExpect(jsonPath(toWorkContentTerm(), equalTo("text")));
  }

  private void validateInstance(Resource instance) {
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
    validateWork(edgeIterator.next(), instance);
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

  private void validateWork(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(INSTANTIATES.getUri());
    var instantiates = edge.getTarget();
    assertThat(instantiates.getResourceHash()).isNotNull();
    assertThat(instantiates.getDoc().size()).isEqualTo(8);
    validateLiteral(instantiates, RESPONSIBILITY_STATEMENT.getValue(), "statement of responsibility");
    validateLiteral(instantiates, SUMMARY.getValue(), "summary text");
    validateLiteral(instantiates, LANGUAGE.getValue(), "eng");
    validateLiteral(instantiates, TARGET_AUDIENCE.getValue(), "target audience");
    validateLiteral(instantiates, TABLE_OF_CONTENTS.getValue(), "table of contents");
    validateLiteral(instantiates, BIBLIOGRAPHY_NOTE.getValue(), "bibliography note");
    validateLiterals(instantiates, LANGUAGE_NOTE.getValue(), List.of("language note", "another note"));
    validateLiterals(instantiates, NOTE.getValue(), List.of("note", "another note"));
    var edgeIterator = instantiates.getOutgoingEdges().iterator();
    validateWorkContentType(edgeIterator.next(), instantiates);
    validateWorkClassification(edgeIterator.next(), instantiates);
    validateWorkContributor(edgeIterator.next(), instantiates, ORGANIZATION, CREATOR.getUri());
    validateWorkContributor(edgeIterator.next(), instantiates, ORGANIZATION, EDITOR.getUri());
    validateWorkContributor(edgeIterator.next(), instantiates, ORGANIZATION, CONTRIBUTOR.getUri());
    validateWorkContributor(edgeIterator.next(), instantiates, ORGANIZATION, ASSIGNEE.getUri());
    validateWorkContributor(edgeIterator.next(), instantiates, FAMILY, CREATOR.getUri());
    validateWorkContributor(edgeIterator.next(), instantiates, FAMILY, CONTRIBUTOR.getUri());
    validateWorkContributor(edgeIterator.next(), instantiates, PERSON, AUTHOR.getUri());
    validateWorkContributor(edgeIterator.next(), instantiates, PERSON, CREATOR.getUri());
    validateWorkContributor(edgeIterator.next(), instantiates, PERSON, CONTRIBUTOR.getUri());
    validateWorkContributor(edgeIterator.next(), instantiates, MEETING, CREATOR.getUri());
    validateWorkContributor(edgeIterator.next(), instantiates, MEETING, CONTRIBUTOR.getUri());
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
    assertThat(contentType.getDoc().get(LINK.getValue()).size()).isEqualTo(1);
    assertThat(contentType.getDoc().get(LINK.getValue()).get(0).asText())
      .isEqualTo("http://id.loc.gov/vocabulary/contentTypes/txt");
    validateLiteral(contentType, CODE.getValue(), "txt");
    validateLiteral(contentType, TERM.getValue(), "text");
    validateLiteral(contentType, SOURCE.getValue(), "content source");
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

  private String toInstance() {
    return join(".", "$", path("resource"), path(INSTANCE.getUri()));
  }

  private String toInventoryId() {
    return join(".", toInstance(), path("inventoryId"));
  }

  private String toSrsId() {
    return join(".", toInstance(), path("srsId"));
  }

  private String toWork() {
    return join(".", toInstance(), arrayPath(INSTANTIATES.getUri()));
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

  private String toInstanceTitlePartName() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(PART_NAME.getValue()));
  }

  private String toInstanceTitlePartNumber() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(PART_NUMBER.getValue()));
  }

  private String toInstanceTitleMain() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(MAIN_TITLE.getValue()));
  }

  private String toInstanceTitleNonSortNum() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(NON_SORT_NUM.getValue()));
  }

  private String toInstanceTitleSubtitle() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()),
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

  private String toParallelTitlePartName() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(PART_NAME.getValue()));
  }

  private String toParallelTitlePartNumber() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(PART_NUMBER.getValue()));
  }

  private String toParallelTitleMain() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(MAIN_TITLE.getValue()));
  }

  private String toParallelTitleDate() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(DATE.getValue()));
  }

  private String toParallelTitleSubtitle() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(SUBTITLE.getValue()));
  }

  private String toParallelTitleNote() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(NOTE.getValue()));
  }

  private String toVariantTitlePartName() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(PART_NAME.getValue()));
  }

  private String toVariantTitlePartNumber() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(PART_NUMBER.getValue()));
  }

  private String toVariantTitleMain() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(MAIN_TITLE.getValue()));
  }

  private String toVariantTitleDate() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(DATE.getValue()));
  }

  private String toVariantTitleSubtitle() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(SUBTITLE.getValue()));
  }

  private String toVariantTitleType() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(VARIANT_TYPE.getValue()));
  }

  private String toVariantTitleNote() {
    return join(".", toInstance(), dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
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

  private String toCarrierCode() {
    return join(".", toInstance(), arrayPath(CARRIER.getUri()), arrayPath(CODE.getValue()));
  }

  private String toCarrierLink() {
    return join(".", toInstance(), arrayPath(CARRIER.getUri()), arrayPath(LINK.getValue()));
  }

  private String toCarrierTerm() {
    return join(".", toInstance(), arrayPath(CARRIER.getUri()), arrayPath(TERM.getValue()));
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

  private String toWorkTargetAudience() {
    return join(".", toWork(), arrayPath(TARGET_AUDIENCE.getValue()));
  }

  private String toWorkResponsibilityStatement() {
    return join(".", toWork(), arrayPath(RESPONSIBILITY_STATEMENT.getValue()));
  }

  private String toWorkNotesValues() {
    return join(".", toWork(), dynamicArrayPath(NOTES_PROPERTY), arrayPath(VALUE_PROPERTY));
  }

  private String toWorkNotesTypes() {
    return join(".", toWork(), dynamicArrayPath(NOTES_PROPERTY), arrayPath(TYPE_PROPERTY));
  }

  private String toWorkTableOfContents() {
    return join(".", toWork(), arrayPath(TABLE_OF_CONTENTS.getValue()));
  }

  private String toWorkSummary() {
    return join(".", toWork(), arrayPath(SUMMARY.getValue()));
  }

  private String toWorkLanguage() {
    return join(".", toWork(), arrayPath(LANGUAGE.getValue()));
  }

  private String toWorkDeweySource() {
    return join(".", toWork(), arrayPath(CLASSIFICATION.getUri()), arrayPath(SOURCE.getValue()));
  }

  private String toWorkDeweyCode() {
    return join(".", toWork(), arrayPath(CLASSIFICATION.getUri()), arrayPath(CODE.getValue()));
  }

  private String toWorkContributorOrgLcnafId() {
    return join(".", toWork(), dynamicArrayPath(CONTRIBUTOR.getUri()), path(ORGANIZATION.getUri()),
      arrayPath(LCNAF_ID.getValue()));
  }

  private String toWorkContributorPersonLcnafId() {
    return join(".", toWork(), dynamicArrayPath(CONTRIBUTOR.getUri()), path(PERSON.getUri()),
      arrayPath(LCNAF_ID.getValue()));
  }

  private String toWorkContributorMeetingLcnafId() {
    return join(".", toWork(), dynamicArrayPath(CONTRIBUTOR.getUri()), path(MEETING.getUri()),
      arrayPath(LCNAF_ID.getValue()));
  }

  private String toWorkContributorFamilyLcnafId() {
    return join(".", toWork(), dynamicArrayPath(CONTRIBUTOR.getUri()), path(FAMILY.getUri()),
      arrayPath(LCNAF_ID.getValue()));
  }

  private String toWorkContributorOrgName() {
    return join(".", toWork(), dynamicArrayPath(CONTRIBUTOR.getUri()), path(ORGANIZATION.getUri()),
      arrayPath(NAME.getValue()));
  }

  private String toWorkContributorOrgRoles() {
    return join(".", toWork(), dynamicArrayPath(CONTRIBUTOR.getUri()), path(ORGANIZATION.getUri()),
      dynamicArrayPath(ROLES_PROPERTY));
  }

  private String toWorkContributorPersonName() {
    return join(".", toWork(), dynamicArrayPath(CONTRIBUTOR.getUri()), path(PERSON.getUri()),
      arrayPath(NAME.getValue()));
  }

  private String toWorkContributorFamilyName() {
    return join(".", toWork(), dynamicArrayPath(CONTRIBUTOR.getUri()), path(FAMILY.getUri()),
      arrayPath(NAME.getValue()));
  }

  private String toWorkContributorMeetingName() {
    return join(".", toWork(), dynamicArrayPath(CONTRIBUTOR.getUri()), path(MEETING.getUri()),
      arrayPath(NAME.getValue()));
  }

  private String toWorkCreatorPersonLcnafId() {
    return join(".", toWork(), dynamicArrayPath(CREATOR.getUri()), path(PERSON.getUri()),
      arrayPath(LCNAF_ID.getValue()));
  }

  private String toWorkCreatorMeetingLcnafId() {
    return join(".", toWork(), dynamicArrayPath(CREATOR.getUri()), path(MEETING.getUri()),
      arrayPath(LCNAF_ID.getValue()));
  }

  private String toWorkCreatorOrganizationLcnafId() {
    return join(".", toWork(), dynamicArrayPath(CREATOR.getUri()), path(ORGANIZATION.getUri()),
      arrayPath(LCNAF_ID.getValue()));
  }

  private String toWorkCreatorFamilyLcnafId() {
    return join(".", toWork(), dynamicArrayPath(CREATOR.getUri()), path(FAMILY.getUri()),
      arrayPath(LCNAF_ID.getValue()));
  }

  private String toWorkCreatorPersonName() {
    return join(".", toWork(), dynamicArrayPath(CREATOR.getUri()), path(PERSON.getUri()), arrayPath(NAME.getValue()));
  }

  private String toWorkCreatorPersonRole() {
    return join(".", toWork(), dynamicArrayPath(CREATOR.getUri()), path(PERSON.getUri()), arrayPath(ROLES_PROPERTY));
  }

  private String toWorkCreatorMeetingName() {
    return join(".", toWork(), dynamicArrayPath(CREATOR.getUri()), path(MEETING.getUri()), arrayPath(NAME.getValue()));
  }

  private String toWorkCreatorOrganizationName() {
    return join(".", toWork(), dynamicArrayPath(CREATOR.getUri()), path(ORGANIZATION.getUri()),
      arrayPath(NAME.getValue()));
  }

  private String toWorkCreatorFamilyName() {
    return join(".", toWork(), dynamicArrayPath(CREATOR.getUri()), path(FAMILY.getUri()), arrayPath(NAME.getValue()));
  }

  private String toWorkContentTerm() {
    return join(".", toWork(), arrayPath(CONTENT.getUri()), arrayPath(TERM.getValue()));
  }

  private String toWorkContentCode() {
    return join(".", toWork(), arrayPath(CONTENT.getUri()), arrayPath(CODE.getValue()));
  }

  private String toWorkContentLink() {
    return join(".", toWork(), arrayPath(CONTENT.getUri()), arrayPath(LINK.getValue()));
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
}
