package com.example.orderservice.clients.impl;

import com.example.orderservice.clients.ProductClient;
import com.example.orderservice.dto.BaseResponse;
import com.example.orderservice.dto.ProductDto;
import com.example.orderservice.dto.client.request.ProductFilter;
import com.example.orderservice.entity.BaseEntity;
import com.example.orderservice.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductClientImpl implements ProductClient {


    @Override
    public List<ProductDto> getProductByIds(ProductFilter productFilter) {

        WebClient.Builder builder =  WebClient.builder();
        BaseResponse<List<ProductDto>> response = builder.build()
                .post()
                .uri("http://localhost:8888/v1/products/search")
                .bodyValue(productFilter)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<BaseResponse<List<ProductDto>>>() {
                })
                .block();
        if(response.getData() == null || response == null){
            throw new ApplicationException("Không có dữ liệu product");
        }
        return response.getData();
    }
}
