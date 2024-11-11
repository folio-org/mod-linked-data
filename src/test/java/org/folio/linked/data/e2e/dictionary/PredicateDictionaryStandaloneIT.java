package org.folio.linked.data.e2e.dictionary;

import static org.folio.linked.data.test.TestUtil.STANDALONE_TEST_PROFILE;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.springframework.test.context.ActiveProfiles;

@IntegrationTest
@ActiveProfiles({STANDALONE_PROFILE, STANDALONE_TEST_PROFILE})
public class PredicateDictionaryStandaloneIT extends PredicateDictionaryITBase {
}
