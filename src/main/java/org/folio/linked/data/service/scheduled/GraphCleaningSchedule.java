package org.folio.linked.data.service.scheduled;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.service.batch.BatchJobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GraphCleaningSchedule {

  private final BatchJobService batchJobService;

  @Scheduled(cron = "${mod-linked-data.graph-cleaning.schedule}")
  public void startGraphCleaningJob() {
    batchJobService.startGraphCleaning(1);
  }

}
