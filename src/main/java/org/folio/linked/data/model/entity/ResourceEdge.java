package org.folio.linked.data.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.folio.linked.data.model.entity.pk.ResourceEdgePk;

@Entity
@Table(name = "resource_edges")
@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class ResourceEdge {

  @EmbeddedId
  private ResourceEdgePk id = new ResourceEdgePk();

  @NonNull
  @ManyToOne(cascade = CascadeType.ALL)
  @MapsId("sourceHash")
  @JoinColumn(name = "source_hash", nullable = false)
  private Resource source;

  @NonNull
  @ManyToOne(cascade = CascadeType.ALL)
  @MapsId("targetHash")
  @JoinColumn(name = "target_hash", nullable = false)
  private Resource target;

  @NonNull
  @ManyToOne
  @MapsId("predicateHash")
  @JoinColumn(name = "predicate_hash", nullable = false)
  private Predicate predicate;

}
