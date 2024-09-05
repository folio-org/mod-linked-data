package org.folio.linked.data.configuration.feed;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.feed.dsl.Feed;
import org.springframework.integration.jdbc.metadata.JdbcMetadataStore;
import org.springframework.integration.metadata.MetadataStore;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URL;

import static org.folio.linked.data.util.Constants.FEED_PROFILE;

@Configuration
@Profile(FEED_PROFILE)
public class AtomFeedConfiguration {

  private static final long POLL_PERIOD_DAILY = 24 * 60 * 60 * 1000;
  private static final String METADATA_KEY = "atom-feed";  // metadata key
  private static final String FEED_URL = "https://id.loc.gov/resources/hubs/feed";  // can be extracted to application.yml

  // this bean will be using to store metadata of last read from feed to avoid entities duplication
  // it requires following table to be created:
  // https://docs.spring.io/spring-integration/docs/5.0.5.RELEASE/reference/html/jdbc.html#jdbc-metadata-store
  @Bean
  public MetadataStore metadataStore(DataSource dataSource) {
    return new JdbcMetadataStore(dataSource);
  }

  @Bean
  public IntegrationFlow feedFlow(MetadataStore metadataStore, AtomFeedHandler atomFeedHandler) throws MalformedURLException {
    return IntegrationFlow
      .from(
        Feed.inboundAdapter(new URL(FEED_URL), METADATA_KEY).metadataStore(metadataStore),
        c -> c.poller(Pollers.fixedDelay(POLL_PERIOD_DAILY))
      )
      .channel(c -> c.queue("entries"))
      .handle(atomFeedHandler::handleMessage)
      .get();
  }

  @Component
  public class AtomFeedHandler {
    public void handleMessage(Message<?> msg) {
      // handle message from the queue
      System.out.println();
    }
  }
}
