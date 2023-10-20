package org.folio.linked.data.e2e;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.ACCESS_LOCATION;
import static org.folio.ld.dictionary.PredicateDictionary.CARRIER;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.COPYRIGHT;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.MEDIA;
import static org.folio.ld.dictionary.PredicateDictionary.PE_DISTRIBUTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_MANUFACTURE;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PRODUCTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.PredicateDictionary.STATUS;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.Property.ASSIGNING_SOURCE;
import static org.folio.ld.dictionary.Property.CODE;
import static org.folio.ld.dictionary.Property.DATE;
import static org.folio.ld.dictionary.Property.DIMENSIONS;
import static org.folio.ld.dictionary.Property.EAN_VALUE;
import static org.folio.ld.dictionary.Property.EDITION_STATEMENT;
import static org.folio.ld.dictionary.Property.EXTENT;
import static org.folio.ld.dictionary.Property.ISSUANCE;
import static org.folio.ld.dictionary.Property.LABEL;
import static org.folio.ld.dictionary.Property.LANGUAGE;
import static org.folio.ld.dictionary.Property.LCNAF_ID;
import static org.folio.ld.dictionary.Property.LINK;
import static org.folio.ld.dictionary.Property.LOCAL_ID_VALUE;
import static org.folio.ld.dictionary.Property.MAIN_TITLE;
import static org.folio.ld.dictionary.Property.NAME;
import static org.folio.ld.dictionary.Property.NON_SORT_NUM;
import static org.folio.ld.dictionary.Property.NOTE;
import static org.folio.ld.dictionary.Property.PART_NAME;
import static org.folio.ld.dictionary.Property.PART_NUMBER;
import static org.folio.ld.dictionary.Property.PROJECTED_PROVISION_DATE;
import static org.folio.ld.dictionary.Property.PROVIDER_DATE;
import static org.folio.ld.dictionary.Property.QUALIFIER;
import static org.folio.ld.dictionary.Property.RESPONSIBILITY_STATEMENT;
import static org.folio.ld.dictionary.Property.SIMPLE_PLACE;
import static org.folio.ld.dictionary.Property.SOURCE;
import static org.folio.ld.dictionary.Property.SUBTITLE;
import static org.folio.ld.dictionary.Property.SUMMARY;
import static org.folio.ld.dictionary.Property.TABLE_OF_CONTENTS;
import static org.folio.ld.dictionary.Property.TARGET_AUDIENCE;
import static org.folio.ld.dictionary.Property.TERM;
import static org.folio.ld.dictionary.Property.VARIANT_TYPE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.COPYRIGHT_EVENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LOCAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.linked.data.model.ErrorCode.NOT_FOUND_ERROR;
import static org.folio.linked.data.model.ErrorCode.VALIDATION_ERROR;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.bibframeSampleResource;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getBibframeSample;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;
import static org.folio.linked.data.util.Constants.TYPE;
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
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
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
    var response = validateInstanceResourceResponse(resultActions)
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
    validateInstanceResourceResponse(resultActions);
    validateWorkResourceResponse(resultActions);
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
      .param(TYPE, monographTestService.getInstanceType().getUri())
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
    assertThat(resourceRepo.count()).isEqualTo(28);
    assertThat(resourceEdgeRepository.count()).isEqualTo(27);
    var requestBuilder = delete(BIBFRAME_URL + "/" + existed.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()));

    // when
    mockMvc.perform(requestBuilder);

    // then
    assertThat(resourceRepo.findById(existed.getResourceHash())).isNotPresent();
    assertThat(resourceRepo.count()).isEqualTo(27);
    assertThat(resourceEdgeRepository.findById(existed.getOutgoingEdges().iterator().next().getId())).isNotPresent();
    assertThat(resourceEdgeRepository.count()).isEqualTo(10);
    checkKafkaMessageSent(null, existed.getResourceHash());
  }

  protected void checkKafkaMessageSent(Resource persisted, Long deleted) {
    // nothing to check without Folio profile
  }

  @NotNull
  private ResultActions validateInstanceResourceResponse(ResultActions resultActions) throws Exception {
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
      .andExpect(jsonPath(toExtent(), equalTo("extent info")))
      .andExpect(jsonPath(toDimensions(), equalTo("20 cm")))
      .andExpect(jsonPath(toEanValue(), equalTo("ean value")))
      .andExpect(jsonPath(toEanQualifier(), equalTo("ean qualifier")))
      .andExpect(jsonPath(toEditionStatement(), equalTo("edition statement")))
      .andExpect(jsonPath(toInstanceTitlePartName(), equalTo("Instance: partName")))
      .andExpect(jsonPath(toInstanceTitlePartNumber(), equalTo("Instance: partNumber")))
      .andExpect(jsonPath(toInstanceTitleMain(), equalTo("Instance: mainTitle")))
      .andExpect(jsonPath(toInstanceTitleNonSortNum(), equalTo("Instance: nonSortNum")))
      .andExpect(jsonPath(toInstanceTitleSubtitle(), equalTo("Instance: subTitle")))
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
      .andExpect(jsonPath(toParallelTitleSubtitle(), equalTo("Parallel: subTitle")))
      .andExpect(jsonPath(toProviderEventDate(PE_PRODUCTION), equalTo("production date")))
      .andExpect(jsonPath(toProviderEventName(PE_PRODUCTION), equalTo("production name")))
      .andExpect(jsonPath(toProviderEventPlaceName(PE_PRODUCTION), equalTo("production providerPlace name")))
      .andExpect(jsonPath(toProviderEventPlaceLink(PE_PRODUCTION), equalTo("production providerPlace link")))
      .andExpect(jsonPath(toProviderEventProviderDate(PE_PRODUCTION), equalTo("production provider date")))
      .andExpect(jsonPath(toProviderEventSimplePlace(PE_PRODUCTION), equalTo("production simple place")))
      .andExpect(jsonPath(toProviderEventDate(PE_PUBLICATION), equalTo("publication date")))
      .andExpect(jsonPath(toProviderEventName(PE_PUBLICATION), equalTo("publication name")))
      .andExpect(jsonPath(toProviderEventPlaceName(PE_PUBLICATION), equalTo("publication providerPlace name")))
      .andExpect(jsonPath(toProviderEventPlaceLink(PE_PUBLICATION), equalTo("publication providerPlace link")))
      .andExpect(jsonPath(toProviderEventProviderDate(PE_PUBLICATION), equalTo("publication provider date")))
      .andExpect(jsonPath(toProviderEventSimplePlace(PE_PUBLICATION), equalTo("publication simple place")))
      .andExpect(jsonPath(toProviderEventDate(PE_DISTRIBUTION), equalTo("distribution date")))
      .andExpect(jsonPath(toProviderEventName(PE_DISTRIBUTION), equalTo("distribution name")))
      .andExpect(jsonPath(toProviderEventPlaceName(PE_DISTRIBUTION), equalTo("distribution providerPlace name")))
      .andExpect(jsonPath(toProviderEventPlaceLink(PE_DISTRIBUTION), equalTo("distribution providerPlace link")))
      .andExpect(jsonPath(toProviderEventProviderDate(PE_DISTRIBUTION), equalTo("distribution provider date")))
      .andExpect(jsonPath(toProviderEventSimplePlace(PE_DISTRIBUTION), equalTo("distribution simple place")))
      .andExpect(jsonPath(toProviderEventDate(PE_MANUFACTURE), equalTo("manufacture date")))
      .andExpect(jsonPath(toProviderEventName(PE_MANUFACTURE), equalTo("manufacture name")))
      .andExpect(jsonPath(toProviderEventPlaceName(PE_MANUFACTURE), equalTo("manufacture providerPlace name")))
      .andExpect(jsonPath(toProviderEventPlaceLink(PE_MANUFACTURE), equalTo("manufacture providerPlace link")))
      .andExpect(jsonPath(toProviderEventProviderDate(PE_MANUFACTURE), equalTo("manufacture provider date")))
      .andExpect(jsonPath(toProviderEventSimplePlace(PE_MANUFACTURE), equalTo("manufacture simple place")))
      .andExpect(jsonPath(toProjectedProvisionDate(), equalTo("projected provision date")))
      .andExpect(jsonPath(toResponsibilityStatement(), equalTo("responsibility statement")))
      .andExpect(jsonPath(toVariantTitlePartName(), equalTo("Variant: partName")))
      .andExpect(jsonPath(toVariantTitlePartNumber(), equalTo("Variant: partNumber")))
      .andExpect(jsonPath(toVariantTitleMain(), equalTo("Variant: mainTitle")))
      .andExpect(jsonPath(toVariantTitleNote(), equalTo("Variant: noteLabel")))
      .andExpect(jsonPath(toVariantTitleDate(), equalTo("Variant: date")))
      .andExpect(jsonPath(toVariantTitleSubtitle(), equalTo("Variant: subTitle")))
      .andExpect(jsonPath(toVariantTitleType(), equalTo("Variant: variantType")));
  }

  @NotNull
  private ResultActions validateWorkResourceResponse(ResultActions resultActions) throws Exception {
    return resultActions
      .andExpect(status().isOk())
      .andExpect(jsonPath(toWorkTargetAudience(), equalTo("Work: target audience")))
      .andExpect(jsonPath(toWorkLanguage(), equalTo("Work: language")))
      .andExpect(jsonPath(toWorkSummary(), equalTo("Work: summary")))
      .andExpect(jsonPath(toWorkTableOfContents(), equalTo("Work: table of contents")))
      .andExpect(jsonPath(toWorkDeweyCode(), equalTo("Dewey: code")))
      .andExpect(jsonPath(toWorkDeweySource(), equalTo("Dewey: source")))
      .andExpect(jsonPath(toWorkCreatorPersonName(), equalTo("Person: name")))
      .andExpect(jsonPath(toWorkCreatorPersonLcnafId(), equalTo("Person: lcnafId")))
      .andExpect(jsonPath(toWorkContributorOrgName(), equalTo("Organization: name")))
      .andExpect(jsonPath(toWorkContributorOrgLcnafId(), equalTo("Organization: lcnafId")))
      .andExpect(jsonPath(toWorkContentLink(), equalTo("Content: link")))
      .andExpect(jsonPath(toWorkContentCode(), equalTo("Content: code")))
      .andExpect(jsonPath(toWorkContentTerm(), equalTo("Content: term")));
  }

  private void validateMonographInstanceResource(Resource resource) {
    assertThat(resource.getFirstType().getUri()).isEqualTo(INSTANCE.getUri());
    validateInstance(resource);
  }

  private void validateInstance(Resource instance) {
    assertThat(instance.getResourceHash()).isNotNull();
    assertThat(instance.getLabel()).isEqualTo("Instance: mainTitle");
    assertThat(instance.getFirstType().getUri()).isEqualTo(INSTANCE.getUri());
    assertThat(instance.getResourceHash()).isNotNull();
    assertThat(instance.getDoc().size()).isEqualTo(6);
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
    validateProviderEvent(edgeIterator.next(), instance, PE_PRODUCTION);
    validateProviderEvent(edgeIterator.next(), instance, PE_PUBLICATION);
    validateProviderEvent(edgeIterator.next(), instance, PE_DISTRIBUTION);
    validateProviderEvent(edgeIterator.next(), instance, PE_MANUFACTURE);
    validateAccessLocation(edgeIterator.next(), instance);
    validateLccn(edgeIterator.next(), instance);
    validateIsbn(edgeIterator.next(), instance);
    validateEan(edgeIterator.next(), instance);
    validateLocalId(edgeIterator.next(), instance);
    validateOtherId(edgeIterator.next(), instance);
    validateCategory(edgeIterator.next(), instance, MEDIA);
    validateCategory(edgeIterator.next(), instance, CARRIER);
    validateCopyrightDate(edgeIterator.next(), instance);
    assertThat(edgeIterator.hasNext()).isFalse();
  }

  private void validateLiteral(Resource resource, String field, String value) {
    assertThat(resource.getDoc().get(field).size()).isEqualTo(1);
    assertThat(resource.getDoc().get(field).get(0).asText()).isEqualTo(value);
  }

  private void validateInstanceTitle(ResourceEdge edge, Resource source) {
    validateSampleTitleBase(edge, source, ResourceTypeDictionary.TITLE, "Instance: ");
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

  private void validateSampleTitleBase(ResourceEdge edge, Resource source, ResourceTypeDictionary type, String prefix) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(TITLE.getUri());
    var title = edge.getTarget();
    assertThat(title.getLabel()).isEqualTo(prefix + "mainTitle");
    assertThat(title.getFirstType().getUri()).isEqualTo(type.getUri());
    assertThat(title.getResourceHash()).isNotNull();
    assertThat(title.getDoc().get(PART_NAME).size()).isEqualTo(1);
    assertThat(title.getDoc().get(PART_NAME).get(0).asText()).isEqualTo(prefix + "partName");
    assertThat(title.getDoc().get(PART_NUMBER).size()).isEqualTo(1);
    assertThat(title.getDoc().get(PART_NUMBER).get(0).asText()).isEqualTo(prefix + "partNumber");
    assertThat(title.getDoc().get(MAIN_TITLE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(MAIN_TITLE).get(0).asText()).isEqualTo(prefix + "mainTitle");
    assertThat(title.getDoc().get(SUBTITLE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(SUBTITLE).get(0).asText()).isEqualTo(prefix + "subTitle");
  }

  private void validateProviderEvent(ResourceEdge edge, Resource source, PredicateDictionary predicate) {
    var type = predicate.getUri().substring(predicate.getUri().indexOf("marc/") + 5);
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(predicate.getUri());
    var providerEvent = edge.getTarget();
    assertThat(providerEvent.getLabel()).isEqualTo(type + " name");
    assertThat(providerEvent.getFirstType().getUri()).isEqualTo(PROVIDER_EVENT.getUri());
    assertThat(providerEvent.getResourceHash()).isNotNull();
    assertThat(providerEvent.getDoc().size()).isEqualTo(4);
    assertThat(providerEvent.getDoc().get(DATE).size()).isEqualTo(1);
    assertThat(providerEvent.getDoc().get(DATE).get(0).asText()).isEqualTo(type + " date");
    assertThat(providerEvent.getDoc().get(NAME).size()).isEqualTo(1);
    assertThat(providerEvent.getDoc().get(NAME).get(0).asText()).isEqualTo(type + " name");
    assertThat(providerEvent.getDoc().get(PROVIDER_DATE).size()).isEqualTo(1);
    assertThat(providerEvent.getDoc().get(PROVIDER_DATE).get(0).asText()).isEqualTo(type + " provider date");
    assertThat(providerEvent.getDoc().get(SIMPLE_PLACE).size()).isEqualTo(1);
    assertThat(providerEvent.getDoc().get(SIMPLE_PLACE).get(0).asText()).isEqualTo(type + " simple place");
    assertThat(providerEvent.getOutgoingEdges()).hasSize(1);
    validateProviderPlace(providerEvent.getOutgoingEdges().iterator().next(), providerEvent, type);
  }

  private void validateProviderPlace(ResourceEdge edge, Resource source, String prefix) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(PROVIDER_PLACE.getUri());
    var place = edge.getTarget();
    assertThat(place.getLabel()).isEqualTo(prefix + " providerPlace name");
    assertThat(place.getFirstType().getUri()).isEqualTo(PLACE.getUri());
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
    assertThat(edge.getPredicate().getUri()).isEqualTo(MAP.getUri());
    var lccn = edge.getTarget();
    assertThat(lccn.getLabel()).isEqualTo("lccn value");
    assertThat(lccn.getFirstType().getUri()).isEqualTo(ID_LCCN.getUri());
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
    assertThat(edge.getPredicate().getUri()).isEqualTo(MAP.getUri());
    var isbn = edge.getTarget();
    assertThat(isbn.getLabel()).isEqualTo("isbn value");
    assertThat(isbn.getFirstType().getUri()).isEqualTo(ID_ISBN.getUri());
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
    assertThat(edge.getPredicate().getUri()).isEqualTo(MAP.getUri());
    var ean = edge.getTarget();
    assertThat(ean.getLabel()).isEqualTo("ean value");
    assertThat(ean.getFirstType().getUri()).isEqualTo(ID_EAN.getUri());
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
    assertThat(edge.getPredicate().getUri()).isEqualTo(MAP.getUri());
    var localId = edge.getTarget();
    assertThat(localId.getLabel()).isEqualTo("localId value");
    assertThat(localId.getFirstType().getUri()).isEqualTo(ID_LOCAL.getUri());
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
    assertThat(edge.getPredicate().getUri()).isEqualTo(MAP.getUri());
    var otherId = edge.getTarget();
    assertThat(otherId.getLabel()).isEqualTo("otherId value");
    assertThat(otherId.getFirstType().getUri()).isEqualTo(ID_UNKNOWN.getUri());
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
    assertThat(edge.getPredicate().getUri()).isEqualTo(STATUS.getUri());
    var status = edge.getTarget();
    assertThat(status.getLabel()).isEqualTo(prefix + " status value");
    assertThat(status.getFirstType().getUri()).isEqualTo(ResourceTypeDictionary.STATUS.getUri());
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
    assertThat(edge.getPredicate().getUri()).isEqualTo(ACCESS_LOCATION.getUri());
    var locator = edge.getTarget();
    assertThat(locator.getLabel()).isEqualTo("accessLocation value");
    assertThat(locator.getFirstType().getUri()).isEqualTo(ANNOTATION.getUri());
    assertThat(locator.getResourceHash()).isNotNull();
    assertThat(locator.getDoc().size()).isEqualTo(2);
    assertThat(locator.getDoc().get(LINK).size()).isEqualTo(1);
    assertThat(locator.getDoc().get(LINK).get(0).asText()).isEqualTo("accessLocation value");
    assertThat(locator.getDoc().get(NOTE).size()).isEqualTo(1);
    assertThat(locator.getDoc().get(NOTE).get(0).asText()).isEqualTo("accessLocation note");
    assertThat(locator.getOutgoingEdges()).isEmpty();
  }

  private void validateCategory(ResourceEdge edge, Resource source, PredicateDictionary pred) {
    var prefix = pred.getUri().substring(pred.getUri().lastIndexOf("/") + 1);
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getUri()).isEqualTo(pred.getUri());
    var media = edge.getTarget();
    assertThat(media.getLabel()).isEqualTo(prefix + " term");
    assertThat(media.getFirstType().getUri()).isEqualTo(CATEGORY.getUri());
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
    assertThat(edge.getPredicate().getUri()).isEqualTo(COPYRIGHT.getUri());
    var copyrightEvent = edge.getTarget();
    assertThat(copyrightEvent.getLabel()).isEqualTo("copyright date value");
    assertThat(copyrightEvent.getFirstType().getUri()).isEqualTo(COPYRIGHT_EVENT.getUri());
    assertThat(copyrightEvent.getResourceHash()).isNotNull();
    assertThat(copyrightEvent.getDoc().size()).isEqualTo(1);
    assertThat(copyrightEvent.getDoc().get(DATE).size()).isEqualTo(1);
    assertThat(copyrightEvent.getDoc().get(DATE).get(0).asText()).isEqualTo("copyright date value");
    assertThat(copyrightEvent.getOutgoingEdges()).isEmpty();
  }

  private String toInstance() {
    return join(".", "$", path("resource"), path(INSTANCE.getUri()));
  }

  private String toWork() {
    return join(".", toInstance(), arrayPath(INSTANTIATES.getUri()));
  }

  private String toExtent() {
    return String.join(".", toInstance(), arrayPath(EXTENT));
  }

  private String toDimensions() {
    return join(".", toInstance(), arrayPath(DIMENSIONS));
  }

  private String toEditionStatement() {
    return join(".", toInstance(), arrayPath(EDITION_STATEMENT));
  }

  private String toAccessLocationLink() {
    return join(".", toInstance(), arrayPath(ACCESS_LOCATION.getUri()), arrayPath(LINK));
  }

  private String toAccessLocationNote() {
    return join(".", toInstance(), arrayPath(ACCESS_LOCATION.getUri()), arrayPath(NOTE));
  }

  private String toResponsibilityStatement() {
    return join(".", toInstance(), arrayPath(RESPONSIBILITY_STATEMENT));
  }

  private String toProjectedProvisionDate() {
    return join(".", toInstance(), arrayPath(PROJECTED_PROVISION_DATE));
  }

  private String toInstanceTitlePartName() {
    return join(".", toInstance(), arrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(PART_NAME));
  }

  private String toInstanceTitlePartNumber() {
    return join(".", toInstance(), arrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(PART_NUMBER));
  }

  private String toInstanceTitleMain() {
    return join(".", toInstance(), arrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(MAIN_TITLE));
  }

  private String toInstanceTitleNonSortNum() {
    return join(".", toInstance(), arrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(NON_SORT_NUM));
  }

  private String toInstanceTitleSubtitle() {
    return join(".", toInstance(), arrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(SUBTITLE));
  }

  private String toIssuance() {
    return join(".", toInstance(), arrayPath(ISSUANCE));
  }

  private String toParallelTitlePartName() {
    return join(".", toInstance(), arrayPath(TITLE.getUri(), 1), path(PARALLEL_TITLE.getUri()),
      arrayPath(PART_NAME));
  }

  private String toParallelTitlePartNumber() {
    return join(".", toInstance(), arrayPath(TITLE.getUri(), 1), path(PARALLEL_TITLE.getUri()),
      arrayPath(PART_NUMBER));
  }

  private String toParallelTitleMain() {
    return join(".", toInstance(), arrayPath(TITLE.getUri(), 1), path(PARALLEL_TITLE.getUri()),
      arrayPath(MAIN_TITLE));
  }

  private String toParallelTitleDate() {
    return join(".", toInstance(), arrayPath(TITLE.getUri(), 1), path(PARALLEL_TITLE.getUri()),
      arrayPath(DATE));
  }

  private String toParallelTitleSubtitle() {
    return join(".", toInstance(), arrayPath(TITLE.getUri(), 1), path(PARALLEL_TITLE.getUri()),
      arrayPath(SUBTITLE));
  }

  private String toParallelTitleNote() {
    return join(".", toInstance(), arrayPath(TITLE.getUri(), 1), path(PARALLEL_TITLE.getUri()),
      arrayPath(NOTE));
  }

  private String toVariantTitlePartName() {
    return join(".", toInstance(), arrayPath(TITLE.getUri(), 2), path(VARIANT_TITLE.getUri()),
      arrayPath(PART_NAME));
  }

  private String toVariantTitlePartNumber() {
    return join(".", toInstance(), arrayPath(TITLE.getUri(), 2), path(VARIANT_TITLE.getUri()),
      arrayPath(PART_NUMBER));
  }

  private String toVariantTitleMain() {
    return join(".", toInstance(), arrayPath(TITLE.getUri(), 2), path(VARIANT_TITLE.getUri()),
      arrayPath(MAIN_TITLE));
  }

  private String toVariantTitleDate() {
    return join(".", toInstance(), arrayPath(TITLE.getUri(), 2), path(VARIANT_TITLE.getUri()),
      arrayPath(DATE));
  }

  private String toVariantTitleSubtitle() {
    return join(".", toInstance(), arrayPath(TITLE.getUri(), 2), path(VARIANT_TITLE.getUri()),
      arrayPath(SUBTITLE));
  }

  private String toVariantTitleType() {
    return join(".", toInstance(), arrayPath(TITLE.getUri(), 2), path(VARIANT_TITLE.getUri()),
      arrayPath(VARIANT_TYPE));
  }

  private String toVariantTitleNote() {
    return join(".", toInstance(), arrayPath(TITLE.getUri(), 2), path(VARIANT_TITLE.getUri()),
      arrayPath(NOTE));
  }

  private String toProviderEventDate(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(DATE));
  }

  private String toProviderEventName(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(NAME));
  }

  private String toProviderEventPlaceName(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(PROVIDER_PLACE.getUri()),
      arrayPath(NAME));
  }

  private String toProviderEventPlaceLink(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(PROVIDER_PLACE.getUri()),
      arrayPath(LINK));
  }

  private String toProviderEventProviderDate(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(PROVIDER_DATE));
  }

  private String toProviderEventSimplePlace(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(SIMPLE_PLACE));
  }

  private String toLccnValue() {
    return join(".", toInstance(), arrayPath(MAP.getUri()), path(ID_LCCN.getUri()), arrayPath(NAME));
  }

  private String toLccnStatusValue() {
    return join(".", toInstance(), arrayPath(MAP.getUri()), path(ID_LCCN.getUri()),
      arrayPath(STATUS.getUri()), arrayPath(LABEL));
  }

  private String toLccnStatusLink() {
    return join(".", toInstance(), arrayPath(MAP.getUri()), path(ID_LCCN.getUri()), arrayPath(STATUS.getUri()),
      arrayPath(LINK));
  }

  private String toIsbnValue() {
    return join(".", toInstance(), arrayPath(MAP.getUri(), 1), path(ID_ISBN.getUri()), arrayPath(NAME));
  }

  private String toIsbnQualifier() {
    return join(".", toInstance(), arrayPath(MAP.getUri(), 1), path(ID_ISBN.getUri()), arrayPath(QUALIFIER));
  }

  private String toIsbnStatusValue() {
    return join(".", toInstance(), arrayPath(MAP.getUri(), 1), path(ID_ISBN.getUri()),
      arrayPath(STATUS.getUri()), arrayPath(LABEL));
  }

  private String toIsbnStatusLink() {
    return join(".", toInstance(), arrayPath(MAP.getUri(), 1), path(ID_ISBN.getUri()),
      arrayPath(STATUS.getUri()), arrayPath(LINK));
  }

  private String toEanValue() {
    return join(".", toInstance(), arrayPath(MAP.getUri(), 2), path(ID_EAN.getUri()), arrayPath(EAN_VALUE));
  }

  private String toEanQualifier() {
    return join(".", toInstance(), arrayPath(MAP.getUri(), 2), path(ID_EAN.getUri()), arrayPath(QUALIFIER));
  }

  private String toLocalIdValue() {
    return join(".", toInstance(), arrayPath(MAP.getUri(), 3), path(ID_LOCAL.getUri()),
      arrayPath(LOCAL_ID_VALUE));
  }

  private String toLocalIdAssigner() {
    return join(".", toInstance(), arrayPath(MAP.getUri(), 3), path(ID_LOCAL.getUri()),
      arrayPath(ASSIGNING_SOURCE));
  }

  private String toOtherIdValue() {
    return join(".", toInstance(), arrayPath(MAP.getUri(), 4), path(ID_UNKNOWN.getUri()), arrayPath(NAME));
  }

  private String toOtherIdQualifier() {
    return join(".", toInstance(), arrayPath(MAP.getUri(), 4), path(ID_UNKNOWN.getUri()), arrayPath(QUALIFIER));
  }

  private String toCarrierCode() {
    return join(".", toInstance(), arrayPath(CARRIER.getUri()), arrayPath(CODE));
  }

  private String toCarrierLink() {
    return join(".", toInstance(), arrayPath(CARRIER.getUri()), arrayPath(LINK));
  }

  private String toCarrierTerm() {
    return join(".", toInstance(), arrayPath(CARRIER.getUri()), arrayPath(TERM));
  }

  private String toCopyrightDate() {
    return join(".", toInstance(), arrayPath(COPYRIGHT.getUri()), arrayPath(DATE));
  }

  private String toMediaCode() {
    return join(".", toInstance(), arrayPath(MEDIA.getUri()), arrayPath(CODE));
  }

  private String toMediaLink() {
    return join(".", toInstance(), arrayPath(MEDIA.getUri()), arrayPath(LINK));
  }

  private String toMediaTerm() {
    return join(".", toInstance(), arrayPath(MEDIA.getUri()), arrayPath(TERM));
  }

  private String toWorkTargetAudience() {
    return join(".", toWork(), arrayPath(TARGET_AUDIENCE));
  }

  private String toWorkTableOfContents() {
    return join(".", toWork(), arrayPath(TABLE_OF_CONTENTS));
  }

  private String toWorkSummary() {
    return join(".", toWork(), arrayPath(SUMMARY));
  }

  private String toWorkLanguage() {
    return join(".", toWork(), arrayPath(LANGUAGE));
  }

  private String toWorkDeweySource() {
    return join(".", toWork(), arrayPath(CLASSIFICATION.getUri()), arrayPath(SOURCE));
  }

  private String toWorkDeweyCode() {
    return join(".", toWork(), arrayPath(CLASSIFICATION.getUri()), arrayPath(CODE));
  }

  private String toWorkContributorOrgLcnafId() {
    return join(".", toWork(), arrayPath(CONTRIBUTOR.getUri()), path(ORGANIZATION.getUri()), arrayPath(LCNAF_ID));
  }

  private String toWorkContributorOrgName() {
    return join(".", toWork(), arrayPath(CONTRIBUTOR.getUri()), path(ORGANIZATION.getUri()), arrayPath(NAME));
  }

  private String toWorkCreatorPersonLcnafId() {
    return join(".", toWork(), arrayPath(CREATOR.getUri()), path(PERSON.getUri()), arrayPath(LCNAF_ID));
  }

  private String toWorkCreatorPersonName() {
    return join(".", toWork(), arrayPath(CREATOR.getUri()), path(PERSON.getUri()), arrayPath(NAME));
  }

  private String toWorkContentTerm() {
    return join(".", toWork(), arrayPath(CONTENT.getUri()), arrayPath(TERM));
  }

  private String toWorkContentCode() {
    return join(".", toWork(), arrayPath(CONTENT.getUri()), arrayPath(CODE));
  }

  private String toWorkContentLink() {
    return join(".", toWork(), arrayPath(CONTENT.getUri()), arrayPath(LINK));
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

  private String arrayPath(String path, int index) {
    return format("['%s'][%d]", path, index);
  }

  private String arrayPath(String path) {
    return arrayPath(path, 0);
  }

}
