package org.folio.linked.data.e2e;

import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE})
class DatabaseFolioIT extends DatabaseIT {
}
