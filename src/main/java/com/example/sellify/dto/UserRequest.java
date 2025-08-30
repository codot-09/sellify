package com.example.sellify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@Builder
public class UserRequest {
    private String chatId;
    private String firstName;
    private String lastName;
    private String username;
}
