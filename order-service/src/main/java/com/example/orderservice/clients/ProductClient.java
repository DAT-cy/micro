package com.example.orderservice.clients;

import com.example.orderservice.dto.ProductDto;
import com.example.orderservice.dto.client.request.ProductFilter;

import java.util.List;

public interface ProductClient {
    List<ProductDto> getProductByIds(ProductFilter productFilter);
}
