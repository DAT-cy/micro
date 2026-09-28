package com.example.productservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CreateProductReq {
    @NotEmpty
    private String name;
    @NotNull
    private Integer price;
    @NotNull
    private Integer stock;
    @NotEmpty
    private String categoryId;
}
