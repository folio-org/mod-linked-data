package org.folio.linked.data.configuration.feed;

import com.rometools.rome.feed.atom.Entry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;

import java.util.List;
import java.util.Map;

// this view can be used for messages transformation
public class AtomFeedView extends AbstractAtomFeedView {

  @Override
  protected List<Entry> buildFeedEntries(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
    return List.of();
  }
}
