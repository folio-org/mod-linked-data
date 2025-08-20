package org.folio.linked.data.model.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Data
@Entity
@Table(name = "profiles")
@Accessors(chain = true)
public class Profile {
  @Id
  private Integer id;

  @Column(nullable = false)
  private String name;

  @ManyToOne
  @JoinColumn(name = "resource_type", nullable = false)
  private ResourceTypeEntity resourceType;

  @ManyToMany
  @JoinTable(
    name = "profile_additional_resource_types",
    joinColumns = @JoinColumn(name = "profile_id"),
    inverseJoinColumns = @JoinColumn(name = "type_hash")
  )
  private List<ResourceTypeEntity> additionalResourceTypes;

  @Type(JsonBinaryType.class)
  private String value;
}
