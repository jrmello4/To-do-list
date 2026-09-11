package com.todolist.dto;

import com.todolist.entity.TipoTransacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaTransacaoRequest {

    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String nome;

    @NotNull(message = "O tipo é obrigatório (RECEITA ou DESPESA)")
    private TipoTransacao tipo;

    @Size(max = 50, message = "O ícone deve ter no máximo 50 caracteres")
    private String icone;

    @Size(max = 10, message = "A cor deve ter no máximo 10 caracteres")
    private String cor;

    private BigDecimal limiteMensal;
}