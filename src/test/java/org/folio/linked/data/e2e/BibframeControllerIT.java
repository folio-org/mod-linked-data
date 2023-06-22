package org.folio.linked.data.e2e;

import static java.util.Comparator.comparing;
import static org.folio.linked.data.TestUtil.defaultHeaders;
import static org.folio.linked.data.TestUtil.randomLong;
import static org.folio.linked.data.TestUtil.randomResource;
import static org.folio.linked.data.model.ErrorCode.NOT_FOUND_ERROR;
import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.CARRIER_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_URL;
import static org.folio.linked.data.util.BibframeConstants.DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.DIMENSIONS_PRED;
import static org.folio.linked.data.util.BibframeConstants.EXTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.EXTENT_URL;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_URL;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ITEM_URL;
import static org.folio.linked.data.util.BibframeConstants.LABEL_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_ID;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_LABEL;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_URI;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION_URL;
import static org.folio.linked.data.util.BibframeConstants.ROLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.WORK_URL;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.common.collect.Lists;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.util.MonographTestService;
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
  private Environment env;


  @AfterEach
  public void clean() {
    resourceRepo.deleteAll();
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
      .andExpect(jsonPath("$." + pathToMainTitle(), equalTo("Laramie holds the range")))
      .andExpect(jsonPath("$." + pathToSimpleDate(), equalTo("1921")))
      .andExpect(jsonPath("$." + pathToSimpleAgent(), equalTo("Charles Scribner's Sons")))
      .andExpect(jsonPath("$." + pathToSimplePlace(), equalTo("New York")))
      .andExpect(jsonPath("$." + pathToPlaceUri(), equalTo("http://id.loc.gov/ontologies/bibframe/Place")))
      .andExpect(jsonPath("$." + pathToPlaceId(), equalTo("lc:RT:bf2:Place")))
      .andExpect(jsonPath("$." + pathToPlaceLabel(), equalTo("New York (State)")))
      .andExpect(jsonPath("$." + pathToDate(), equalTo("1921")))
      .andExpect(jsonPath("$." + pathToAgentId(), equalTo("lc:RT:bf2:Agent:bfPerson")))
      .andExpect(jsonPath("$." + pathToAgentUri(), equalTo("http://id.loc.gov/ontologies/bibframe/Person")))
      .andExpect(jsonPath("$." + pathToAgentLabel(), equalTo("Spearman, Frank H. (Frank Hamilton), 1859-1937")))
      .andExpect(jsonPath("$." + pathToRoleId(), equalTo("lc:RT:bf2:Agent:bfRole")))
      .andExpect(jsonPath("$." + pathToRoleUri(), equalTo("http://id.loc.gov/ontologies/bibframe/Role")))
      .andExpect(jsonPath("$." + pathToRoleLabel(), equalTo("Author")))
      .andExpect(jsonPath("$." + pathToExtentLabel(), equalTo("vi, 374 pages, 4 unnumbered leaves of plates")))
      .andExpect(jsonPath("$." + pathToDimensions(), equalTo("20 cm")))
      .andExpect(jsonPath("$." + pathToIssuanceUri(), equalTo("http://id.loc.gov/ontologies/bibframe/Issuance")))
      .andExpect(jsonPath("$." + pathToIssuanceLabel(), equalTo("single unit")))
      .andExpect(jsonPath("$." + pathToMediaUri(), equalTo("http://id.loc.gov/ontologies/bibframe/Media")))
      .andExpect(jsonPath("$." + pathToMediaLabel(), equalTo("unmediated")))
      .andExpect(jsonPath("$." + pathToCarrierUri(), equalTo("http://id.loc.gov/ontologies/bibframe/Carrier")))
      .andExpect(jsonPath("$." + pathToCarrierLabel(), equalTo("volume")));
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

  private String pathToCarrierLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CARRIER_PRED), path(PROPERTY_LABEL));
  }

  private String pathToCarrierUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CARRIER_PRED), path(PROPERTY_URI));
  }


  private String pathToMediaLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(MEDIA_PRED), path(PROPERTY_LABEL));
  }

  private String pathToMediaUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(MEDIA_PRED), path(PROPERTY_URI));
  }


  private String pathToIssuanceLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(ISSUANCE_PRED), path(PROPERTY_LABEL));
  }

  private String pathToIssuanceUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(ISSUANCE_PRED), path(PROPERTY_URI));
  }

  private String pathToDimensions() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(DIMENSIONS_PRED));
  }

  private String pathToExtentLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(EXTENT_PRED),
      path(EXTENT_URL), arrayPath(LABEL_PRED));
  }

  private String pathToRoleLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CONTRIBUTION_PRED),
      path(CONTRIBUTION_URL), arrayPath(ROLE_PRED), path(PROPERTY_LABEL));
  }

  private String pathToRoleUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CONTRIBUTION_PRED),
      path(CONTRIBUTION_URL), arrayPath(ROLE_PRED), path(PROPERTY_URI));
  }

  private String pathToRoleId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CONTRIBUTION_PRED),
      path(CONTRIBUTION_URL), arrayPath(ROLE_PRED), path(PROPERTY_ID));
  }

  private String pathToAgentLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CONTRIBUTION_PRED),
      path(CONTRIBUTION_URL), arrayPath(AGENT_PRED), path(PROPERTY_LABEL));
  }

  private String pathToAgentUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CONTRIBUTION_PRED),
      path(CONTRIBUTION_URL), arrayPath(AGENT_PRED), path(PROPERTY_URI));
  }

  private String pathToAgentId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CONTRIBUTION_PRED),
      path(CONTRIBUTION_URL), arrayPath(AGENT_PRED), path(PROPERTY_ID));
  }

  private String pathToDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(PUBLICATION_URL), arrayPath(DATE_PRED));
  }

  private String pathToPlaceUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(PUBLICATION_URL), arrayPath(PLACE_PRED), path(PROPERTY_URI));
  }

  private String pathToPlaceLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(PUBLICATION_URL), arrayPath(PLACE_PRED), path(PROPERTY_LABEL));
  }

  private String pathToPlaceId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(PUBLICATION_URL), arrayPath(PLACE_PRED), path(PROPERTY_ID));
  }

  private String pathToSimplePlace() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(PUBLICATION_URL), arrayPath(SIMPLE_PLACE_PRED));
  }

  private String pathToSimpleAgent() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(PUBLICATION_URL), arrayPath(SIMPLE_AGENT_PRED));
  }

  private String pathToSimpleDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(PUBLICATION_URL), arrayPath(SIMPLE_DATE_PRED));
  }

  private String pathToMainTitle() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(TITLE_PRED), path(TITLE_URL),
      arrayPath(MAIN_TITLE_PRED));
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
