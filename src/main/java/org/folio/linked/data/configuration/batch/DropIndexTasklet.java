package org.folio.linked.data.configuration.batch;

import static java.lang.Boolean.parseBoolean;
import static java.util.Objects.isNull;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.configuration.batch.BatchConfig.JOB_PARAM_IS_FULL_REINDEX;
import static org.folio.linked.data.configuration.batch.BatchConfig.JOB_PARAM_RESOURCE_TYPE;
import static org.folio.linked.data.configuration.batch.BatchConfig.SUPPORTED_TYPES;
import static org.folio.linked.data.domain.dto.ReindexRequest.ResourceNameEnum.LINKED_DATA_HUB;
import static org.folio.linked.data.domain.dto.ReindexRequest.ResourceNameEnum.LINKED_DATA_WORK;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ReindexRequest;
import org.folio.linked.data.integration.rest.search.SearchClient;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class DropIndexTasklet implements Tasklet {

  private final SearchClient searchClient;

  @Override
  public RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) {
    var isFullReindex = getParameter(chunkContext, JOB_PARAM_IS_FULL_REINDEX);
    if (parseBoolean(isFullReindex)) {
      var searchResourceType = getSearchResourceType(getParameter(chunkContext, JOB_PARAM_RESOURCE_TYPE));
      if (isNull(searchResourceType)) {
        log.info("Full reindex has been launched for all resource types. Dropping existed indexes...");
        SUPPORTED_TYPES.forEach(st -> sendSearchMessage(getSearchResourceType(st.name())));
      } else {
        log.info("Full reindex has been launched for [{}]. Dropping existed index...", searchResourceType);
        sendSearchMessage(searchResourceType);
      }
    } else {
      log.info("Incremental reindex has been launched, no index to be dropped.");
    }
    return RepeatStatus.FINISHED;
  }

  private String getParameter(@NonNull ChunkContext chunkContext, String paramName) {
    return chunkContext.getStepContext()
      .getStepExecution()
      .getJobParameters()
      .getString(paramName);
  }

  private ReindexRequest.ResourceNameEnum getSearchResourceType(String resourceType) {
    if (HUB.name().equals(resourceType)) {
      return LINKED_DATA_HUB;
    }
    if (WORK.name().equals(resourceType)) {
      return LINKED_DATA_WORK;
    }
    return null;
  }

  private void sendSearchMessage(ReindexRequest.ResourceNameEnum searchResourceType) {
    searchClient.reindex(
      new ReindexRequest()
        .recreateIndex(true)
        .resourceName(searchResourceType)
    );
  }

}
