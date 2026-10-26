package com.example.productservice.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
public class ProductDocument {
    @Id
    private String id;
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Integer)
    private Integer price;
    @Field(type = FieldType.Integer)
    private Integer stock;
    @Field(type = FieldType.Keyword)
    private String categoryId;
}
