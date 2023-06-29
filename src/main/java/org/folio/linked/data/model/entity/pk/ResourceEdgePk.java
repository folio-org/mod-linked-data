package org.folio.linked.data.model.entity.pk;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class ResourceEdgePk implements Serializable {

  @Column(name = "source_hash")
  private Long sourceHash;

  @Column(name = "target_hash")
  private Long targetHash;

  @Column(name = "predicate_hash")
  private Long predicateHash;

}
