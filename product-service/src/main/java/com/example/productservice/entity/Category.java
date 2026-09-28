package com.example.productservice.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private String id ;
    private String name;
    @Column(name ="parent_id")
    private String parentId;

}
