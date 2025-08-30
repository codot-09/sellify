package com.example.sellify.service;

import com.example.sellify.dto.UserRequest;
import com.example.sellify.entity.User;
import com.example.sellify.entity.enums.Role;
import com.example.sellify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public String saveUser(UserRequest request){
        if (userRepository.existsByChatId(request.getChatId())) return "Sizni yana ko'rib turganimdan xursandman !";

        User user = User.builder()
                .chatId(request.getChatId())
                .username(request.getUsername() != null ? request.getUsername() : "")
                .firstName(request.getFirstName() != null ? request.getFirstName() : "")
                .lastName(request.getLastName() != null ? request.getLastName() : "")
                .role(Role.USER)
                .build();

        userRepository.save(user);

        return "Salom jamoamizga xush kelibsiz !";
    }

    public long count(){
        return userRepository.count();
    }
}
