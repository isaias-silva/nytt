package dev.nytt.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name="files")
public class FileEntity extends PanacheEntityBase {
   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    public String externalId;

    public FileEntity(String externalId) {
        this.externalId=externalId;
    }
}
