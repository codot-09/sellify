package com.example.sellify.entity;

import com.example.sellify.dto.ProductRequest;
import com.example.sellify.entity.enums.ProductStep;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSession {
    private ProductRequest productRequest;
    private ProductStep step;
}
