package com.example.productservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Setter
@Getter
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private String id ;
    private String name;
    private Integer price;
    private Integer stock;

    @Column(name = "category_id")
    private String categoryId;

}
