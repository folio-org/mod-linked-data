package org.folio.linked.data.model.entity;

import static jakarta.persistence.CascadeType.DETACH;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REFRESH;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.folio.ld.dictionary.api.Predicate;
import org.folio.linked.data.model.entity.pk.ResourceEdgePk;

@Data
@Entity
@NoArgsConstructor
@Accessors(chain = true)
@RequiredArgsConstructor
@Table(name = "resource_edges")
public class ResourceEdge {

  @ToString.Exclude
  @EmbeddedId
  private ResourceEdgePk id = new ResourceEdgePk();

  @NonNull
  @ToString.Exclude
  @ManyToOne(cascade = {PERSIST, MERGE, REFRESH, DETACH})
  @MapsId("sourceHash")
  @JoinColumn(name = "source_hash", nullable = false)
  private Resource source;

  @NonNull
  @ManyToOne(cascade = {PERSIST, MERGE, REFRESH, DETACH})
  @MapsId("targetHash")
  @JoinColumn(name = "target_hash", nullable = false)
  private Resource target;

  @NonNull
  @ManyToOne
  @MapsId("predicateHash")
  @JoinColumn(name = "predicate_hash", nullable = false)
  private PredicateEntity predicate;

  public ResourceEdge(@NonNull Resource source, @NonNull Resource target, @NonNull Predicate predicate) {
    this.source = source;
    this.target = target;
    this.predicate = new PredicateEntity(predicate.getHash(), predicate.getUri());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceEdge that = (ResourceEdge) o;
    return Objects.equals(source.getResourceHash(), that.source.getResourceHash())
      && Objects.equals(target.getResourceHash(), that.target.getResourceHash())
      && Objects.equals(predicate.getHash(), that.predicate.getHash());
  }

  @Override
  public int hashCode() {
    return Objects.hash(source.getResourceHash(), target.getResourceHash(), predicate.getHash());
  }
}
