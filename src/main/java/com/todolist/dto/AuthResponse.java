package com.todolist.dto;

import com.todolist.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private Long id;
    private String nome;
    private String email;
    private Role role;
}
