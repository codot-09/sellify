package com.example.sellify.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDisplayResponse {
    private Long id;
    private String name;
    private Double price;
    private String imageUrl;
}
