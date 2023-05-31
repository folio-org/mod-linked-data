package org.folio.linked.data.model.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.folio.linked.data.model.entity.pk.ResourceEdgePk;

@Entity
@Table(name = "resource_edges")
@Getter
@Setter
public class ResourceEdge {

  @EmbeddedId
  private ResourceEdgePk id;

  @ManyToOne
  @MapsId("sourceHash")
  @JoinColumn(name = "source_hash")
  private Resource source;

  @ManyToOne
  @MapsId("targetHash")
  @JoinColumn(name = "target_hash")
  private Resource target;

  @ManyToOne
  @MapsId("predicateHash")
  @JoinColumn(name = "predicate_hash")
  private Predicate predicate;

}
