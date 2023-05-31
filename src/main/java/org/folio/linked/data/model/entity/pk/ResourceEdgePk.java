package org.folio.linked.data.model.entity.pk;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ResourceEdgePk implements Serializable {

  @Column(name = "source_hash")
  private Long sourceHash;

  @Column(name = "target_hash")
  private Long targetHash;

  @Column(name = "predicate_hash")
  private Long predicateHash;

}
