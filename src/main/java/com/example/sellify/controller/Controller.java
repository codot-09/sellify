package com.example.sellify.controller;

import com.example.sellify.dto.response.ProductDisplayResponse;
import com.example.sellify.dto.response.ProductResponse;
import com.example.sellify.entity.enums.Category;
import com.example.sellify.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/sellify")
@RequiredArgsConstructor
public class Controller {
    private final ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<List<ProductDisplayResponse>> getProducts(
            @RequestParam Category category
    ){
        return ResponseEntity.ok(productService.getProducts(category));
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(productService.getProduct(id));
    }
}
