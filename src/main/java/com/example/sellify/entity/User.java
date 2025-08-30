package com.example.sellify.entity;

import com.example.sellify.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    private String chatId;

    @Column(nullable = false,unique = true)
    private String username;

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Role role;
}
