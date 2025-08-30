package com.example.sellify.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {
    private String name;
    private String description;
    private Double price;
    private List<String> photoUrls;
    private String ownerUsername;
}
