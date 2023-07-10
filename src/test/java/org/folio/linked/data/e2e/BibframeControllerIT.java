package org.folio.linked.data.e2e;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.linked.data.model.ErrorCode.NOT_FOUND_ERROR;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getResourceSample;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.test.TestUtil.randomResource;
import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.CARRIER_PRED;
import static org.folio.linked.data.util.BibframeConstants.CARRIER_URL;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_URL;
import static org.folio.linked.data.util.BibframeConstants.DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.DATE_URL;
import static org.folio.linked.data.util.BibframeConstants.DIMENSIONS_URL;
import static org.folio.linked.data.util.BibframeConstants.EXTENT;
import static org.folio.linked.data.util.BibframeConstants.EXTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.EXTENT_URL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LCCN;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LCCN_URL;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_URL;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE_URL;
import static org.folio.linked.data.util.BibframeConstants.ITEM_URL;
import static org.folio.linked.data.util.BibframeConstants.LABEL_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_URL;
import static org.folio.linked.data.util.BibframeConstants.MONOGRAPH;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PERSON;
import static org.folio.linked.data.util.BibframeConstants.PERSON_URL;
import static org.folio.linked.data.util.BibframeConstants.PLACE;
import static org.folio.linked.data.util.BibframeConstants.PLACE_COMPONENTS;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PLACE_URL;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_ID;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_LABEL;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_URI;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION_URL;
import static org.folio.linked.data.util.BibframeConstants.ROLE;
import static org.folio.linked.data.util.BibframeConstants.ROLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ROLE_URL;
import static org.folio.linked.data.util.BibframeConstants.SAME_AS_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.VALUE_URL;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.WORK_URL;
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
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.MonographTestService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
class BibframeControllerIT {

  public static final String BIBFRAMES_URL = "/bibframes";

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


  @AfterEach
  public void clean() {
    resourceEdgeRepository.deleteAll();
    resourceRepo.deleteAll();
  }

  @Test
  void createMonographInstanceBibframe_shouldSaveEntityCorrectly() throws Exception {
    // given
    var requestBuilder = post(BIBFRAMES_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(getResourceSample());

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = validateSampleBibframeResponse(resultActions)
      .andReturn().getResponse().getContentAsString();

    var bibframeResponse = objectMapper.readValue(response, BibframeResponse.class);
    var persistedOptional = resourceRepo.findById(bibframeResponse.getId());
    assertThat(persistedOptional.isPresent()).isTrue();
    var monograph = persistedOptional.get();
    validateSampleMonographEntity(monograph);
  }


  @Test
  void getBibframeById_shouldReturnExistedEntity() throws Exception {
    // given
    var existed = resourceRepo.save(monographTestService.createSampleMonograph());
    var requestBuilder = get(BIBFRAMES_URL + "/" + existed.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    validateSampleBibframeResponse(resultActions);
  }


  @Test
  void getBibframeById_shouldReturn404_ifNoExistedEntity() throws Exception {
    // given
    var notExistedId = randomLong();
    var requestBuilder = get(BIBFRAMES_URL + "/" + notExistedId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

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
  void getBibframesShortInfoPage_shouldReturnPageWithExistedEntities() throws Exception {
    // given
    var existed = Lists.newArrayList(
      resourceRepo.save(randomResource(1L, monographTestService.getMonographProfile())),
      resourceRepo.save(randomResource(2L, monographTestService.getMonographProfile())),
      resourceRepo.save(randomResource(3L, monographTestService.getMonographProfile()))
    ).stream().sorted(comparing(Resource::getResourceHash)).toList();
    var requestBuilder = get(BIBFRAMES_URL)
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
      .andExpect(jsonPath("content[0].id", equalTo(existed.get(0).getResourceHash().intValue())))
      .andExpect(jsonPath("content[1].id", equalTo(existed.get(1).getResourceHash().intValue())))
      .andExpect(jsonPath("content[2].id", equalTo(existed.get(2).getResourceHash().intValue())));
  }

  @Test
  void deleteBibframeById_shouldDeleteRootResourceAndAllEdges() throws Exception {
    // given
    var existed = resourceRepo.save(monographTestService.createSampleMonograph());
    assertThat(resourceRepo.findById(existed.getResourceHash()).isPresent()).isTrue();
    assertThat(resourceRepo.count()).isEqualTo(16);
    assertThat(resourceEdgeRepository.count()).isEqualTo(15);
    var requestBuilder = delete(BIBFRAMES_URL + "/" + existed.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    mockMvc.perform(requestBuilder);

    // then
    assertThat(resourceRepo.findById(existed.getResourceHash()).isPresent()).isFalse();
    assertThat(resourceRepo.count()).isEqualTo(15);
    assertThat(resourceEdgeRepository.count()).isEqualTo(0);
  }

  @NotNull
  private ResultActions validateSampleBibframeResponse(ResultActions resultActions) throws Exception {
    return resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id", notNullValue()))
      .andExpect(jsonPath("$." + path(WORK_URL)).doesNotExist())
      .andExpect(jsonPath("$." + path(ITEM_URL)).doesNotExist())
      .andExpect(jsonPath("$." + path(INSTANCE_URL), notNullValue()))
      .andExpect(jsonPath("$." + toCarrierUri(), equalTo(CARRIER_URL)))
      .andExpect(jsonPath("$." + toCarrierLabel(), equalTo("volume")))
      .andExpect(jsonPath("$." + toContributionAgentUri(), equalTo("http://id.loc.gov/authorities/names/no98072015")))
      .andExpect(jsonPath("$." + toContributionAgentLabel(), equalTo("Test and Evaluation Year-2000 Team (U.S.)")))
      .andExpect(jsonPath("$." + toContributionRoleId(), equalTo(ROLE)))
      .andExpect(jsonPath("$." + toContributionRoleUri(), equalTo(ROLE_URL)))
      .andExpect(jsonPath("$." + toContributionRoleLabel(), equalTo("Author")))
      .andExpect(jsonPath("$." + toDimensions(), equalTo("20 cm")))
      .andExpect(jsonPath("$." + toExtentLabel(), equalTo("vi, 374 pages, 4 unnumbered leaves of plates")))
      .andExpect(jsonPath("$." + toIdentifiedByLccn(), equalTo("21014542")))
      .andExpect(jsonPath("$." + toIssuanceLabel(), equalTo("single unit")))
      .andExpect(jsonPath("$." + toIssuanceUri(), equalTo(ISSUANCE_URL)))
      .andExpect(jsonPath("$." + toInstanceTitle(), equalTo("Laramie holds the range")))
      .andExpect(jsonPath("$." + toParallelTitle(), equalTo("Parallel: Laramie holds the range")))
      .andExpect(jsonPath("$." + toVariantTitle(), equalTo("Variant: Laramie holds the range")))
      .andExpect(jsonPath("$." + toMediaLabel(), equalTo("unmediated")))
      .andExpect(jsonPath("$." + toMediaUri(), equalTo(MEDIA_URL)))
      //.andExpect(jsonPath("$." + toNoteId(), equalTo(NOTE)))
      //.andExpect(jsonPath("$." + toNoteLabel(), equalTo("some note")))
      //.andExpect(jsonPath("$." + toNoteUri(), equalTo(NOTE_URL)))
      .andExpect(jsonPath("$." + toPublicationSimpleAgent(), equalTo("Charles Scribner's Sons")))
      .andExpect(jsonPath("$." + toPublicationSimpleDate(), equalTo("1921")))
      .andExpect(jsonPath("$." + toPublicationSimplePlace(), equalTo("New York")))
      .andExpect(jsonPath("$." + toPublicationPlaceId(), equalTo(PLACE)))
      .andExpect(jsonPath("$." + toPublicationPlaceLabel(), equalTo("New York (State)")))
      .andExpect(jsonPath("$." + toPublicationPlaceUri(), equalTo(PLACE_URL)))
      .andExpect(jsonPath("$." + toPublicationDate(), equalTo("1921")));
  }

  private void validateSampleMonographEntity(Resource monograph) {
    assertThat(monograph.getType().getSimpleLabel()).isEqualTo(MONOGRAPH);
    assertThat(monograph.getLabel()).isEqualTo(MONOGRAPH);
    assertThat(monograph.getDoc()).isNull();
    assertThat(monograph.getResourceHash()).isNotNull();
    assertThat(monograph.getOutgoingEdges().size()).isEqualTo(1);
    validateSampleInstance(monograph.getOutgoingEdges().iterator().next(), monograph);
  }

  private void validateSampleInstance(ResourceEdge instanceEdge, Resource monograph) {
    assertThat(instanceEdge.getId()).isNotNull();
    assertThat(instanceEdge.getSource()).isEqualTo(monograph);
    assertThat(instanceEdge.getPredicate().getLabel()).isEqualTo(INSTANCE_URL);
    var instance = instanceEdge.getTarget();
    assertThat(instance.getLabel()).isEqualTo(INSTANCE_URL);
    assertThat(instance.getType().getSimpleLabel()).isEqualTo(INSTANCE);
    assertThat(instance.getResourceHash()).isNotNull();
    assertThat(instance.getDoc().size()).isEqualTo(1);
    assertThat(instance.getDoc().get(DIMENSIONS_URL).size()).isEqualTo(1);
    assertThat(instance.getDoc().get(DIMENSIONS_URL).get(0).asText()).isEqualTo("20 cm");
    assertThat(instance.getOutgoingEdges().size()).isEqualTo(10);

    var instanceEdgeIterator = instance.getOutgoingEdges().iterator();
    validateSampleInstanceTitle(instanceEdgeIterator.next(), instance);
    validateSampleParallelTitle(instanceEdgeIterator.next(), instance);
    validateSampleVariantTitle(instanceEdgeIterator.next(), instance);
    validateSamplePublication(instanceEdgeIterator.next(), instance);
    validateSampleContribution(instanceEdgeIterator.next(), instance);
    validateSampleIdentifiedByLccn(instanceEdgeIterator.next(), instance);
    validateSampleExtent(instanceEdgeIterator.next(), instance);
    validateSampleIssuance(instanceEdgeIterator.next(), instance);
    validateSampleMedia(instanceEdgeIterator.next(), instance);
    validateSampleCarrier(instanceEdgeIterator.next(), instance);
  }

  private void validateSampleIdentifiedByLccn(ResourceEdge identifiedByLccnEdge, Resource instance) {
    assertThat(identifiedByLccnEdge.getId()).isNotNull();
    assertThat(identifiedByLccnEdge.getSource()).isEqualTo(instance);
    assertThat(identifiedByLccnEdge.getPredicate().getLabel()).isEqualTo(IDENTIFIED_BY_PRED);
    var identifiedByLccn = identifiedByLccnEdge.getTarget();
    assertThat(identifiedByLccn.getLabel()).isEqualTo(IDENTIFIERS_LCCN_URL);
    assertThat(identifiedByLccn.getType().getSimpleLabel()).isEqualTo(IDENTIFIERS_LCCN);
    assertThat(identifiedByLccn.getResourceHash()).isNotNull();
    assertThat(identifiedByLccn.getDoc().size()).isEqualTo(1);
    assertThat(identifiedByLccn.getDoc().get(VALUE_URL).size()).isEqualTo(1);
    assertThat(identifiedByLccn.getDoc().get(VALUE_URL).get(0).asText()).isEqualTo("21014542");
    assertThat(identifiedByLccn.getOutgoingEdges().isEmpty()).isTrue();
  }

  private void validateSampleCarrier(ResourceEdge carrierEdge, Resource instance) {
    assertThat(carrierEdge.getId()).isNotNull();
    assertThat(carrierEdge.getSource()).isEqualTo(instance);
    assertThat(carrierEdge.getPredicate().getLabel()).isEqualTo(CARRIER_PRED);
    var carrier = carrierEdge.getTarget();
    assertThat(carrier.getLabel()).isEqualTo("volume");
    assertThat(carrier.getType().getTypeUri()).isEqualTo(CARRIER_URL);
    assertThat(carrier.getResourceHash()).isNotNull();
    assertThat(carrier.getDoc().size()).isEqualTo(2);
    assertThat(carrier.getDoc().get(PROPERTY_URI).asText()).isEqualTo(CARRIER_URL);
    assertThat(carrier.getDoc().get(PROPERTY_LABEL).asText()).isEqualTo("volume");
    assertThat(carrier.getOutgoingEdges().isEmpty()).isTrue();
  }

  private void validateSampleInstanceTitle(ResourceEdge instanceTitleEdge, Resource instance) {
    assertThat(instanceTitleEdge.getId()).isNotNull();
    assertThat(instanceTitleEdge.getSource()).isEqualTo(instance);
    assertThat(instanceTitleEdge.getPredicate().getLabel()).isEqualTo(TITLE_PRED);
    var instanceTitle = instanceTitleEdge.getTarget();
    assertThat(instanceTitle.getLabel()).isEqualTo(INSTANCE_TITLE_URL);
    assertThat(instanceTitle.getType().getSimpleLabel()).isEqualTo(INSTANCE_TITLE);
    assertThat(instanceTitle.getResourceHash()).isNotNull();
    assertThat(instanceTitle.getDoc().size()).isEqualTo(1);
    assertThat(instanceTitle.getDoc().get(MAIN_TITLE_URL).size()).isEqualTo(1);
    assertThat(instanceTitle.getDoc().get(MAIN_TITLE_URL).get(0).asText()).isEqualTo("Laramie holds the range");
    assertThat(instanceTitle.getOutgoingEdges().isEmpty()).isTrue();
  }

  private void validateSampleParallelTitle(ResourceEdge parallelTitleEdge, Resource instance) {
    assertThat(parallelTitleEdge.getId()).isNotNull();
    assertThat(parallelTitleEdge.getSource()).isEqualTo(instance);
    assertThat(parallelTitleEdge.getPredicate().getLabel()).isEqualTo(TITLE_PRED);
    var parallelTitle = parallelTitleEdge.getTarget();
    assertThat(parallelTitle.getLabel()).isEqualTo(PARALLEL_TITLE_URL);
    assertThat(parallelTitle.getType().getSimpleLabel()).isEqualTo(PARALLEL_TITLE);
    assertThat(parallelTitle.getResourceHash()).isNotNull();
    assertThat(parallelTitle.getDoc().size()).isEqualTo(1);
    assertThat(parallelTitle.getDoc().get(MAIN_TITLE_URL).size()).isEqualTo(1);
    assertThat(parallelTitle.getDoc().get(MAIN_TITLE_URL).get(0).asText()).isEqualTo("Parallel: Laramie holds the range");
    assertThat(parallelTitle.getOutgoingEdges().isEmpty()).isTrue();
  }

  private void validateSampleVariantTitle(ResourceEdge variantTitleEdge, Resource instance) {
    assertThat(variantTitleEdge.getId()).isNotNull();
    assertThat(variantTitleEdge.getSource()).isEqualTo(instance);
    assertThat(variantTitleEdge.getPredicate().getLabel()).isEqualTo(TITLE_PRED);
    var variantTitle = variantTitleEdge.getTarget();
    assertThat(variantTitle.getLabel()).isEqualTo(VARIANT_TITLE_URL);
    assertThat(variantTitle.getType().getSimpleLabel()).isEqualTo(VARIANT_TITLE);
    assertThat(variantTitle.getResourceHash()).isNotNull();
    assertThat(variantTitle.getDoc().size()).isEqualTo(1);
    assertThat(variantTitle.getDoc().get(MAIN_TITLE_URL).size()).isEqualTo(1);
    assertThat(variantTitle.getDoc().get(MAIN_TITLE_URL).get(0).asText()).isEqualTo("Variant: Laramie holds the range");
    assertThat(variantTitle.getOutgoingEdges().isEmpty()).isTrue();
  }

  private void validateSampleMedia(ResourceEdge mediaEdge, Resource instance) {
    assertThat(mediaEdge.getId()).isNotNull();
    assertThat(mediaEdge.getSource()).isEqualTo(instance);
    assertThat(mediaEdge.getPredicate().getLabel()).isEqualTo(MEDIA_PRED);
    var media = mediaEdge.getTarget();
    assertThat(media.getLabel()).isEqualTo("unmediated");
    assertThat(media.getType().getTypeUri()).isEqualTo(MEDIA_URL);
    assertThat(media.getResourceHash()).isNotNull();
    assertThat(media.getDoc().size()).isEqualTo(2);
    assertThat(media.getDoc().get(PROPERTY_URI).asText()).isEqualTo(MEDIA_URL);
    assertThat(media.getDoc().get(PROPERTY_LABEL).asText()).isEqualTo("unmediated");
    assertThat(media.getOutgoingEdges().isEmpty()).isTrue();
  }

  private void validateSampleIssuance(ResourceEdge issuanceEdge, Resource instance) {
    assertThat(issuanceEdge.getId()).isNotNull();
    assertThat(issuanceEdge.getSource()).isEqualTo(instance);
    assertThat(issuanceEdge.getPredicate().getLabel()).isEqualTo(ISSUANCE_PRED);
    var issuance = issuanceEdge.getTarget();
    assertThat(issuance.getLabel()).isEqualTo("single unit");
    assertThat(issuance.getType().getTypeUri()).isEqualTo(ISSUANCE_URL);
    assertThat(issuance.getResourceHash()).isNotNull();
    assertThat(issuance.getDoc().size()).isEqualTo(2);
    assertThat(issuance.getDoc().get(PROPERTY_URI).asText()).isEqualTo(ISSUANCE_URL);
    assertThat(issuance.getDoc().get(PROPERTY_LABEL).asText()).isEqualTo("single unit");
    assertThat(issuance.getOutgoingEdges().isEmpty()).isTrue();
  }

  private void validateSampleExtent(ResourceEdge extentEdge, Resource instance) {
    assertThat(extentEdge.getId()).isNotNull();
    assertThat(extentEdge.getSource()).isEqualTo(instance);
    assertThat(extentEdge.getPredicate().getLabel()).isEqualTo(EXTENT_PRED);
    var extent = extentEdge.getTarget();
    assertThat(extent.getLabel()).isEqualTo(EXTENT_URL);
    assertThat(extent.getType().getSimpleLabel()).isEqualTo(EXTENT);
    assertThat(extent.getResourceHash()).isNotNull();
    assertThat(extent.getDoc().size()).isEqualTo(1);
    assertThat(extent.getDoc().get(LABEL_PRED).size()).isEqualTo(1);
    assertThat(extent.getDoc().get(LABEL_PRED).get(0).asText())
      .isEqualTo("vi, 374 pages, 4 unnumbered leaves of plates");
    assertThat(extent.getOutgoingEdges().isEmpty()).isTrue();
  }

  private void validateSampleContribution(ResourceEdge contributionEdge, Resource instance) {
    assertThat(contributionEdge.getId()).isNotNull();
    assertThat(contributionEdge.getSource()).isEqualTo(instance);
    assertThat(contributionEdge.getPredicate().getLabel()).isEqualTo(CONTRIBUTION_PRED);
    var contribution = contributionEdge.getTarget();
    assertThat(contribution.getLabel()).isEqualTo(CONTRIBUTION_URL);
    assertThat(contribution.getType().getTypeUri()).isEqualTo(CONTRIBUTION_URL);
    assertThat(contribution.getResourceHash()).isNotNull();
    assertThat(contribution.getDoc()).isNull();
    assertThat(contribution.getOutgoingEdges().size()).isEqualTo(2);
    var contributionEdgeIterator = contribution.getOutgoingEdges().iterator();
    validateSampleContributionAgent(contributionEdgeIterator.next(), contribution);
    validateSampleContributionRole(contributionEdgeIterator.next(), contribution);
  }

  private void validateSampleContributionRole(ResourceEdge contributionRoleEdge, Resource contribution) {
    assertThat(contributionRoleEdge.getId()).isNotNull();
    assertThat(contributionRoleEdge.getSource()).isEqualTo(contribution);
    assertThat(contributionRoleEdge.getPredicate().getLabel()).isEqualTo(ROLE_PRED);
    var contributionRole = contributionRoleEdge.getTarget();
    assertThat(contributionRole.getLabel()).isEqualTo("Author");
    assertThat(contributionRole.getType().getTypeUri()).isEqualTo(ROLE_URL);
    assertThat(contributionRole.getResourceHash()).isNotNull();
    assertThat(contributionRole.getDoc().size()).isEqualTo(3);
    assertThat(contributionRole.getDoc().get(PROPERTY_URI).asText()).isEqualTo(ROLE_URL);
    assertThat(contributionRole.getDoc().get(PROPERTY_LABEL).asText()).isEqualTo("Author");
    assertThat(contributionRole.getDoc().get(PROPERTY_ID).asText()).isEqualTo(ROLE);
    assertThat(contributionRole.getOutgoingEdges().isEmpty()).isTrue();
  }

  private void validateSampleContributionAgent(ResourceEdge contributionAgentEdge, Resource contribution) {
    assertThat(contributionAgentEdge.getId()).isNotNull();
    assertThat(contributionAgentEdge.getSource()).isEqualTo(contribution);
    assertThat(contributionAgentEdge.getPredicate().getLabel()).isEqualTo(AGENT_PRED);
    var contributionAgent = contributionAgentEdge.getTarget();
    assertThat(contributionAgent.getLabel()).isEqualTo(PERSON_URL);
    assertThat(contributionAgent.getType().getSimpleLabel()).isEqualTo(PERSON);
    assertThat(contributionAgent.getResourceHash()).isNotNull();
    assertThat(contributionAgent.getDoc().size()).isEqualTo(1);
    assertThat(contributionAgent.getDoc().get(SAME_AS_PRED).get(0).get(PROPERTY_LABEL).asText())
      .isEqualTo("Test and Evaluation Year-2000 Team (U.S.)");
    assertThat(contributionAgent.getDoc().get(SAME_AS_PRED).get(0).get(PROPERTY_URI).asText())
      .isEqualTo("http://id.loc.gov/authorities/names/no98072015");
    assertThat(contributionAgent.getOutgoingEdges().isEmpty()).isTrue();
  }

  private void validateSamplePublication(ResourceEdge publicationEdge, Resource instance) {
    assertThat(publicationEdge.getId()).isNotNull();
    assertThat(publicationEdge.getSource()).isEqualTo(instance);
    assertThat(publicationEdge.getPredicate().getLabel()).isEqualTo(PROVISION_ACTIVITY_PRED);
    var publication = publicationEdge.getTarget();
    assertThat(publication.getLabel()).isEqualTo(PUBLICATION_URL);
    assertThat(publication.getType().getSimpleLabel()).isEqualTo(PUBLICATION);
    assertThat(publication.getResourceHash()).isNotNull();
    assertThat(publication.getDoc().size()).isEqualTo(4);
    assertThat(publication.getDoc().get(SIMPLE_DATE_PRED).size()).isEqualTo(1);
    assertThat(publication.getDoc().get(SIMPLE_DATE_PRED).get(0).asText()).isEqualTo("1921");
    assertThat(publication.getDoc().get(SIMPLE_AGENT_PRED).size()).isEqualTo(1);
    assertThat(publication.getDoc().get(SIMPLE_AGENT_PRED).get(0).asText()).isEqualTo("Charles Scribner's Sons");
    assertThat(publication.getDoc().get(SIMPLE_PLACE_PRED).size()).isEqualTo(1);
    assertThat(publication.getDoc().get(SIMPLE_PLACE_PRED).get(0).asText()).isEqualTo("New York");
    assertThat(publication.getDoc().get(DATE_URL).size()).isEqualTo(1);
    assertThat(publication.getDoc().get(DATE_URL).get(0).asText()).isEqualTo("1921");
    assertThat(publication.getOutgoingEdges().size()).isEqualTo(1);
    validateSamplePublicationPlace(publication.getOutgoingEdges().iterator().next(), publication);
  }

  private void validateSamplePublicationPlace(ResourceEdge publicationPlaceEdge, Resource publication) {
    assertThat(publicationPlaceEdge.getSource()).isEqualTo(publication);
    assertThat(publicationPlaceEdge.getId()).isNotNull();
    assertThat(publicationPlaceEdge.getSource()).isEqualTo(publication);
    assertThat(publicationPlaceEdge.getPredicate().getLabel()).isEqualTo(PLACE_PRED);
    var publicationPlace = publicationPlaceEdge.getTarget();
    assertThat(publicationPlace.getLabel()).isEqualTo("New York (State)");
    assertThat(publicationPlace.getType().getSimpleLabel()).isEqualTo(PLACE_COMPONENTS);
    assertThat(publicationPlace.getResourceHash()).isNotNull();
    assertThat(publicationPlace.getDoc().size()).isEqualTo(3);
    assertThat(publicationPlace.getDoc().get(PROPERTY_URI).asText()).isEqualTo(PLACE_URL);
    assertThat(publicationPlace.getDoc().get(PROPERTY_LABEL).asText()).isEqualTo("New York (State)");
    assertThat(publicationPlace.getDoc().get(PROPERTY_ID).asText()).isEqualTo(PLACE);
    assertThat(publicationPlace.getOutgoingEdges().isEmpty()).isTrue();
  }

  private String toCarrierLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CARRIER_PRED), path(PROPERTY_LABEL));
  }

  private String toCarrierUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CARRIER_PRED), path(PROPERTY_URI));
  }


  private String toMediaLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(MEDIA_PRED), path(PROPERTY_LABEL));
  }

  private String toMediaUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(MEDIA_PRED), path(PROPERTY_URI));
  }

  private String toNoteId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(NOTE_PRED), path(PROPERTY_ID));
  }

  private String toNoteLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(NOTE_PRED), path(PROPERTY_LABEL));
  }

  private String toNoteUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(NOTE_PRED), path(PROPERTY_URI));
  }

  private String toIssuanceLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(ISSUANCE_PRED), path(PROPERTY_LABEL));
  }

  private String toIssuanceUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(ISSUANCE_PRED), path(PROPERTY_URI));
  }

  private String toDimensions() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(DIMENSIONS_URL));
  }

  private String toExtentLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(EXTENT_PRED),
      path(EXTENT_URL), arrayPath(LABEL_PRED));
  }

  private String toContributionRoleLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CONTRIBUTION_PRED),
      path(CONTRIBUTION_URL), arrayPath(ROLE_PRED), path(PROPERTY_LABEL));
  }

  private String toContributionRoleUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CONTRIBUTION_PRED),
      path(CONTRIBUTION_URL), arrayPath(ROLE_PRED), path(PROPERTY_URI));
  }

  private String toContributionRoleId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CONTRIBUTION_PRED),
      path(CONTRIBUTION_URL), arrayPath(ROLE_PRED), path(PROPERTY_ID));
  }

  private String toContributionAgentLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CONTRIBUTION_PRED), path(CONTRIBUTION_URL),
      arrayPath(AGENT_PRED), path(PERSON_URL), arrayPath(SAME_AS_PRED), path(PROPERTY_LABEL));
  }

  private String toContributionAgentUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CONTRIBUTION_PRED), path(CONTRIBUTION_URL),
      arrayPath(AGENT_PRED), path(PERSON_URL), arrayPath(SAME_AS_PRED), path(PROPERTY_URI));
  }

  private String toPublicationDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(PUBLICATION_URL), arrayPath(DATE_PRED));
  }

  private String toPublicationPlaceUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(PUBLICATION_URL), arrayPath(PLACE_PRED), path(PROPERTY_URI));
  }

  private String toPublicationPlaceLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(PUBLICATION_URL), arrayPath(PLACE_PRED), path(PROPERTY_LABEL));
  }

  private String toPublicationPlaceId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(PUBLICATION_URL), arrayPath(PLACE_PRED), path(PROPERTY_ID));
  }

  private String toPublicationSimplePlace() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(PUBLICATION_URL), arrayPath(SIMPLE_PLACE_PRED));
  }

  private String toPublicationSimpleAgent() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(PUBLICATION_URL), arrayPath(SIMPLE_AGENT_PRED));
  }

  private String toPublicationSimpleDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(PUBLICATION_URL), arrayPath(SIMPLE_DATE_PRED));
  }

  private String toInstanceTitle() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(TITLE_PRED), path(INSTANCE_TITLE_URL),
      arrayPath(MAIN_TITLE_PRED));
  }

  private String toParallelTitle() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(TITLE_PRED, 1), path(PARALLEL_TITLE_URL),
      arrayPath(MAIN_TITLE_PRED));
  }

  private String toVariantTitle() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(TITLE_PRED, 2), path(VARIANT_TITLE_URL),
      arrayPath(MAIN_TITLE_PRED));
  }

  private String toIdentifiedByLccn() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED), path(IDENTIFIERS_LCCN_URL),
      arrayPath(VALUE_URL));
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
