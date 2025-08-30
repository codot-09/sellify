package com.example.sellify.dto;

import lombok.*;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    private String chatId;
    private String title;
    private String description;
    private Double price;
    private String category;
    private boolean isActive;
    private Map<String, String> photoInfos = new HashMap<>();
}

