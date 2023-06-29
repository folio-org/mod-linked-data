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
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_URL;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ITEM_URL;
import static org.folio.linked.data.util.BibframeConstants.LABEL_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_URL;
import static org.folio.linked.data.util.BibframeConstants.MONOGRAPH;
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
import static org.folio.linked.data.util.BibframeConstants.WORK_URL;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.MonographTestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
class BibframeControllerIT {

  public static final String BIBFRAMES_URL = "/bibframes";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ResourceRepository resourceRepo;
  @Autowired
  private MonographTestService monographTestService;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private Environment env;


  @AfterEach
  public void clean() {
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
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id", notNullValue()))
      .andExpect(jsonPath("$." + path(WORK_URL)).doesNotExist())
      .andExpect(jsonPath("$." + path(ITEM_URL)).doesNotExist())
      .andExpect(jsonPath("$." + path(INSTANCE_URL), notNullValue()))
      .andExpect(jsonPath("$." + toCarrierUri(), equalTo("http://id.loc.gov/ontologies/bibframe/Carrier")))
      .andExpect(jsonPath("$." + toCarrierLabel(), equalTo("volume")))
      .andExpect(jsonPath("$." + toContributionAgentUri(), equalTo("http://id.loc.gov/authorities/names/no98072015")))
      .andExpect(jsonPath("$." + toContributionAgentLabel(), equalTo("Test and Evaluation Year-2000 Team (U.S.)")))
      .andExpect(jsonPath("$." + toContributionRoleId(), equalTo("lc:RT:bf2:Agent:bfRole")))
      .andExpect(jsonPath("$." + toContributionRoleUri(), equalTo("http://id.loc.gov/ontologies/bibframe/Role")))
      .andExpect(jsonPath("$." + toContributionRoleLabel(), equalTo("Author")))
      .andExpect(jsonPath("$." + toDimensions(), equalTo("20 cm")))
      .andExpect(jsonPath("$." + toExtentLabel(), equalTo("vi, 374 pages, 4 unnumbered leaves of plates")))
      .andExpect(jsonPath("$." + toIdentifiedByLccn(), equalTo("21014542")))
      .andExpect(jsonPath("$." + toIssuanceLabel(), equalTo("single unit")))
      .andExpect(jsonPath("$." + toIssuanceUri(), equalTo("http://id.loc.gov/ontologies/bibframe/Issuance")))
      .andExpect(jsonPath("$." + toMainTitle(), equalTo("Laramie holds the range")))
      .andExpect(jsonPath("$." + toMediaLabel(), equalTo("unmediated")))
      .andExpect(jsonPath("$." + toMediaUri(), equalTo("http://id.loc.gov/ontologies/bibframe/Media")))
      .andExpect(jsonPath("$." + toPublicationSimpleAgent(), equalTo("Charles Scribner's Sons")))
      .andExpect(jsonPath("$." + toPublicationSimpleDate(), equalTo("1921")))
      .andExpect(jsonPath("$." + toPublicationSimplePlace(), equalTo("New York")))
      .andExpect(jsonPath("$." + toPublicationPlaceId(), equalTo("lc:RT:bf2:Place")))
      .andExpect(jsonPath("$." + toPublicationPlaceLabel(), equalTo("New York (State)")))
      .andExpect(jsonPath("$." + toPublicationPlaceUri(), equalTo(PLACE_URL)))
      .andExpect(jsonPath("$." + toPublicationDate(), equalTo("1921")))
      .andReturn().getResponse().getContentAsString();

    var bibframeResponse = objectMapper.readValue(response, BibframeResponse.class);
    var persistedOptional = resourceRepo.findById(bibframeResponse.getId());
    assertThat(persistedOptional.isPresent()).isTrue();
    var monograph = persistedOptional.get();
    assertThat(monograph.getType().getSimpleLabel()).isEqualTo(MONOGRAPH);
    assertThat(monograph.getLabel()).isEqualTo(MONOGRAPH);
    assertThat(monograph.getDoc()).isNull();
    assertThat(monograph.getResourceHash()).isNotNull();
    assertThat(monograph.getOutgoingEdges().size()).isEqualTo(1);
    var instanceEdge = monograph.getOutgoingEdges().iterator().next();
    assertThat(instanceEdge.getId()).isNotNull();
    assertThat(instanceEdge.getSource()).isEqualTo(monograph);
    assertThat(instanceEdge.getPredicate().getLabel()).isEqualTo(INSTANCE_PRED);
    var instance = instanceEdge.getTarget();
    assertThat(instance.getLabel()).isEqualTo(INSTANCE_URL);
    assertThat(instance.getType().getSimpleLabel()).isEqualTo(INSTANCE);
    assertThat(instance.getResourceHash()).isNotNull();
    assertThat(instance.getDoc().size()).isEqualTo(1);
    assertThat(instance.getDoc().get(DIMENSIONS_URL).size()).isEqualTo(1);
    assertThat(instance.getDoc().get(DIMENSIONS_URL).get(0).asText()).isEqualTo("20 cm");
    assertThat(instance.getOutgoingEdges().size()).isEqualTo(8);
    var instanceEdgeIterator = instance.getOutgoingEdges().iterator();
    var publicationEdge = instanceEdgeIterator.next();
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
    var publicationPlaceEdge = publication.getOutgoingEdges().iterator().next();
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
    var contributionEdge = instanceEdgeIterator.next();
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
    var contributionAgentEdge = contributionEdgeIterator.next();
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
    var contributionRoleEdge = contributionEdgeIterator.next();
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
    var extentEdge = instanceEdgeIterator.next();
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
    var mediaEdge = instanceEdgeIterator.next();
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
    var instanceTitleEdge = instanceEdgeIterator.next();
    assertThat(instanceTitleEdge.getId()).isNotNull();
    assertThat(instanceTitleEdge.getSource()).isEqualTo(instance);
    assertThat(instanceTitleEdge.getPredicate().getLabel()).isEqualTo(INSTANCE_TITLE_PRED);
    var instanceTitle = instanceTitleEdge.getTarget();
    assertThat(instanceTitle.getLabel()).isEqualTo(INSTANCE_TITLE_URL);
    assertThat(instanceTitle.getType().getSimpleLabel()).isEqualTo(INSTANCE_TITLE);
    assertThat(instanceTitle.getResourceHash()).isNotNull();
    assertThat(instanceTitle.getDoc().size()).isEqualTo(1);
    assertThat(instanceTitle.getDoc().get(MAIN_TITLE_URL).size()).isEqualTo(1);
    assertThat(instanceTitle.getDoc().get(MAIN_TITLE_URL).get(0).asText()).isEqualTo("Laramie holds the range");
    assertThat(instanceTitle.getOutgoingEdges().isEmpty()).isTrue();
    var carrierEdge = instanceEdgeIterator.next();
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
    var identifiedByLccnEdge = instanceEdgeIterator.next();
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


  @Test
  @Transactional
  void getBibframeById_shouldReturnExistedEntity() throws Exception {
    // given
    var existed = resourceRepo.save(monographTestService.createSampleMonograph());
    var requestBuilder = get(BIBFRAMES_URL + "/" + existed.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id", notNullValue()))
      .andExpect(jsonPath("$." + path(WORK_URL)).doesNotExist())
      .andExpect(jsonPath("$." + path(ITEM_URL)).doesNotExist())
      .andExpect(jsonPath("$." + path(INSTANCE_URL), notNullValue()))
      .andExpect(jsonPath("$." + toMainTitle(), equalTo("Laramie holds the range")))
      .andExpect(jsonPath("$." + toPublicationSimpleDate(), equalTo("1921")))
      .andExpect(jsonPath("$." + toPublicationSimpleAgent(), equalTo("Charles Scribner's Sons")))
      .andExpect(jsonPath("$." + toPublicationSimplePlace(), equalTo("New York")))
      .andExpect(jsonPath("$." + toPublicationPlaceUri(), equalTo(PLACE_URL)))
      .andExpect(jsonPath("$." + toPublicationPlaceId(), equalTo("lc:RT:bf2:Place")))
      .andExpect(jsonPath("$." + toPublicationPlaceLabel(), equalTo("New York (State)")))
      .andExpect(jsonPath("$." + toPublicationDate(), equalTo("1921")))
      .andExpect(jsonPath("$." + toContributionAgentUri(), equalTo("http://id.loc.gov/authorities/names/no98072015")))
      .andExpect(jsonPath("$." + toContributionAgentLabel(), equalTo("Test and Evaluation Year-2000 Team (U.S.)")))
      .andExpect(jsonPath("$." + toContributionRoleId(), equalTo("lc:RT:bf2:Agent:bfRole")))
      .andExpect(jsonPath("$." + toContributionRoleUri(), equalTo("http://id.loc.gov/ontologies/bibframe/Role")))
      .andExpect(jsonPath("$." + toContributionRoleLabel(), equalTo("Author")))
      .andExpect(jsonPath("$." + toExtentLabel(), equalTo("vi, 374 pages, 4 unnumbered leaves of plates")))
      .andExpect(jsonPath("$." + toDimensions(), equalTo("20 cm")))
      .andExpect(jsonPath("$." + toIssuanceUri(), equalTo("http://id.loc.gov/ontologies/bibframe/Issuance")))
      .andExpect(jsonPath("$." + toIssuanceLabel(), equalTo("single unit")))
      .andExpect(jsonPath("$." + toMediaUri(), equalTo("http://id.loc.gov/ontologies/bibframe/Media")))
      .andExpect(jsonPath("$." + toMediaLabel(), equalTo("unmediated")))
      .andExpect(jsonPath("$." + toCarrierUri(), equalTo("http://id.loc.gov/ontologies/bibframe/Carrier")))
      .andExpect(jsonPath("$." + toCarrierLabel(), equalTo("volume")));
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

  private String toMainTitle() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE_URL),
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
