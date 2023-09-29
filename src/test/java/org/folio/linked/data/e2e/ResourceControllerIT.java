package org.folio.linked.data.e2e;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.model.ErrorCode.NOT_FOUND_ERROR;
import static org.folio.linked.data.model.ErrorCode.VALIDATION_ERROR;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.bibframeSampleResource;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getBibframeSample;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.BibframeConstants.ACCESS_LOCATION;
import static org.folio.linked.data.util.BibframeConstants.ACCESS_LOCATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.ASSIGNING_SOURCE;
import static org.folio.linked.data.util.BibframeConstants.CARRIER_PRED;
import static org.folio.linked.data.util.BibframeConstants.CATEGORY;
import static org.folio.linked.data.util.BibframeConstants.CODE;
import static org.folio.linked.data.util.BibframeConstants.COPYRIGHT_EVENT;
import static org.folio.linked.data.util.BibframeConstants.COPYRIGHT_PRED;
import static org.folio.linked.data.util.BibframeConstants.DATE;
import static org.folio.linked.data.util.BibframeConstants.DIMENSIONS;
import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.EAN;
import static org.folio.linked.data.util.BibframeConstants.EAN_VALUE;
import static org.folio.linked.data.util.BibframeConstants.EDITION_STATEMENT;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ISBN;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE;
import static org.folio.linked.data.util.BibframeConstants.LABEL;
import static org.folio.linked.data.util.BibframeConstants.LCCN;
import static org.folio.linked.data.util.BibframeConstants.LINK;
import static org.folio.linked.data.util.BibframeConstants.LOCAL_ID;
import static org.folio.linked.data.util.BibframeConstants.LOCAL_ID_VALUE;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE;
import static org.folio.linked.data.util.BibframeConstants.MANUFACTURE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAP_PRED;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;
import static org.folio.linked.data.util.BibframeConstants.NAME;
import static org.folio.linked.data.util.BibframeConstants.NON_SORT_NUM;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.OTHER_ID;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeConstants.PART_NAME;
import static org.folio.linked.data.util.BibframeConstants.PART_NUMBER;
import static org.folio.linked.data.util.BibframeConstants.PLACE;
import static org.folio.linked.data.util.BibframeConstants.PRODUCTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROJECTED_PROVISION_DATE;
import static org.folio.linked.data.util.BibframeConstants.PROVIDER_EVENT;
import static org.folio.linked.data.util.BibframeConstants.PROVIDER_PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.QUALIFIER;
import static org.folio.linked.data.util.BibframeConstants.RESPONSIBILITY_STATEMENT;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_DATE;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_PLACE;
import static org.folio.linked.data.util.BibframeConstants.STATUS;
import static org.folio.linked.data.util.BibframeConstants.STATUS_PRED;
import static org.folio.linked.data.util.BibframeConstants.SUBTITLE;
import static org.folio.linked.data.util.BibframeConstants.TERM;
import static org.folio.linked.data.util.BibframeConstants.TYPE;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TYPE;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.MonographTestService;
import org.folio.linked.data.test.ResourceEdgeRepository;
import org.folio.spring.test.extension.impl.OkapiConfiguration;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
public class ResourceControllerIT {

  public static final String BIBFRAME_URL = "/resource";
  public static OkapiConfiguration okapi;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ResourceRepository resourceRepo;
  @Autowired
  private ResourceEdgeRepository resourceEdgeRepository;
  @Autowired
  private MonographTestService monographTestService;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private Environment env;

  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @AfterEach
  public void clean() {
    resourceEdgeRepository.deleteAll();
    resourceRepo.deleteAll();
  }

  @Test
  void createMonographInstanceBibframe_shouldSaveEntityCorrectly() throws Exception {
    // given
    var requestBuilder = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getBibframeSample());

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = validateResourceResponse(resultActions)
      .andReturn().getResponse().getContentAsString();

    var resourceResponse = objectMapper.readValue(response, ResourceDto.class);
    var id = ((InstanceField) resourceResponse.getResource()).getInstance().getId();
    var persistedOptional = resourceRepo.findById(Long.parseLong(id));
    assertThat(persistedOptional).isPresent();
    var bibframe = persistedOptional.get();
    validateMonographInstanceResource(bibframe);
    checkKafkaMessageSent(bibframe, null);
  }

  @Test
  void createTwoMonographInstancesWithSharedResources_shouldSaveBothCorrectly() throws Exception {
    // given
    var requestBuilder1 = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getBibframeSample());
    var resultActions1 = mockMvc.perform(requestBuilder1);
    var response1 = resultActions1.andReturn().getResponse().getContentAsString();
    var resourceResponse1 = objectMapper.readValue(response1, ResourceDto.class);
    var id1 = ((InstanceField) resourceResponse1.getResource()).getInstance().getId();
    var persistedOptional1 = resourceRepo.findById(Long.parseLong(id1));
    assertThat(persistedOptional1).isPresent();
    var requestBuilder2 = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getBibframeSample().replace("Instance: partName", "Instance: partName2"));

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
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getBibframeSample().replace("http://bibfra.me/vocab/marc/Title", wrongValue));

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
  void getBibframeById_shouldReturnExistedEntity() throws Exception {
    // given
    var existed = resourceRepo.save(monographTestService.createSampleInstance());
    var requestBuilder = get(BIBFRAME_URL + "/" + existed.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    validateResourceResponse(resultActions);
  }

  @Test
  void getBibframeById_shouldReturn404_ifNoExistedEntity() throws Exception {
    // given
    var notExistedId = randomLong();
    var requestBuilder = get(BIBFRAME_URL + "/" + notExistedId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()));

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
  void getBibframe2ShortInfoPage_shouldReturnPageWithExistedEntities() throws Exception {
    // given
    var existed = Lists.newArrayList(
      resourceRepo.save(bibframeSampleResource(1L, monographTestService.getInstanceType())),
      resourceRepo.save(bibframeSampleResource(2L, monographTestService.getInstanceType())),
      resourceRepo.save(bibframeSampleResource(3L, monographTestService.getInstanceType()))
    ).stream().sorted(comparing(Resource::getResourceHash)).toList();
    var requestBuilder = get(BIBFRAME_URL)
      .param(TYPE, monographTestService.getInstanceType().getTypeUri())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()));

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
      .andExpect(jsonPath("content[0].id", equalTo(existed.get(0).getResourceHash().toString())))
      .andExpect(jsonPath("content[1].id", equalTo(existed.get(1).getResourceHash().toString())))
      .andExpect(jsonPath("content[2].id", equalTo(existed.get(2).getResourceHash().toString())));
  }

  @Test
  void deleteBibframeById_shouldDeleteRootResourceAndRootEdge() throws Exception {
    // given
    var existed = resourceRepo.save(monographTestService.createSampleInstance());
    assertThat(resourceRepo.findById(existed.getResourceHash())).isPresent();
    assertThat(resourceRepo.count()).isEqualTo(23);
    assertThat(resourceEdgeRepository.count()).isEqualTo(22);
    var requestBuilder = delete(BIBFRAME_URL + "/" + existed.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()));

    // when
    mockMvc.perform(requestBuilder);

    // then
    assertThat(resourceRepo.findById(existed.getResourceHash())).isNotPresent();
    assertThat(resourceRepo.count()).isEqualTo(22);
    assertThat(resourceEdgeRepository.findById(existed.getOutgoingEdges().iterator().next().getId())).isNotPresent();
    assertThat(resourceEdgeRepository.count()).isEqualTo(6);
    checkKafkaMessageSent(null, existed.getResourceHash());
  }

  protected void checkKafkaMessageSent(Resource persisted, Long deleted) {
    // nothing to check without Folio profile
  }

  @NotNull
  private ResultActions validateResourceResponse(ResultActions resultActions) throws Exception {
    return resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(toInstance(), notNullValue()))
      .andExpect(jsonPath(toAccessLocationLink(), equalTo("accessLocation value")))
      .andExpect(jsonPath(toAccessLocationNote(), equalTo("accessLocation note")))
      .andExpect(jsonPath(toCarrierCode(), equalTo("carrier code")))
      .andExpect(jsonPath(toCarrierLink(), equalTo("carrier link")))
      .andExpect(jsonPath(toCarrierTerm(), equalTo("carrier term")))
      .andExpect(jsonPath(toCopyrightDate(), equalTo("copyright date value")))
      .andExpect(jsonPath(toDimensions(), equalTo("20 cm")))
      .andExpect(jsonPath(toEanValue(), equalTo("ean value")))
      .andExpect(jsonPath(toEanQualifier(), equalTo("ean qualifier")))
      .andExpect(jsonPath(toEditionStatement(), equalTo("edition statement")))
      .andExpect(jsonPath(toInstanceTitlePartName(), equalTo("Instance: partName")))
      .andExpect(jsonPath(toInstanceTitlePartNumber(), equalTo("Instance: partNumber")))
      .andExpect(jsonPath(toInstanceTitleMain(), equalTo("Instance: mainTitle")))
      .andExpect(jsonPath(toInstanceTitleNonSortNum(), equalTo("Instance: nonSortNum")))
      .andExpect(jsonPath(toInstanceTitleSubtitle(), equalTo("Instance: subtitle")))
      .andExpect(jsonPath(toIsbnValue(), equalTo("isbn value")))
      .andExpect(jsonPath(toIsbnQualifier(), equalTo("isbn qualifier")))
      .andExpect(jsonPath(toIsbnStatusValue(), equalTo("isbn status value")))
      .andExpect(jsonPath(toIsbnStatusLink(), equalTo("isbn status link")))
      .andExpect(jsonPath(toIssuance(), equalTo("single unit")))
      .andExpect(jsonPath(toLccnValue(), equalTo("lccn value")))
      .andExpect(jsonPath(toLccnStatusValue(), equalTo("lccn status value")))
      .andExpect(jsonPath(toLccnStatusLink(), equalTo("lccn status link")))
      .andExpect(jsonPath(toLocalIdValue(), equalTo("localId value")))
      .andExpect(jsonPath(toLocalIdAssigner(), equalTo("localId assigner")))
      .andExpect(jsonPath(toMediaCode(), equalTo("media code")))
      .andExpect(jsonPath(toMediaLink(), equalTo("media link")))
      .andExpect(jsonPath(toMediaTerm(), equalTo("media term")))
      .andExpect(jsonPath(toOtherIdValue(), equalTo("otherId value")))
      .andExpect(jsonPath(toOtherIdQualifier(), equalTo("otherId qualifier")))
      .andExpect(jsonPath(toParallelTitlePartName(), equalTo("Parallel: partName")))
      .andExpect(jsonPath(toParallelTitlePartNumber(), equalTo("Parallel: partNumber")))
      .andExpect(jsonPath(toParallelTitleMain(), equalTo("Parallel: mainTitle")))
      .andExpect(jsonPath(toParallelTitleNote(), equalTo("Parallel: noteLabel")))
      .andExpect(jsonPath(toParallelTitleDate(), equalTo("Parallel: date")))
      .andExpect(jsonPath(toParallelTitleSubtitle(), equalTo("Parallel: subtitle")))
      .andExpect(jsonPath(toProviderEventDate(PRODUCTION_PRED), equalTo("production date")))
      .andExpect(jsonPath(toProviderEventName(PRODUCTION_PRED), equalTo("production name")))
      .andExpect(jsonPath(toProviderEventPlaceName(PRODUCTION_PRED), equalTo("production providerPlace name")))
      .andExpect(jsonPath(toProviderEventPlaceLink(PRODUCTION_PRED), equalTo("production providerPlace link")))
      .andExpect(jsonPath(toProviderEventSimpleDate(PRODUCTION_PRED), equalTo("production simple date")))
      .andExpect(jsonPath(toProviderEventSimplePlace(PRODUCTION_PRED), equalTo("production simple place")))
      .andExpect(jsonPath(toProviderEventDate(PUBLICATION_PRED), equalTo("publication date")))
      .andExpect(jsonPath(toProviderEventName(PUBLICATION_PRED), equalTo("publication name")))
      .andExpect(jsonPath(toProviderEventPlaceName(PUBLICATION_PRED), equalTo("publication providerPlace name")))
      .andExpect(jsonPath(toProviderEventPlaceLink(PUBLICATION_PRED), equalTo("publication providerPlace link")))
      .andExpect(jsonPath(toProviderEventSimpleDate(PUBLICATION_PRED), equalTo("publication simple date")))
      .andExpect(jsonPath(toProviderEventSimplePlace(PUBLICATION_PRED), equalTo("publication simple place")))
      .andExpect(jsonPath(toProviderEventDate(DISTRIBUTION_PRED), equalTo("distribution date")))
      .andExpect(jsonPath(toProviderEventName(DISTRIBUTION_PRED), equalTo("distribution name")))
      .andExpect(jsonPath(toProviderEventPlaceName(DISTRIBUTION_PRED), equalTo("distribution providerPlace name")))
      .andExpect(jsonPath(toProviderEventPlaceLink(DISTRIBUTION_PRED), equalTo("distribution providerPlace link")))
      .andExpect(jsonPath(toProviderEventSimpleDate(DISTRIBUTION_PRED), equalTo("distribution simple date")))
      .andExpect(jsonPath(toProviderEventSimplePlace(DISTRIBUTION_PRED), equalTo("distribution simple place")))
      .andExpect(jsonPath(toProviderEventDate(MANUFACTURE_PRED), equalTo("manufacture date")))
      .andExpect(jsonPath(toProviderEventName(MANUFACTURE_PRED), equalTo("manufacture name")))
      .andExpect(jsonPath(toProviderEventPlaceName(MANUFACTURE_PRED), equalTo("manufacture providerPlace name")))
      .andExpect(jsonPath(toProviderEventPlaceLink(MANUFACTURE_PRED), equalTo("manufacture providerPlace link")))
      .andExpect(jsonPath(toProviderEventSimpleDate(MANUFACTURE_PRED), equalTo("manufacture simple date")))
      .andExpect(jsonPath(toProviderEventSimplePlace(MANUFACTURE_PRED), equalTo("manufacture simple place")))
      .andExpect(jsonPath(toProjectedProvisionDate(), equalTo("projected provision date")))
      .andExpect(jsonPath(toResponsibilityStatement(), equalTo("responsibility statement")))
      .andExpect(jsonPath(toVariantTitlePartName(), equalTo("Variant: partName")))
      .andExpect(jsonPath(toVariantTitlePartNumber(), equalTo("Variant: partNumber")))
      .andExpect(jsonPath(toVariantTitleMain(), equalTo("Variant: mainTitle")))
      .andExpect(jsonPath(toVariantTitleNote(), equalTo("Variant: noteLabel")))
      .andExpect(jsonPath(toVariantTitleDate(), equalTo("Variant: date")))
      .andExpect(jsonPath(toVariantTitleSubtitle(), equalTo("Variant: subtitle")))
      .andExpect(jsonPath(toVariantTitleType(), equalTo("Variant: variantType")));
  }

  private void validateMonographInstanceResource(Resource resource) {
    assertThat(resource.getFirstType().getTypeUri()).isEqualTo(INSTANCE);
    validateInstance(resource);
  }

  private void validateInstance(Resource instance) {
    assertThat(instance.getResourceHash()).isNotNull();
    assertThat(instance.getLabel()).isEqualTo("Instance: mainTitle");
    assertThat(instance.getFirstType().getTypeUri()).isEqualTo(INSTANCE);
    assertThat(instance.getResourceHash()).isNotNull();
    assertThat(instance.getDoc().size()).isEqualTo(5);
    validateLiteral(instance, DIMENSIONS, "20 cm");
    validateLiteral(instance, EDITION_STATEMENT, "edition statement");
    validateLiteral(instance, RESPONSIBILITY_STATEMENT, "responsibility statement");
    validateLiteral(instance, PROJECTED_PROVISION_DATE, "projected provision date");
    validateLiteral(instance, ISSUANCE, "single unit");
    assertThat(instance.getOutgoingEdges()).hasSize(16);

    var edgeIterator = instance.getOutgoingEdges().iterator();
    validateInstanceTitle(edgeIterator.next(), instance);
    validateParallelTitle(edgeIterator.next(), instance);
    validateVariantTitle(edgeIterator.next(), instance);
    validateProviderEvent(edgeIterator.next(), instance, PRODUCTION_PRED);
    validateProviderEvent(edgeIterator.next(), instance, PUBLICATION_PRED);
    validateProviderEvent(edgeIterator.next(), instance, DISTRIBUTION_PRED);
    validateProviderEvent(edgeIterator.next(), instance, MANUFACTURE_PRED);
    validateAccessLocation(edgeIterator.next(), instance);
    validateLccn(edgeIterator.next(), instance);
    validateIsbn(edgeIterator.next(), instance);
    validateEan(edgeIterator.next(), instance);
    validateLocalId(edgeIterator.next(), instance);
    validateOtherId(edgeIterator.next(), instance);
    validateCategory(edgeIterator.next(), instance, MEDIA_PRED, CATEGORY);
    validateCategory(edgeIterator.next(), instance, CARRIER_PRED, CATEGORY);
    validateCopyrightDate(edgeIterator.next(), instance);
    assertThat(edgeIterator.hasNext()).isFalse();
  }

  private void validateLiteral(Resource resource, String field, String value) {
    assertThat(resource.getDoc().get(field).size()).isEqualTo(1);
    assertThat(resource.getDoc().get(field).get(0).asText()).isEqualTo(value);
  }

  private void validateInstanceTitle(ResourceEdge edge, Resource source) {
    validateSampleTitleBase(edge, source, INSTANCE_TITLE, "Instance: ");
    var title = edge.getTarget();
    assertThat(title.getDoc().size()).isEqualTo(5);
    assertThat(title.getDoc().get(NON_SORT_NUM).size()).isEqualTo(1);
    assertThat(title.getDoc().get(NON_SORT_NUM).get(0).asText()).isEqualTo("Instance: nonSortNum");
    assertThat(title.getOutgoingEdges()).isEmpty();
  }

  private void validateParallelTitle(ResourceEdge edge, Resource source) {
    validateSampleTitleBase(edge, source, PARALLEL_TITLE, "Parallel: ");
    var title = edge.getTarget();
    assertThat(title.getDoc().size()).isEqualTo(6);
    assertThat(title.getDoc().get(DATE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(DATE).get(0).asText()).isEqualTo("Parallel: date");
    assertThat(title.getDoc().get(NOTE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(NOTE).get(0).asText()).isEqualTo("Parallel: noteLabel");
    assertThat(title.getOutgoingEdges()).isEmpty();
  }

  private void validateVariantTitle(ResourceEdge edge, Resource source) {
    validateSampleTitleBase(edge, source, VARIANT_TITLE, "Variant: ");
    var title = edge.getTarget();
    assertThat(title.getDoc().size()).isEqualTo(7);
    assertThat(title.getDoc().get(DATE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(DATE).get(0).asText()).isEqualTo("Variant: date");
    assertThat(title.getDoc().get(VARIANT_TYPE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(VARIANT_TYPE).get(0).asText()).isEqualTo("Variant: variantType");
    assertThat(title.getDoc().get(NOTE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(NOTE).get(0).asText()).isEqualTo("Variant: noteLabel");
    assertThat(title.getOutgoingEdges()).isEmpty();
  }

  private void validateSampleTitleBase(ResourceEdge edge, Resource source, String type, String prefix) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(INSTANCE_TITLE_PRED);
    var title = edge.getTarget();
    assertThat(title.getLabel()).isEqualTo(prefix + "mainTitle");
    assertThat(title.getFirstType().getTypeUri()).isEqualTo(type);
    assertThat(title.getResourceHash()).isNotNull();
    assertThat(title.getDoc().get(PART_NAME).size()).isEqualTo(1);
    assertThat(title.getDoc().get(PART_NAME).get(0).asText()).isEqualTo(prefix + "partName");
    assertThat(title.getDoc().get(PART_NUMBER).size()).isEqualTo(1);
    assertThat(title.getDoc().get(PART_NUMBER).get(0).asText()).isEqualTo(prefix + "partNumber");
    assertThat(title.getDoc().get(MAIN_TITLE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(MAIN_TITLE).get(0).asText()).isEqualTo(prefix + "mainTitle");
    assertThat(title.getDoc().get(SUBTITLE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(SUBTITLE).get(0).asText()).isEqualTo(prefix + "subtitle");
  }

  private void validateProviderEvent(ResourceEdge edge, Resource source, String predicate) {
    var type = predicate.substring(predicate.indexOf("marc/") + 5);
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(predicate);
    var providerEvent = edge.getTarget();
    assertThat(providerEvent.getLabel()).isEqualTo(type + " name");
    assertThat(providerEvent.getFirstType().getTypeUri()).isEqualTo(PROVIDER_EVENT);
    assertThat(providerEvent.getResourceHash()).isNotNull();
    assertThat(providerEvent.getDoc().size()).isEqualTo(4);
    assertThat(providerEvent.getDoc().get(DATE).size()).isEqualTo(1);
    assertThat(providerEvent.getDoc().get(DATE).get(0).asText()).isEqualTo(type + " date");
    assertThat(providerEvent.getDoc().get(NAME).size()).isEqualTo(1);
    assertThat(providerEvent.getDoc().get(NAME).get(0).asText()).isEqualTo(type + " name");
    assertThat(providerEvent.getDoc().get(SIMPLE_DATE).size()).isEqualTo(1);
    assertThat(providerEvent.getDoc().get(SIMPLE_DATE).get(0).asText()).isEqualTo(type + " simple date");
    assertThat(providerEvent.getDoc().get(SIMPLE_PLACE).size()).isEqualTo(1);
    assertThat(providerEvent.getDoc().get(SIMPLE_PLACE).get(0).asText()).isEqualTo(type + " simple place");
    assertThat(providerEvent.getOutgoingEdges()).hasSize(1);
    validateProviderPlace(providerEvent.getOutgoingEdges().iterator().next(), providerEvent, type);
  }

  private void validateProviderPlace(ResourceEdge edge, Resource source, String prefix) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(PROVIDER_PLACE_PRED);
    var place = edge.getTarget();
    assertThat(place.getLabel()).isEqualTo(prefix + " providerPlace name");
    assertThat(place.getFirstType().getTypeUri()).isEqualTo(PLACE);
    assertThat(place.getResourceHash()).isNotNull();
    assertThat(place.getDoc().size()).isEqualTo(2);
    assertThat(place.getDoc().get(NAME).size()).isEqualTo(1);
    assertThat(place.getDoc().get(NAME).get(0).asText()).isEqualTo(prefix + " providerPlace name");
    assertThat(place.getDoc().get(LINK).size()).isEqualTo(1);
    assertThat(place.getDoc().get(LINK).get(0).asText()).isEqualTo(prefix + " providerPlace link");
    assertThat(place.getOutgoingEdges()).isEmpty();
  }

  private void validateLccn(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(MAP_PRED);
    var lccn = edge.getTarget();
    assertThat(lccn.getLabel()).isEqualTo("lccn value");
    assertThat(lccn.getFirstType().getTypeUri()).isEqualTo(LCCN);
    assertThat(lccn.getResourceHash()).isNotNull();
    assertThat(lccn.getDoc().size()).isEqualTo(1);
    assertThat(lccn.getDoc().get(NAME).size()).isEqualTo(1);
    assertThat(lccn.getDoc().get(NAME).get(0).asText()).isEqualTo("lccn value");
    assertThat(lccn.getOutgoingEdges()).hasSize(1);
    validateStatus(lccn.getOutgoingEdges().iterator().next(), lccn, "lccn");
  }

  private void validateIsbn(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(MAP_PRED);
    var isbn = edge.getTarget();
    assertThat(isbn.getLabel()).isEqualTo("isbn value");
    assertThat(isbn.getFirstType().getTypeUri()).isEqualTo(ISBN);
    assertThat(isbn.getResourceHash()).isNotNull();
    assertThat(isbn.getDoc().size()).isEqualTo(2);
    assertThat(isbn.getDoc().get(NAME).size()).isEqualTo(1);
    assertThat(isbn.getDoc().get(NAME).get(0).asText()).isEqualTo("isbn value");
    assertThat(isbn.getDoc().get(QUALIFIER).size()).isEqualTo(1);
    assertThat(isbn.getDoc().get(QUALIFIER).get(0).asText()).isEqualTo("isbn qualifier");
    assertThat(isbn.getOutgoingEdges()).hasSize(1);
    validateStatus(isbn.getOutgoingEdges().iterator().next(), isbn, "isbn");
  }

  private void validateEan(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(MAP_PRED);
    var ean = edge.getTarget();
    assertThat(ean.getLabel()).isEqualTo("ean value");
    assertThat(ean.getFirstType().getTypeUri()).isEqualTo(EAN);
    assertThat(ean.getResourceHash()).isNotNull();
    assertThat(ean.getDoc().size()).isEqualTo(2);
    assertThat(ean.getDoc().get(EAN_VALUE).size()).isEqualTo(1);
    assertThat(ean.getDoc().get(EAN_VALUE).get(0).asText()).isEqualTo("ean value");
    assertThat(ean.getDoc().get(QUALIFIER).size()).isEqualTo(1);
    assertThat(ean.getDoc().get(QUALIFIER).get(0).asText()).isEqualTo("ean qualifier");
    assertThat(ean.getOutgoingEdges()).isEmpty();
  }

  private void validateLocalId(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(MAP_PRED);
    var localId = edge.getTarget();
    assertThat(localId.getLabel()).isEqualTo("localId value");
    assertThat(localId.getFirstType().getTypeUri()).isEqualTo(LOCAL_ID);
    assertThat(localId.getResourceHash()).isNotNull();
    assertThat(localId.getDoc().size()).isEqualTo(2);
    assertThat(localId.getDoc().get(LOCAL_ID_VALUE).size()).isEqualTo(1);
    assertThat(localId.getDoc().get(LOCAL_ID_VALUE).get(0).asText()).isEqualTo("localId value");
    assertThat(localId.getDoc().get(ASSIGNING_SOURCE).size()).isEqualTo(1);
    assertThat(localId.getDoc().get(ASSIGNING_SOURCE).get(0).asText()).isEqualTo("localId assigner");
    assertThat(localId.getOutgoingEdges()).isEmpty();
  }

  private void validateOtherId(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(MAP_PRED);
    var otherId = edge.getTarget();
    assertThat(otherId.getLabel()).isEqualTo("otherId value");
    assertThat(otherId.getFirstType().getTypeUri()).isEqualTo(OTHER_ID);
    assertThat(otherId.getResourceHash()).isNotNull();
    assertThat(otherId.getDoc().size()).isEqualTo(2);
    assertThat(otherId.getDoc().get(NAME).size()).isEqualTo(1);
    assertThat(otherId.getDoc().get(NAME).get(0).asText()).isEqualTo("otherId value");
    assertThat(otherId.getDoc().get(QUALIFIER).size()).isEqualTo(1);
    assertThat(otherId.getDoc().get(QUALIFIER).get(0).asText()).isEqualTo("otherId qualifier");
    assertThat(otherId.getOutgoingEdges()).isEmpty();
  }

  private void validateStatus(ResourceEdge edge, Resource source, String prefix) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(STATUS_PRED);
    var status = edge.getTarget();
    assertThat(status.getLabel()).isEqualTo(prefix + " status value");
    assertThat(status.getFirstType().getTypeUri()).isEqualTo(STATUS);
    assertThat(status.getResourceHash()).isNotNull();
    assertThat(status.getDoc().size()).isEqualTo(2);
    assertThat(status.getDoc().get(LINK).size()).isEqualTo(1);
    assertThat(status.getDoc().get(LINK).get(0).asText()).isEqualTo(prefix + " status link");
    assertThat(status.getDoc().get(LABEL).size()).isEqualTo(1);
    assertThat(status.getDoc().get(LABEL).get(0).asText()).isEqualTo(prefix + " status value");
    assertThat(status.getOutgoingEdges()).isEmpty();
  }

  private void validateAccessLocation(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(ACCESS_LOCATION_PRED);
    var locator = edge.getTarget();
    assertThat(locator.getLabel()).isEqualTo("accessLocation value");
    assertThat(locator.getFirstType().getTypeUri()).isEqualTo(ACCESS_LOCATION);
    assertThat(locator.getResourceHash()).isNotNull();
    assertThat(locator.getDoc().size()).isEqualTo(2);
    assertThat(locator.getDoc().get(LINK).size()).isEqualTo(1);
    assertThat(locator.getDoc().get(LINK).get(0).asText()).isEqualTo("accessLocation value");
    assertThat(locator.getDoc().get(NOTE).size()).isEqualTo(1);
    assertThat(locator.getDoc().get(NOTE).get(0).asText()).isEqualTo("accessLocation note");
    assertThat(locator.getOutgoingEdges()).isEmpty();
  }

  private void validateCategory(ResourceEdge edge, Resource source, String pred, String type) {
    var prefix = pred.substring(pred.lastIndexOf("/") + 1);
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(pred);
    var media = edge.getTarget();
    assertThat(media.getLabel()).isEqualTo(prefix + " term");
    assertThat(media.getFirstType().getTypeUri()).isEqualTo(type);
    assertThat(media.getResourceHash()).isNotNull();
    assertThat(media.getDoc().size()).isEqualTo(3);
    assertThat(media.getDoc().get(CODE).size()).isEqualTo(1);
    assertThat(media.getDoc().get(CODE).get(0).asText()).isEqualTo(prefix + " code");
    assertThat(media.getDoc().get(TERM).size()).isEqualTo(1);
    assertThat(media.getDoc().get(TERM).get(0).asText()).isEqualTo(prefix + " term");
    assertThat(media.getDoc().get(LINK).size()).isEqualTo(1);
    assertThat(media.getDoc().get(LINK).get(0).asText()).isEqualTo(prefix + " link");
    assertThat(media.getOutgoingEdges()).isEmpty();
  }

  private void validateCopyrightDate(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(COPYRIGHT_PRED);
    var copyrightEvent = edge.getTarget();
    assertThat(copyrightEvent.getLabel()).isEqualTo("copyright date value");
    assertThat(copyrightEvent.getFirstType().getTypeUri()).isEqualTo(COPYRIGHT_EVENT);
    assertThat(copyrightEvent.getResourceHash()).isNotNull();
    assertThat(copyrightEvent.getDoc().size()).isEqualTo(1);
    assertThat(copyrightEvent.getDoc().get(DATE).size()).isEqualTo(1);
    assertThat(copyrightEvent.getDoc().get(DATE).get(0).asText()).isEqualTo("copyright date value");
    assertThat(copyrightEvent.getOutgoingEdges()).isEmpty();
  }

  private String toInstance() {
    return String.join(".", "$", path("resource"), path(INSTANCE));
  }

  private String toDimensions() {
    return String.join(".", toInstance(), arrayPath(DIMENSIONS));
  }

  private String toEditionStatement() {
    return String.join(".", toInstance(), arrayPath(EDITION_STATEMENT));
  }

  private String toAccessLocationLink() {
    return String.join(".", toInstance(), arrayPath(ACCESS_LOCATION_PRED), arrayPath(LINK));
  }

  private String toAccessLocationNote() {
    return String.join(".", toInstance(), arrayPath(ACCESS_LOCATION_PRED), arrayPath(NOTE));
  }

  private String toResponsibilityStatement() {
    return String.join(".", toInstance(), arrayPath(RESPONSIBILITY_STATEMENT));
  }

  private String toProjectedProvisionDate() {
    return String.join(".", toInstance(), arrayPath(PROJECTED_PROVISION_DATE));
  }

  private String toInstanceTitlePartName() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE),
      arrayPath(PART_NAME));
  }

  private String toInstanceTitlePartNumber() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE),
      arrayPath(PART_NUMBER));
  }

  private String toInstanceTitleMain() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE),
      arrayPath(MAIN_TITLE));
  }

  private String toInstanceTitleNonSortNum() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE),
      arrayPath(NON_SORT_NUM));
  }

  private String toInstanceTitleSubtitle() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE),
      arrayPath(SUBTITLE));
  }

  private String toIssuance() {
    return String.join(".", toInstance(), arrayPath(ISSUANCE));
  }

  private String toParallelTitlePartName() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(PART_NAME));
  }

  private String toParallelTitlePartNumber() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(PART_NUMBER));
  }

  private String toParallelTitleMain() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(MAIN_TITLE));
  }

  private String toParallelTitleDate() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(DATE));
  }

  private String toParallelTitleSubtitle() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(SUBTITLE));
  }

  private String toParallelTitleNote() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(NOTE));
  }

  private String toVariantTitlePartName() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(PART_NAME));
  }

  private String toVariantTitlePartNumber() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(PART_NUMBER));
  }

  private String toVariantTitleMain() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(MAIN_TITLE));
  }

  private String toVariantTitleDate() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(DATE));
  }

  private String toVariantTitleSubtitle() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(SUBTITLE));
  }

  private String toVariantTitleType() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(VARIANT_TYPE));
  }

  private String toVariantTitleNote() {
    return String.join(".", toInstance(), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(NOTE));
  }

  private String toProviderEventDate(String predicate) {
    return String.join(".", toInstance(), arrayPath(predicate), arrayPath(DATE));
  }

  private String toProviderEventName(String predicate) {
    return String.join(".", toInstance(), arrayPath(predicate), arrayPath(NAME));
  }

  private String toProviderEventPlaceName(String predicate) {
    return String.join(".", toInstance(), arrayPath(predicate), arrayPath(PROVIDER_PLACE_PRED),
      arrayPath(NAME));
  }

  private String toProviderEventPlaceLink(String predicate) {
    return String.join(".", toInstance(), arrayPath(predicate), arrayPath(PROVIDER_PLACE_PRED),
      arrayPath(LINK));
  }

  private String toProviderEventSimpleDate(String predicate) {
    return String.join(".", toInstance(), arrayPath(predicate), arrayPath(SIMPLE_DATE));
  }

  private String toProviderEventSimplePlace(String predicate) {
    return String.join(".", toInstance(), arrayPath(predicate), arrayPath(SIMPLE_PLACE));
  }

  private String toLccnValue() {
    return String.join(".", toInstance(), arrayPath(MAP_PRED), path(LCCN), arrayPath(NAME));
  }

  private String toLccnStatusValue() {
    return String.join(".", toInstance(), arrayPath(MAP_PRED), path(LCCN), arrayPath(STATUS_PRED),
      arrayPath(LABEL));
  }

  private String toLccnStatusLink() {
    return String.join(".", toInstance(), arrayPath(MAP_PRED), path(LCCN), arrayPath(STATUS_PRED),
      arrayPath(LINK));
  }

  private String toIsbnValue() {
    return String.join(".", toInstance(), arrayPath(MAP_PRED, 1), path(ISBN), arrayPath(NAME));
  }

  private String toIsbnQualifier() {
    return String.join(".", toInstance(), arrayPath(MAP_PRED, 1), path(ISBN), arrayPath(QUALIFIER));
  }

  private String toIsbnStatusValue() {
    return String.join(".", toInstance(), arrayPath(MAP_PRED, 1), path(ISBN), arrayPath(STATUS_PRED),
      arrayPath(LABEL));
  }

  private String toIsbnStatusLink() {
    return String.join(".", toInstance(), arrayPath(MAP_PRED, 1), path(ISBN), arrayPath(STATUS_PRED),
      arrayPath(LINK));
  }

  private String toEanValue() {
    return String.join(".", toInstance(), arrayPath(MAP_PRED, 2), path(EAN), arrayPath(EAN_VALUE));
  }

  private String toEanQualifier() {
    return String.join(".", toInstance(), arrayPath(MAP_PRED, 2), path(EAN), arrayPath(QUALIFIER));
  }

  private String toLocalIdValue() {
    return String.join(".", toInstance(), arrayPath(MAP_PRED, 3), path(LOCAL_ID), arrayPath(LOCAL_ID_VALUE));
  }

  private String toLocalIdAssigner() {
    return String.join(".", toInstance(), arrayPath(MAP_PRED, 3), path(LOCAL_ID), arrayPath(ASSIGNING_SOURCE));
  }

  private String toOtherIdValue() {
    return String.join(".", toInstance(), arrayPath(MAP_PRED, 4), path(OTHER_ID), arrayPath(NAME));
  }

  private String toOtherIdQualifier() {
    return String.join(".", toInstance(), arrayPath(MAP_PRED, 4), path(OTHER_ID), arrayPath(QUALIFIER));
  }

  private String toCarrierCode() {
    return String.join(".", toInstance(), arrayPath(CARRIER_PRED), arrayPath(CODE));
  }

  private String toCarrierLink() {
    return String.join(".", toInstance(), arrayPath(CARRIER_PRED), arrayPath(LINK));
  }

  private String toCarrierTerm() {
    return String.join(".", toInstance(), arrayPath(CARRIER_PRED), arrayPath(TERM));
  }

  private String toCopyrightDate() {
    return String.join(".", toInstance(), arrayPath(COPYRIGHT_PRED), arrayPath(DATE));
  }

  private String toMediaCode() {
    return String.join(".", toInstance(), arrayPath(MEDIA_PRED), arrayPath(CODE));
  }

  private String toMediaLink() {
    return String.join(".", toInstance(), arrayPath(MEDIA_PRED), arrayPath(LINK));
  }

  private String toMediaTerm() {
    return String.join(".", toInstance(), arrayPath(MEDIA_PRED), arrayPath(TERM));
  }

  private String toErrorType() {
    return String.join(".", arrayPath("errors"), path("type"));
  }

  private String toErrorCode() {
    return String.join(".", arrayPath("errors"), path("code"));
  }

  private String toErrorMessage() {
    return String.join(".", arrayPath("errors"), path("message"));
  }

  private String path(String path) {
    return String.format("['%s']", path);
  }

  private String arrayPath(String path, int index) {
    return String.format("['%s'][%d]", path, index);
  }

  private String arrayPath(String path) {
    return arrayPath(path, 0);
  }

}
