package com.todolist.dto;

import com.todolist.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String nome;
    private String email;
    private Role role;
}
