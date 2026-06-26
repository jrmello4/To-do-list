package com.todolist.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private int status;
    private String mensagem;
    private LocalDateTime timestamp;
    private List<String> erros;

    public static ErrorResponse of(int status, String mensagem, List<String> erros) {
        return ErrorResponse.builder()
                .status(status)
                .mensagem(mensagem)
                .timestamp(LocalDateTime.now())
                .erros(erros)
                .build();
    }

    public static ErrorResponse of(int status, String mensagem) {
        return of(status, mensagem, List.of());
    }
}
