package com.example.productservice.mapper;

import com.example.productservice.dto.ProductDto;
import com.example.productservice.dto.request.CreateProductReq;
import com.example.productservice.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product mapProduct(CreateProductReq createProductReq);

    ProductDto mapDto(Product product);
}
