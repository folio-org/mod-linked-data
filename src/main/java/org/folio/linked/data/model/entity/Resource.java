package org.folio.linked.data.model.entity;

import static jakarta.persistence.CascadeType.ALL;
import static java.util.Objects.isNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Entity
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "resources")
@EqualsAndHashCode(of = "resourceHash")
public class Resource {

  @Id
  private Long resourceHash;

  @NonNull
  @Column(nullable = false)
  private String label;

  @Column(columnDefinition = "json")
  @Type(JsonBinaryType.class)
  private JsonNode doc;

  @OrderBy
  @ToString.Exclude
  @OneToMany(mappedBy = "source", cascade = ALL, fetch = FetchType.EAGER)
  private Set<ResourceEdge> outgoingEdges = new LinkedHashSet<>();

  @OrderBy
  @ManyToMany
  @JoinTable(
    name = "resource_type_map",
    joinColumns = @JoinColumn(name = "resource_hash"),
    inverseJoinColumns = @JoinColumn(name = "type_hash")
  )
  private Set<ResourceType> types;

  public Resource addType(ResourceType type) {
    if (isNull(types)) {
      types = new LinkedHashSet<>();
    }
    types.add(type);
    return this;
  }

  public ResourceType getFirstType() {
    return (isNull(types) || types.isEmpty()) ? null : types.iterator().next();
  }
}
