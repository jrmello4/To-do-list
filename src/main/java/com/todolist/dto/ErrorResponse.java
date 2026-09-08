package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ErrorResponse", description = "Corpo padrão retornado quando a requisição falha")
public class ErrorResponse {

    @Schema(description = "Código HTTP da resposta", example = "400")
    private int status;

    @Schema(description = "Mensagem descrevendo o erro", example = "Erro de validação")
    private String mensagem;

    @Schema(description = "Momento em que o erro ocorreu", example = "2026-06-26T10:00:00")
    private LocalDateTime timestamp;

    @Schema(
            description = "Lista de erros detalhados. Preenchida em falhas de validação, "
                    + "vazia nos demais casos.",
            example = "[\"titulo: O título é obrigatório\"]"
    )
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
