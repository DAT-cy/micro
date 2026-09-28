package com.example.productservice.controller;


import com.example.productservice.dto.BaseResponse;
import com.example.productservice.dto.ProductDto;
import com.example.productservice.dto.request.CreateProductReq;
import com.example.productservice.dto.request.ProductFilter;
import com.example.productservice.entity.Product;
import com.example.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<BaseResponse<Product>> create(
            @RequestBody @Valid CreateProductReq createProductReq
    ) {
        return ResponseEntity.ok(
                new BaseResponse<>(productService.create(createProductReq),"result")
        );
    }

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<List<ProductDto>>> search(
            @RequestBody @Valid ProductFilter productFilter
    ) {
        return ResponseEntity.ok(
                new BaseResponse<>(productService.search(productFilter),"result")
        );
    }
}
