package org.folio.linked.data.e2e;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.model.ErrorCode.NOT_FOUND_ERROR;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.bibframeSampleResource;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getBibframeSample;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.BibframeConstants.ACCESS_LOCATION;
import static org.folio.linked.data.util.BibframeConstants.ACCESS_LOCATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.ASSIGNING_SOURCE;
import static org.folio.linked.data.util.BibframeConstants.CARRIER;
import static org.folio.linked.data.util.BibframeConstants.CARRIER_PRED;
import static org.folio.linked.data.util.BibframeConstants.CODE;
import static org.folio.linked.data.util.BibframeConstants.COPYRIGHT_DATE;
import static org.folio.linked.data.util.BibframeConstants.DATE;
import static org.folio.linked.data.util.BibframeConstants.DIMENSIONS;
import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.EAN;
import static org.folio.linked.data.util.BibframeConstants.EAN_VALUE;
import static org.folio.linked.data.util.BibframeConstants.EDITION_STATEMENT;
import static org.folio.linked.data.util.BibframeConstants.ID;
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
import static org.folio.linked.data.util.BibframeConstants.MEDIA;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;
import static org.folio.linked.data.util.BibframeConstants.MONOGRAPH;
import static org.folio.linked.data.util.BibframeConstants.NAME;
import static org.folio.linked.data.util.BibframeConstants.NON_SORT_NUM;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.OTHER_ID;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeConstants.PART_NAME;
import static org.folio.linked.data.util.BibframeConstants.PART_NUMBER;
import static org.folio.linked.data.util.BibframeConstants.PLACE;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PRODUCTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROJECTED_PROVISION_DATE;
import static org.folio.linked.data.util.BibframeConstants.PROVIDER_EVENT;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.folio.linked.data.domain.dto.BibframeResponse;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
public class BibframeControllerIT {

  public static final String BIBFRAME_URI = "/bibframe";
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
    var requestBuilder = post(BIBFRAME_URI)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getBibframeSample());

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = validateBibframeResponse(resultActions)
      .andReturn().getResponse().getContentAsString();

    var bibframeResponse = objectMapper.readValue(response, BibframeResponse.class);
    var persistedOptional = resourceRepo.findById(bibframeResponse.getId());
    assertThat(persistedOptional).isPresent();
    var bibframe = persistedOptional.get();
    validateMonographResource(bibframe);
    checkKafkaMessageSent(bibframe, null);
  }

  @Test
  void getBibframeById_shouldReturnExistedEntity() throws Exception {
    // given
    var existed = resourceRepo.save(monographTestService.createSampleMonograph());
    var requestBuilder = get(BIBFRAME_URI + "/" + existed.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    validateBibframeResponse(resultActions);
  }

  @Test
  void getBibframeById_shouldReturn404_ifNoExistedEntity() throws Exception {
    // given
    var notExistedId = randomLong();
    var requestBuilder = get(BIBFRAME_URI + "/" + notExistedId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isNotFound())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].message", equalTo("Bibframe record with given id ["
        + notExistedId + "] is not found")))
      .andExpect(jsonPath("errors[0].type", equalTo(NotFoundException.class.getSimpleName())))
      .andExpect(jsonPath("errors[0].code", equalTo(NOT_FOUND_ERROR.getValue())))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }

  @Test
  void getBibframeShortInfoPage_shouldReturnPageWithExistedEntities() throws Exception {
    // given
    var existed = Lists.newArrayList(
      resourceRepo.save(bibframeSampleResource(1L, monographTestService.getMonographType())),
      resourceRepo.save(bibframeSampleResource(2L, monographTestService.getMonographType())),
      resourceRepo.save(bibframeSampleResource(3L, monographTestService.getMonographType()))
    ).stream().sorted(comparing(Resource::getResourceHash)).toList();
    var requestBuilder = get(BIBFRAME_URI)
      .param(TYPE, monographTestService.getMonographType().getTypeUri())
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
      .andExpect(jsonPath("content[0].id", equalTo(existed.get(0).getResourceHash().intValue())))
      .andExpect(jsonPath("content[1].id", equalTo(existed.get(1).getResourceHash().intValue())))
      .andExpect(jsonPath("content[2].id", equalTo(existed.get(2).getResourceHash().intValue())));
  }

  @Test
  void deleteBibframeById_shouldDeleteRootResourceAndRootEdge() throws Exception {
    // given
    var existed = resourceRepo.save(monographTestService.createSampleMonograph());
    assertThat(resourceRepo.findById(existed.getResourceHash())).isPresent();
    assertThat(resourceRepo.count()).isEqualTo(23);
    assertThat(resourceEdgeRepository.count()).isEqualTo(22);
    var requestBuilder = delete(BIBFRAME_URI + "/" + existed.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()));

    // when
    mockMvc.perform(requestBuilder);

    // then
    assertThat(resourceRepo.findById(existed.getResourceHash())).isNotPresent();
    assertThat(resourceRepo.count()).isEqualTo(22);
    assertThat(resourceEdgeRepository.findById(existed.getOutgoingEdges().iterator().next().getId())).isNotPresent();
    assertThat(resourceEdgeRepository.count()).isEqualTo(21);
    checkKafkaMessageSent(null, existed.getResourceHash());
  }

  protected void checkKafkaMessageSent(Resource persisted, Long deleted) {
    // nothing to check without Folio profile
  }

  @NotNull
  private ResultActions validateBibframeResponse(ResultActions resultActions) throws Exception {
    return resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(ID, notNullValue()))
      .andExpect(jsonPath(TYPE, equalTo(MONOGRAPH)))
      .andExpect(jsonPath("$." + path(INSTANCE), notNullValue()))
      .andExpect(jsonPath("$." + toAccessLocationLink(), equalTo("accessLocationValue")))
      .andExpect(jsonPath("$." + toAccessLocationNote(), equalTo("accessLocationNote")))
      .andExpect(jsonPath("$." + toCarrierCode(), equalTo("carrier code")))
      .andExpect(jsonPath("$." + toCarrierLink(), equalTo("carrier link")))
      .andExpect(jsonPath("$." + toCarrierTerm(), equalTo("carrier 1")))
      .andExpect(jsonPath("$." + toCopyrightDate(), equalTo("copyright date")))
      .andExpect(jsonPath("$." + toDimensions(), equalTo("20 cm")))
      .andExpect(jsonPath("$." + toEanValue(), equalTo("ean value")))
      .andExpect(jsonPath("$." + toEanQualifier(), equalTo("ean qualifier")))
      .andExpect(jsonPath("$." + toEditionStatement(), equalTo("edition statement")))
      .andExpect(jsonPath("$." + toInstanceTitlePartName(), equalTo("Instance: partName")))
      .andExpect(jsonPath("$." + toInstanceTitlePartNumber(), equalTo("Instance: partNumber")))
      .andExpect(jsonPath("$." + toInstanceTitleMain(), equalTo("Instance: Laramie holds the range")))
      .andExpect(jsonPath("$." + toInstanceTitleNonSortNum(), equalTo("Instance: nonSortNum")))
      .andExpect(jsonPath("$." + toInstanceTitleSubtitle(), equalTo("Instance: subtitle")))
      .andExpect(jsonPath("$." + toIsbnValue(), equalTo("isbn value")))
      .andExpect(jsonPath("$." + toIsbnQualifier(), equalTo("isbn qualifier")))
      .andExpect(jsonPath("$." + toIsbnStatusValue(), equalTo("isbn status label")))
      .andExpect(jsonPath("$." + toIsbnStatusLink(), equalTo("isbn status link")))
      .andExpect(jsonPath("$." + toIssuance(), equalTo("single unit")))
      .andExpect(jsonPath("$." + toLccnValue(), equalTo("lccn value")))
      .andExpect(jsonPath("$." + toLccnStatusValue(), equalTo("lccn status label")))
      .andExpect(jsonPath("$." + toLccnStatusLink(), equalTo("lccn status link")))
      .andExpect(jsonPath("$." + toLocalIdValue(), equalTo("localId value")))
      .andExpect(jsonPath("$." + toLocalIdAssigner(), equalTo("localId assigner")))
      .andExpect(jsonPath("$." + toMediaCode(), equalTo("media code")))
      .andExpect(jsonPath("$." + toMediaLink(), equalTo("media link")))
      .andExpect(jsonPath("$." + toMediaTerm(), equalTo("unmediated")))
      .andExpect(jsonPath("$." + toOtherIdValue(), equalTo("otherId value")))
      .andExpect(jsonPath("$." + toOtherIdQualifier(), equalTo("otherId qualifier")))
      .andExpect(jsonPath("$." + toParallelTitlePartName(), equalTo("Parallel: partName")))
      .andExpect(jsonPath("$." + toParallelTitlePartNumber(), equalTo("Parallel: partNumber")))
      .andExpect(jsonPath("$." + toParallelTitleMain(), equalTo("Parallel: Laramie holds the range")))
      .andExpect(jsonPath("$." + toParallelTitleNote(), equalTo("Parallel: noteLabel")))
      .andExpect(jsonPath("$." + toParallelTitleDate(), equalTo("Parallel: date")))
      .andExpect(jsonPath("$." + toParallelTitleSubtitle(), equalTo("Parallel: subtitle")))
      .andExpect(jsonPath("$." + toProviderEventDate(PRODUCTION_PRED), equalTo("production date")))
      .andExpect(jsonPath("$." + toProviderEventName(PRODUCTION_PRED), equalTo("production name")))
      .andExpect(jsonPath("$." + toProviderEventPlaceName(PRODUCTION_PRED), equalTo("production place name")))
      .andExpect(jsonPath("$." + toProviderEventPlaceLink(PRODUCTION_PRED), equalTo("production place link")))
      .andExpect(jsonPath("$." + toProviderEventSimpleDate(PRODUCTION_PRED), equalTo("production simple date")))
      .andExpect(jsonPath("$." + toProviderEventSimplePlace(PRODUCTION_PRED), equalTo("production simple place")))
      .andExpect(jsonPath("$." + toProviderEventDate(PUBLICATION_PRED), equalTo("publication date")))
      .andExpect(jsonPath("$." + toProviderEventName(PUBLICATION_PRED), equalTo("publication name")))
      .andExpect(jsonPath("$." + toProviderEventPlaceName(PUBLICATION_PRED), equalTo("publication place name")))
      .andExpect(jsonPath("$." + toProviderEventPlaceLink(PUBLICATION_PRED), equalTo("publication place link")))
      .andExpect(jsonPath("$." + toProviderEventSimpleDate(PUBLICATION_PRED), equalTo("publication simple date")))
      .andExpect(jsonPath("$." + toProviderEventSimplePlace(PUBLICATION_PRED), equalTo("publication simple place")))
      .andExpect(jsonPath("$." + toProviderEventDate(DISTRIBUTION_PRED), equalTo("distribution date")))
      .andExpect(jsonPath("$." + toProviderEventName(DISTRIBUTION_PRED), equalTo("distribution name")))
      .andExpect(jsonPath("$." + toProviderEventPlaceName(DISTRIBUTION_PRED), equalTo("distribution place name")))
      .andExpect(jsonPath("$." + toProviderEventPlaceLink(DISTRIBUTION_PRED), equalTo("distribution place link")))
      .andExpect(jsonPath("$." + toProviderEventSimpleDate(DISTRIBUTION_PRED), equalTo("distribution simple date")))
      .andExpect(jsonPath("$." + toProviderEventSimplePlace(DISTRIBUTION_PRED), equalTo("distribution simple place")))
      .andExpect(jsonPath("$." + toProviderEventDate(MANUFACTURE_PRED), equalTo("manufacture date")))
      .andExpect(jsonPath("$." + toProviderEventName(MANUFACTURE_PRED), equalTo("manufacture name")))
      .andExpect(jsonPath("$." + toProviderEventPlaceName(MANUFACTURE_PRED), equalTo("manufacture place name")))
      .andExpect(jsonPath("$." + toProviderEventPlaceLink(MANUFACTURE_PRED), equalTo("manufacture place link")))
      .andExpect(jsonPath("$." + toProviderEventSimpleDate(MANUFACTURE_PRED), equalTo("manufacture simple date")))
      .andExpect(jsonPath("$." + toProviderEventSimplePlace(MANUFACTURE_PRED), equalTo("manufacture simple place")))
      .andExpect(jsonPath("$." + toProjectedProvisionDate(), equalTo("projected provision date")))
      .andExpect(jsonPath("$." + toResponsibilityStatement(), equalTo("responsibility statement")))
      .andExpect(jsonPath("$." + toVariantTitlePartName(), equalTo("Variant: partName")))
      .andExpect(jsonPath("$." + toVariantTitlePartNumber(), equalTo("Variant: partNumber")))
      .andExpect(jsonPath("$." + toVariantTitleMain(), equalTo("Variant: Laramie holds the range")))
      .andExpect(jsonPath("$." + toVariantTitleNote(), equalTo("Variant: noteLabel")))
      .andExpect(jsonPath("$." + toVariantTitleDate(), equalTo("Variant: date")))
      .andExpect(jsonPath("$." + toVariantTitleSubtitle(), equalTo("Variant: subtitle")))
      .andExpect(jsonPath("$." + toVariantTitleType(), equalTo("Variant: variantType")));
  }

  private void validateMonographResource(Resource resource) {
    assertThat(resource.getType().getTypeUri()).isEqualTo(MONOGRAPH);
    assertThat(resource.getLabel()).isEqualTo("Instance: Laramie holds the range");
    assertThat(resource.getDoc()).isNull();
    assertThat(resource.getResourceHash()).isNotNull();
    assertThat(resource.getOutgoingEdges()).hasSize(1);
    validateInstance(resource.getOutgoingEdges().iterator().next(), resource);
  }

  private void validateInstance(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(INSTANCE);
    var instance = edge.getTarget();
    assertThat(instance.getLabel()).isEqualTo("Instance: Laramie holds the range");
    assertThat(instance.getType().getTypeUri()).isEqualTo(INSTANCE);
    assertThat(instance.getResourceHash()).isNotNull();
    assertThat(instance.getDoc().size()).isEqualTo(6);
    validateLiteral(instance, DIMENSIONS, "20 cm");
    validateLiteral(instance, EDITION_STATEMENT, "edition statement");
    validateLiteral(instance, RESPONSIBILITY_STATEMENT, "responsibility statement");
    validateLiteral(instance, COPYRIGHT_DATE, "copyright date");
    validateLiteral(instance, PROJECTED_PROVISION_DATE, "projected provision date");
    validateLiteral(instance, ISSUANCE, "single unit");
    assertThat(instance.getOutgoingEdges()).hasSize(15);

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
    validateTriple(edgeIterator.next(), instance, MEDIA_PRED, MEDIA, "unmediated");
    validateTriple(edgeIterator.next(), instance, CARRIER_PRED, CARRIER, "carrier 1");
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
    assertThat(title.getLabel()).isEqualTo(title.getDoc().get(MAIN_TITLE).get(0).asText());
    assertThat(title.getType().getTypeUri()).isEqualTo(type);
    assertThat(title.getResourceHash()).isNotNull();
    assertThat(title.getDoc().get(PART_NAME).size()).isEqualTo(1);
    assertThat(title.getDoc().get(PART_NAME).get(0).asText()).isEqualTo(prefix + "partName");
    assertThat(title.getDoc().get(PART_NUMBER).size()).isEqualTo(1);
    assertThat(title.getDoc().get(PART_NUMBER).get(0).asText()).isEqualTo(prefix + "partNumber");
    assertThat(title.getDoc().get(MAIN_TITLE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(MAIN_TITLE).get(0).asText()).isEqualTo(prefix + "Laramie holds the range");
    assertThat(title.getDoc().get(SUBTITLE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(SUBTITLE).get(0).asText()).isEqualTo(prefix + "subtitle");
  }

  private void validateProviderEvent(ResourceEdge edge, Resource source, String predicate) {
    var type = predicate.substring(predicate.indexOf("marc/") + 5);
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(predicate);
    var providerEvent = edge.getTarget();
    assertThat(providerEvent.getLabel()).isEqualTo(providerEvent.getDoc().get(SIMPLE_PLACE).get(0).asText());
    assertThat(providerEvent.getType().getTypeUri()).isEqualTo(PROVIDER_EVENT);
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
    validatePlace(providerEvent.getOutgoingEdges().iterator().next(), providerEvent, type);
  }

  private void validatePlace(ResourceEdge edge, Resource source, String prefix) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(PLACE_PRED);
    var place = edge.getTarget();
    assertThat(place.getLabel()).isEqualTo(place.getDoc().get(NAME).get(0).asText());
    assertThat(place.getType().getTypeUri()).isEqualTo(PLACE);
    assertThat(place.getResourceHash()).isNotNull();
    assertThat(place.getDoc().size()).isEqualTo(2);
    assertThat(place.getDoc().get(NAME).size()).isEqualTo(1);
    assertThat(place.getDoc().get(NAME).get(0).asText()).isEqualTo(prefix + " place name");
    assertThat(place.getDoc().get(LINK).size()).isEqualTo(1);
    assertThat(place.getDoc().get(LINK).get(0).asText()).isEqualTo(prefix + " place link");
    assertThat(place.getOutgoingEdges()).isEmpty();
  }

  private void validateLccn(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(MAP_PRED);
    var lccn = edge.getTarget();
    assertThat(lccn.getLabel()).isEqualTo(lccn.getDoc().get(NAME).get(0).asText());
    assertThat(lccn.getType().getTypeUri()).isEqualTo(LCCN);
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
    assertThat(isbn.getLabel()).isEqualTo(isbn.getDoc().get(NAME).get(0).asText());
    assertThat(isbn.getType().getTypeUri()).isEqualTo(ISBN);
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
    assertThat(ean.getLabel()).isEqualTo(ean.getDoc().get(EAN_VALUE).get(0).asText());
    assertThat(ean.getType().getTypeUri()).isEqualTo(EAN);
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
    assertThat(localId.getLabel()).isEqualTo(localId.getDoc().get(LOCAL_ID_VALUE).get(0).asText());
    assertThat(localId.getType().getTypeUri()).isEqualTo(LOCAL_ID);
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
    assertThat(otherId.getLabel()).isEqualTo(otherId.getDoc().get(NAME).get(0).asText());
    assertThat(otherId.getType().getTypeUri()).isEqualTo(OTHER_ID);
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
    assertThat(status.getLabel()).isEqualTo(status.getDoc().get(LABEL).get(0).asText());
    assertThat(status.getType().getTypeUri()).isEqualTo(STATUS);
    assertThat(status.getResourceHash()).isNotNull();
    assertThat(status.getDoc().size()).isEqualTo(2);
    assertThat(status.getDoc().get(LABEL).size()).isEqualTo(1);
    assertThat(status.getDoc().get(LABEL).get(0).asText()).isEqualTo(prefix + " status label");
    assertThat(status.getDoc().get(LINK).size()).isEqualTo(1);
    assertThat(status.getDoc().get(LINK).get(0).asText()).isEqualTo(prefix + " status link");
    assertThat(status.getOutgoingEdges()).isEmpty();
  }

  private void validateAccessLocation(ResourceEdge edge, Resource source) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(ACCESS_LOCATION_PRED);
    var locator = edge.getTarget();
    assertThat(locator.getLabel()).isEqualTo(locator.getDoc().get(LINK).get(0).asText());
    assertThat(locator.getType().getTypeUri()).isEqualTo(ACCESS_LOCATION);
    assertThat(locator.getResourceHash()).isNotNull();
    assertThat(locator.getDoc().size()).isEqualTo(2);
    assertThat(locator.getDoc().get(LINK).size()).isEqualTo(1);
    assertThat(locator.getDoc().get(LINK).get(0).asText()).isEqualTo("accessLocationValue");
    assertThat(locator.getDoc().get(NOTE).size()).isEqualTo(1);
    assertThat(locator.getDoc().get(NOTE).get(0).asText()).isEqualTo("accessLocationNote");
    assertThat(locator.getOutgoingEdges()).isEmpty();
  }

  private void validateTriple(ResourceEdge edge, Resource source, String pred, String type, String term) {
    var prefix = pred.substring(pred.lastIndexOf("/") + 1);
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(source);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(pred);
    var media = edge.getTarget();
    assertThat(media.getLabel()).isEqualTo(media.getDoc().get(TERM).get(0).asText());
    assertThat(media.getType().getTypeUri()).isEqualTo(type);
    assertThat(media.getResourceHash()).isNotNull();
    assertThat(media.getDoc().size()).isEqualTo(3);
    assertThat(media.getDoc().get(CODE).size()).isEqualTo(1);
    assertThat(media.getDoc().get(CODE).get(0).asText()).isEqualTo(prefix + " code");
    assertThat(media.getDoc().get(TERM).size()).isEqualTo(1);
    assertThat(media.getDoc().get(TERM).get(0).asText()).isEqualTo(term);
    assertThat(media.getDoc().get(LINK).size()).isEqualTo(1);
    assertThat(media.getDoc().get(LINK).get(0).asText()).isEqualTo(prefix + " link");
    assertThat(media.getOutgoingEdges()).isEmpty();
  }

  private String toDimensions() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(DIMENSIONS));
  }

  private String toEditionStatement() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(EDITION_STATEMENT));
  }

  private String toAccessLocationLink() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(ACCESS_LOCATION_PRED), arrayPath(LINK));
  }

  private String toAccessLocationNote() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(ACCESS_LOCATION_PRED), arrayPath(NOTE));
  }

  private String toResponsibilityStatement() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(RESPONSIBILITY_STATEMENT));
  }

  private String toCopyrightDate() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(COPYRIGHT_DATE));
  }

  private String toProjectedProvisionDate() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(PROJECTED_PROVISION_DATE));
  }

  private String toInstanceTitlePartName() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE),
      arrayPath(PART_NAME));
  }

  private String toInstanceTitlePartNumber() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE),
      arrayPath(PART_NUMBER));
  }

  private String toInstanceTitleMain() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE),
      arrayPath(MAIN_TITLE));
  }

  private String toInstanceTitleNonSortNum() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE),
      arrayPath(NON_SORT_NUM));
  }

  private String toInstanceTitleSubtitle() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE),
      arrayPath(SUBTITLE));
  }

  private String toIssuance() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(ISSUANCE));
  }

  private String toParallelTitlePartName() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(PART_NAME));
  }

  private String toParallelTitlePartNumber() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(PART_NUMBER));
  }

  private String toParallelTitleMain() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(MAIN_TITLE));
  }

  private String toParallelTitleDate() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(DATE));
  }

  private String toParallelTitleSubtitle() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(SUBTITLE));
  }

  private String toParallelTitleNote() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(NOTE));
  }

  private String toVariantTitlePartName() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(PART_NAME));
  }

  private String toVariantTitlePartNumber() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(PART_NUMBER));
  }

  private String toVariantTitleMain() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(MAIN_TITLE));
  }

  private String toVariantTitleDate() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(DATE));
  }

  private String toVariantTitleSubtitle() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(SUBTITLE));
  }

  private String toVariantTitleType() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(VARIANT_TYPE));
  }

  private String toVariantTitleNote() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(NOTE));
  }

  private String toProviderEventDate(String predicate) {
    return String.join(".", arrayPath(INSTANCE), arrayPath(predicate), arrayPath(DATE));
  }

  private String toProviderEventName(String predicate) {
    return String.join(".", arrayPath(INSTANCE), arrayPath(predicate), arrayPath(NAME));
  }

  private String toProviderEventPlaceName(String predicate) {
    return String.join(".", arrayPath(INSTANCE), arrayPath(predicate), arrayPath(PLACE_PRED),
      arrayPath(NAME));
  }

  private String toProviderEventPlaceLink(String predicate) {
    return String.join(".", arrayPath(INSTANCE), arrayPath(predicate), arrayPath(PLACE_PRED),
      arrayPath(LINK));
  }

  private String toProviderEventSimpleDate(String predicate) {
    return String.join(".", arrayPath(INSTANCE), arrayPath(predicate), arrayPath(SIMPLE_DATE));
  }

  private String toProviderEventSimplePlace(String predicate) {
    return String.join(".", arrayPath(INSTANCE), arrayPath(predicate), arrayPath(SIMPLE_PLACE));
  }

  private String toLccnValue() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MAP_PRED), path(LCCN), arrayPath(NAME));
  }

  private String toLccnStatusValue() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MAP_PRED), path(LCCN), arrayPath(STATUS_PRED),
      arrayPath(LABEL));
  }

  private String toLccnStatusLink() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MAP_PRED), path(LCCN), arrayPath(STATUS_PRED),
      arrayPath(LINK));
  }

  private String toIsbnValue() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MAP_PRED, 1), path(ISBN), arrayPath(NAME));
  }

  private String toIsbnQualifier() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MAP_PRED, 1), path(ISBN), arrayPath(QUALIFIER));
  }

  private String toIsbnStatusValue() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MAP_PRED, 1), path(ISBN), arrayPath(STATUS_PRED),
      arrayPath(LABEL));
  }

  private String toIsbnStatusLink() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MAP_PRED, 1), path(ISBN), arrayPath(STATUS_PRED),
      arrayPath(LINK));
  }

  private String toEanValue() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MAP_PRED, 2), path(EAN), arrayPath(EAN_VALUE));
  }

  private String toEanQualifier() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MAP_PRED, 2), path(EAN), arrayPath(QUALIFIER));
  }

  private String toLocalIdValue() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MAP_PRED, 3), path(LOCAL_ID), arrayPath(LOCAL_ID_VALUE));
  }

  private String toLocalIdAssigner() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MAP_PRED, 3), path(LOCAL_ID), arrayPath(ASSIGNING_SOURCE));
  }

  private String toOtherIdValue() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MAP_PRED, 4), path(OTHER_ID), arrayPath(NAME));
  }

  private String toOtherIdQualifier() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MAP_PRED, 4), path(OTHER_ID), arrayPath(QUALIFIER));
  }

  private String toCarrierCode() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(CARRIER_PRED, 0), arrayPath(CODE));
  }

  private String toCarrierLink() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(CARRIER_PRED, 0), arrayPath(LINK));
  }

  private String toCarrierTerm() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(CARRIER_PRED, 0), arrayPath(TERM));
  }

  private String toMediaCode() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MEDIA_PRED, 0), arrayPath(CODE));
  }

  private String toMediaLink() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MEDIA_PRED, 0), arrayPath(LINK));
  }

  private String toMediaTerm() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MEDIA_PRED, 0), arrayPath(TERM));
  }

  private String toErrorType() {
    return String.join(".", arrayPath("errors"), path("type"));
  }

  private String toErrorCode() {
    return String.join(".", arrayPath("errors"), path("code"));
  }

  private String toErrorKey() {
    return String.join(".", arrayPath("errors"), arrayPath("parameters"), path("key"));
  }

  private String toErrorValue() {
    return String.join(".", arrayPath("errors"), arrayPath("parameters"), path("value"));
  }

  private String filterPath(String... paths) {
    return String.format("[?(@.%s)]", String.join(".", paths));
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
