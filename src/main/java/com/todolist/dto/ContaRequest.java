package com.todolist.dto;

import com.todolist.entity.TipoConta;
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
public class ContaRequest {

    @NotBlank(message = "O nome da conta é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String nome;

    @NotNull(message = "O tipo da conta é obrigatório")
    private TipoConta tipo;

    private BigDecimal saldoInicial;

    @Size(max = 10, message = "A cor deve ter no máximo 10 caracteres")
    private String cor;
}