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
import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION_URL;
import static org.folio.linked.data.util.BibframeConstants.EXTENT;
import static org.folio.linked.data.util.BibframeConstants.EXTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.EXTENT_URL;
import static org.folio.linked.data.util.BibframeConstants.ID;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_EAN;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_EAN_URL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_ISBN;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_ISBN_URL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LCCN;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LCCN_URL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LOCAL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LOCAL_URL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_OTHER;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_OTHER_URL;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_URL;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE_URL;
import static org.folio.linked.data.util.BibframeConstants.ITEM_URL;
import static org.folio.linked.data.util.BibframeConstants.LABEL_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.MANUFACTURE;
import static org.folio.linked.data.util.BibframeConstants.MANUFACTURE_URL;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_URL;
import static org.folio.linked.data.util.BibframeConstants.MONOGRAPH;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.PERSON;
import static org.folio.linked.data.util.BibframeConstants.PERSON_URL;
import static org.folio.linked.data.util.BibframeConstants.PLACE;
import static org.folio.linked.data.util.BibframeConstants.PLACE_COMPONENTS;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PLACE_URL;
import static org.folio.linked.data.util.BibframeConstants.PRODUCTION;
import static org.folio.linked.data.util.BibframeConstants.PRODUCTION_URL;
import static org.folio.linked.data.util.BibframeConstants.PROFILE;
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
import org.apache.commons.lang3.StringUtils;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.MonographTestService;
import org.folio.linked.data.test.ResourceEdgeRepository;
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

  public static final String BIBFRAME_URL = "/bibframe";

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
    var requestBuilder = post(BIBFRAME_URL)
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
  void createTwoMonographInstancesWithSharedResources_shouldSaveBothCorrectly() throws Exception {
    // given
    var requestBuilder1 = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(getResourceSample());
    var resultActions1 = mockMvc.perform(requestBuilder1);
    var response1 = validateSampleBibframeResponse(resultActions1)
      .andReturn().getResponse().getContentAsString();
    var bibframeResponse1 = objectMapper.readValue(response1, BibframeResponse.class);
    var persistedOptional1 = resourceRepo.findById(bibframeResponse1.getId());
    assertThat(persistedOptional1.isPresent()).isTrue();
    var monograph1 = persistedOptional1.get();
    validateSampleMonographEntity(monograph1);
    var requestBuilder2 = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(getResourceSample().replace("volume", "length"));
    var expectedDifference = "length\"}]}],\"id\":3561758308,\"profile\":\"lc:profile:bf2:Monograph\"}";

    // when
    var response2 = mockMvc.perform(requestBuilder2).andReturn().getResponse().getContentAsString();

    // then
    assertThat(StringUtils.difference(response1, response2)).isEqualTo(expectedDifference);
  }

  @Test
  void getBibframeById_shouldReturnExistedEntity() throws Exception {
    // given
    var existed = resourceRepo.save(monographTestService.createSampleMonograph());
    var requestBuilder = get(BIBFRAME_URL + "/" + existed.getResourceHash())
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
    var requestBuilder = get(BIBFRAME_URL + "/" + notExistedId)
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
  void getBibframeShortInfoPage_shouldReturnPageWithExistedEntities() throws Exception {
    // given
    var existed = Lists.newArrayList(
      resourceRepo.save(randomResource(1L, monographTestService.getMonographProfile())),
      resourceRepo.save(randomResource(2L, monographTestService.getMonographProfile())),
      resourceRepo.save(randomResource(3L, monographTestService.getMonographProfile()))
    ).stream().sorted(comparing(Resource::getResourceHash)).toList();
    var requestBuilder = get(BIBFRAME_URL)
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
  void deleteBibframeById_shouldDeleteRootResourceAndRootEdge() throws Exception {
    // given
    var existed = resourceRepo.save(monographTestService.createSampleMonograph());
    assertThat(resourceRepo.findById(existed.getResourceHash())).isPresent();
    assertThat(resourceRepo.count()).isEqualTo(26);
    assertThat(resourceEdgeRepository.count()).isEqualTo(25);
    var requestBuilder = delete(BIBFRAME_URL + "/" + existed.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    mockMvc.perform(requestBuilder);

    // then
    assertThat(resourceRepo.findById(existed.getResourceHash())).isNotPresent();
    assertThat(resourceRepo.count()).isEqualTo(25);
    assertThat(resourceEdgeRepository.findById(existed.getOutgoingEdges().iterator().next().getId())).isNotPresent();
    assertThat(resourceEdgeRepository.count()).isEqualTo(24);
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
      .andExpect(jsonPath("$." + toIdentifiedByEan(), equalTo("12345670")))
      .andExpect(jsonPath("$." + toIdentifiedByIsbn(), equalTo("12345671")))
      .andExpect(jsonPath("$." + toIdentifiedByLccn(), equalTo("12345672")))
      .andExpect(jsonPath("$." + toIdentifiedByLocal(), equalTo("12345673")))
      .andExpect(jsonPath("$." + toIdentifiedByOther(), equalTo("12345674")))
      .andExpect(jsonPath("$." + toIssuanceLabel(), equalTo("single unit")))
      .andExpect(jsonPath("$." + toIssuanceUri(), equalTo(ISSUANCE_URL)))
      .andExpect(jsonPath("$." + toInstanceTitle(), equalTo("Instance: Laramie holds the range")))
      .andExpect(jsonPath("$." + toParallelTitle(), equalTo("Parallel: Laramie holds the range")))
      .andExpect(jsonPath("$." + toVariantTitle(), equalTo("Variant: Laramie holds the range")))
      .andExpect(jsonPath("$." + toMediaLabel(), equalTo("unmediated")))
      .andExpect(jsonPath("$." + toMediaUri(), equalTo(MEDIA_URL)))
      .andExpect(jsonPath("$." + toDistributionSimpleAgent(), equalTo("Distribution: Charles Scribner's Sons")))
      .andExpect(jsonPath("$." + toDistributionSimpleDate(), equalTo("Distribution: 1921")))
      .andExpect(jsonPath("$." + toDistributionSimplePlace(), equalTo("Distribution: New York")))
      .andExpect(jsonPath("$." + toDistributionPlaceId(), equalTo(PLACE)))
      .andExpect(jsonPath("$." + toDistributionPlaceLabel(), equalTo("Distribution: New York (State)")))
      .andExpect(jsonPath("$." + toDistributionPlaceUri(), equalTo(PLACE_URL)))
      .andExpect(jsonPath("$." + toDistributionDate(), equalTo("Distribution: 1921")))
      .andExpect(jsonPath("$." + toManufactureSimpleAgent(), equalTo("Manufacture: Charles Scribner's Sons")))
      .andExpect(jsonPath("$." + toManufactureSimpleDate(), equalTo("Manufacture: 1921")))
      .andExpect(jsonPath("$." + toManufactureSimplePlace(), equalTo("Manufacture: New York")))
      .andExpect(jsonPath("$." + toManufacturePlaceId(), equalTo(PLACE)))
      .andExpect(jsonPath("$." + toManufacturePlaceLabel(), equalTo("Manufacture: New York (State)")))
      .andExpect(jsonPath("$." + toManufacturePlaceUri(), equalTo(PLACE_URL)))
      .andExpect(jsonPath("$." + toManufactureDate(), equalTo("Manufacture: 1921")))
      .andExpect(jsonPath("$." + toProductionSimpleAgent(), equalTo("Production: Charles Scribner's Sons")))
      .andExpect(jsonPath("$." + toProductionSimpleDate(), equalTo("Production: 1921")))
      .andExpect(jsonPath("$." + toProductionSimplePlace(), equalTo("Production: New York")))
      .andExpect(jsonPath("$." + toProductionPlaceId(), equalTo(PLACE)))
      .andExpect(jsonPath("$." + toProductionPlaceLabel(), equalTo("Production: New York (State)")))
      .andExpect(jsonPath("$." + toProductionPlaceUri(), equalTo(PLACE_URL)))
      .andExpect(jsonPath("$." + toProductionDate(), equalTo("Production: 1921")))
      .andExpect(jsonPath("$." + toPublicationSimpleAgent(), equalTo("Publication: Charles Scribner's Sons")))
      .andExpect(jsonPath("$." + toPublicationSimpleDate(), equalTo("Publication: 1921")))
      .andExpect(jsonPath("$." + toPublicationSimplePlace(), equalTo("Publication: New York")))
      .andExpect(jsonPath("$." + toPublicationPlaceId(), equalTo(PLACE)))
      .andExpect(jsonPath("$." + toPublicationPlaceLabel(), equalTo("Publication: New York (State)")))
      .andExpect(jsonPath("$." + toPublicationPlaceUri(), equalTo(PLACE_URL)))
      .andExpect(jsonPath("$." + toPublicationDate(), equalTo("Publication: 1921")))
      .andExpect(jsonPath("$." + toProfile(), equalTo(MONOGRAPH)))
      .andExpect(jsonPath("$." + toId(), notNullValue()));
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
    assertThat(instance.getOutgoingEdges().size()).isEqualTo(17);

    var instanceEdgeIterator = instance.getOutgoingEdges().iterator();
    validateSampleTitle(instanceEdgeIterator.next(), instance, INSTANCE_TITLE_URL, INSTANCE_TITLE, "Instance: ");
    validateSampleTitle(instanceEdgeIterator.next(), instance, PARALLEL_TITLE_URL, PARALLEL_TITLE, "Parallel: ");
    validateSampleTitle(instanceEdgeIterator.next(), instance, VARIANT_TITLE_URL, VARIANT_TITLE, "Variant: ");
    validateSampleProvision(instanceEdgeIterator.next(), instance, DISTRIBUTION_URL, DISTRIBUTION, "Distribution: ");
    validateSampleProvision(instanceEdgeIterator.next(), instance, MANUFACTURE_URL, MANUFACTURE, "Manufacture: ");
    validateSampleProvision(instanceEdgeIterator.next(), instance, PRODUCTION_URL, PRODUCTION, "Production: ");
    validateSampleProvision(instanceEdgeIterator.next(), instance, PUBLICATION_URL, PUBLICATION, "Publication: ");
    validateSampleContribution(instanceEdgeIterator.next(), instance);
    validateSampleIdentified(instanceEdgeIterator.next(), instance, IDENTIFIERS_EAN_URL, IDENTIFIERS_EAN, "12345670");
    validateSampleIdentified(instanceEdgeIterator.next(), instance, IDENTIFIERS_ISBN_URL, IDENTIFIERS_ISBN, "12345671");
    validateSampleIdentified(instanceEdgeIterator.next(), instance, IDENTIFIERS_LCCN_URL, IDENTIFIERS_LCCN, "12345672");
    validateSampleIdentified(instanceEdgeIterator.next(), instance, IDENTIFIERS_LOCAL_URL, IDENTIFIERS_LOCAL,
      "12345673");
    validateSampleIdentified(instanceEdgeIterator.next(), instance, IDENTIFIERS_OTHER_URL, IDENTIFIERS_OTHER,
      "12345674");
    validateSampleExtent(instanceEdgeIterator.next(), instance);
    validateSampleIssuance(instanceEdgeIterator.next(), instance);
    validateSampleMedia(instanceEdgeIterator.next(), instance);
    validateSampleCarrier(instanceEdgeIterator.next(), instance);
  }

  private void validateSampleIdentified(ResourceEdge identifiedByLccnEdge, Resource instance, String label,
                                        String type, String value) {
    assertThat(identifiedByLccnEdge.getId()).isNotNull();
    assertThat(identifiedByLccnEdge.getSource()).isEqualTo(instance);
    assertThat(identifiedByLccnEdge.getPredicate().getLabel()).isEqualTo(IDENTIFIED_BY_PRED);
    var identifiedByLccn = identifiedByLccnEdge.getTarget();
    assertThat(identifiedByLccn.getLabel()).isEqualTo(label);
    assertThat(identifiedByLccn.getType().getSimpleLabel()).isEqualTo(type);
    assertThat(identifiedByLccn.getResourceHash()).isNotNull();
    assertThat(identifiedByLccn.getDoc().size()).isEqualTo(1);
    assertThat(identifiedByLccn.getDoc().get(VALUE_URL).size()).isEqualTo(1);
    assertThat(identifiedByLccn.getDoc().get(VALUE_URL).get(0).asText()).isEqualTo(value);
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

  private void validateSampleTitle(ResourceEdge titleEdge, Resource instance, String label, String type,
                                   String prefix) {
    assertThat(titleEdge.getId()).isNotNull();
    assertThat(titleEdge.getSource()).isEqualTo(instance);
    assertThat(titleEdge.getPredicate().getLabel()).isEqualTo(INSTANCE_TITLE_PRED);
    var title = titleEdge.getTarget();
    assertThat(title.getLabel()).isEqualTo(label);
    assertThat(title.getType().getSimpleLabel()).isEqualTo(type);
    assertThat(title.getResourceHash()).isNotNull();
    assertThat(title.getDoc().size()).isEqualTo(1);
    assertThat(title.getDoc().get(MAIN_TITLE_URL).size()).isEqualTo(1);
    assertThat(title.getDoc().get(MAIN_TITLE_URL).get(0).asText()).isEqualTo(prefix + "Laramie holds the range");
    assertThat(title.getOutgoingEdges().isEmpty()).isTrue();
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

  private void validateSampleProvision(ResourceEdge provisionEdge, Resource instance, String label, String type,
                                       String prefix) {
    assertThat(provisionEdge.getId()).isNotNull();
    assertThat(provisionEdge.getSource()).isEqualTo(instance);
    assertThat(provisionEdge.getPredicate().getLabel()).isEqualTo(PROVISION_ACTIVITY_PRED);
    var provision = provisionEdge.getTarget();
    assertThat(provision.getLabel()).isEqualTo(label);
    assertThat(provision.getType().getSimpleLabel()).isEqualTo(type);
    assertThat(provision.getResourceHash()).isNotNull();
    assertThat(provision.getDoc().size()).isEqualTo(4);
    assertThat(provision.getDoc().get(SIMPLE_DATE_PRED).size()).isEqualTo(1);
    assertThat(provision.getDoc().get(SIMPLE_DATE_PRED).get(0).asText()).isEqualTo(prefix + "1921");
    assertThat(provision.getDoc().get(SIMPLE_AGENT_PRED).size()).isEqualTo(1);
    assertThat(provision.getDoc().get(SIMPLE_AGENT_PRED).get(0).asText()).isEqualTo(prefix + "Charles Scribner's Sons");
    assertThat(provision.getDoc().get(SIMPLE_PLACE_PRED).size()).isEqualTo(1);
    assertThat(provision.getDoc().get(SIMPLE_PLACE_PRED).get(0).asText()).isEqualTo(prefix + "New York");
    assertThat(provision.getDoc().get(DATE_URL).size()).isEqualTo(1);
    assertThat(provision.getDoc().get(DATE_URL).get(0).asText()).isEqualTo(prefix + "1921");
    assertThat(provision.getOutgoingEdges().size()).isEqualTo(1);
    validateSamplePublicationPlace(provision.getOutgoingEdges().iterator().next(), provision, prefix);
  }

  private void validateSamplePublicationPlace(ResourceEdge publicationPlaceEdge, Resource publication, String prefix) {
    assertThat(publicationPlaceEdge.getSource()).isEqualTo(publication);
    assertThat(publicationPlaceEdge.getId()).isNotNull();
    assertThat(publicationPlaceEdge.getSource()).isEqualTo(publication);
    assertThat(publicationPlaceEdge.getPredicate().getLabel()).isEqualTo(PLACE_PRED);
    var publicationPlace = publicationPlaceEdge.getTarget();
    assertThat(publicationPlace.getLabel()).isEqualTo(prefix + "New York (State)");
    assertThat(publicationPlace.getType().getSimpleLabel()).isEqualTo(PLACE_COMPONENTS);
    assertThat(publicationPlace.getResourceHash()).isNotNull();
    assertThat(publicationPlace.getDoc().size()).isEqualTo(3);
    assertThat(publicationPlace.getDoc().get(PROPERTY_URI).asText()).isEqualTo(PLACE_URL);
    assertThat(publicationPlace.getDoc().get(PROPERTY_LABEL).asText()).isEqualTo(prefix + "New York (State)");
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

  private String toDistributionDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(DISTRIBUTION_URL), arrayPath(DATE_PRED));
  }

  private String toDistributionPlaceUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(DISTRIBUTION_URL), arrayPath(PLACE_PRED), path(PROPERTY_URI));
  }

  private String toDistributionPlaceLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(DISTRIBUTION_URL), arrayPath(PLACE_PRED), path(PROPERTY_LABEL));
  }

  private String toDistributionPlaceId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(DISTRIBUTION_URL), arrayPath(PLACE_PRED), path(PROPERTY_ID));
  }

  private String toDistributionSimplePlace() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(DISTRIBUTION_URL), arrayPath(SIMPLE_PLACE_PRED));
  }

  private String toDistributionSimpleAgent() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(DISTRIBUTION_URL), arrayPath(SIMPLE_AGENT_PRED));
  }

  private String toDistributionSimpleDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(DISTRIBUTION_URL), arrayPath(SIMPLE_DATE_PRED));
  }

  private String toManufactureDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 1),
      path(MANUFACTURE_URL), arrayPath(DATE_PRED));
  }

  private String toManufacturePlaceUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 1),
      path(MANUFACTURE_URL), arrayPath(PLACE_PRED), path(PROPERTY_URI));
  }

  private String toManufacturePlaceLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 1),
      path(MANUFACTURE_URL), arrayPath(PLACE_PRED), path(PROPERTY_LABEL));
  }

  private String toManufacturePlaceId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 1),
      path(MANUFACTURE_URL), arrayPath(PLACE_PRED), path(PROPERTY_ID));
  }

  private String toManufactureSimplePlace() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 1),
      path(MANUFACTURE_URL), arrayPath(SIMPLE_PLACE_PRED));
  }

  private String toManufactureSimpleAgent() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 1),
      path(MANUFACTURE_URL), arrayPath(SIMPLE_AGENT_PRED));
  }

  private String toManufactureSimpleDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 1),
      path(MANUFACTURE_URL), arrayPath(SIMPLE_DATE_PRED));
  }

  private String toProductionDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 2),
      path(PRODUCTION_URL), arrayPath(DATE_PRED));
  }

  private String toProductionPlaceUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 2),
      path(PRODUCTION_URL), arrayPath(PLACE_PRED), path(PROPERTY_URI));
  }

  private String toProductionPlaceLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 2),
      path(PRODUCTION_URL), arrayPath(PLACE_PRED), path(PROPERTY_LABEL));
  }

  private String toProductionPlaceId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 2),
      path(PRODUCTION_URL), arrayPath(PLACE_PRED), path(PROPERTY_ID));
  }

  private String toProductionSimplePlace() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 2),
      path(PRODUCTION_URL), arrayPath(SIMPLE_PLACE_PRED));
  }

  private String toProductionSimpleAgent() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 2),
      path(PRODUCTION_URL), arrayPath(SIMPLE_AGENT_PRED));
  }

  private String toProductionSimpleDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 2),
      path(PRODUCTION_URL), arrayPath(SIMPLE_DATE_PRED));
  }

  private String toPublicationDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 3),
      path(PUBLICATION_URL), arrayPath(DATE_PRED));
  }

  private String toPublicationPlaceUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 3),
      path(PUBLICATION_URL), arrayPath(PLACE_PRED), path(PROPERTY_URI));
  }

  private String toPublicationPlaceLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 3),
      path(PUBLICATION_URL), arrayPath(PLACE_PRED), path(PROPERTY_LABEL));
  }

  private String toPublicationPlaceId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 3),
      path(PUBLICATION_URL), arrayPath(PLACE_PRED), path(PROPERTY_ID));
  }

  private String toPublicationSimplePlace() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 3),
      path(PUBLICATION_URL), arrayPath(SIMPLE_PLACE_PRED));
  }

  private String toPublicationSimpleAgent() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 3),
      path(PUBLICATION_URL), arrayPath(SIMPLE_AGENT_PRED));
  }

  private String toPublicationSimpleDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 3),
      path(PUBLICATION_URL), arrayPath(SIMPLE_DATE_PRED));
  }

  private String toInstanceTitle() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE_URL),
      arrayPath(MAIN_TITLE_PRED));
  }

  private String toParallelTitle() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE_URL),
      arrayPath(MAIN_TITLE_PRED));
  }

  private String toVariantTitle() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE_URL),
      arrayPath(MAIN_TITLE_PRED));
  }

  private String toIdentifiedByEan() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED), path(IDENTIFIERS_EAN_URL),
      arrayPath(VALUE_URL));
  }

  private String toIdentifiedByIsbn() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 1), path(IDENTIFIERS_ISBN_URL),
      arrayPath(VALUE_URL));
  }

  private String toIdentifiedByLccn() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 2), path(IDENTIFIERS_LCCN_URL),
      arrayPath(VALUE_URL));
  }

  private String toIdentifiedByLocal() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 3), path(IDENTIFIERS_LOCAL_URL),
      arrayPath(VALUE_URL));
  }

  private String toIdentifiedByOther() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 4), path(IDENTIFIERS_OTHER_URL),
      arrayPath(VALUE_URL));
  }

  private String toId() {
    return path(ID);
  }

  private String toProfile() {
    return path(PROFILE);
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
