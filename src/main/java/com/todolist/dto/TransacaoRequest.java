package com.todolist.dto;

import com.todolist.entity.StatusTransacao;
import com.todolist.entity.TipoTransacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransacaoRequest {

    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    private String descricao;

    @NotNull(message = "O tipo da transação é obrigatório")
    private TipoTransacao tipo;

    @NotNull(message = "O valor é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    private BigDecimal valor;

    @NotNull(message = "A data de vencimento é obrigatória")
    private LocalDate dataVencimento;

    private LocalDate dataPagamento;

    private StatusTransacao status;

    @NotNull(message = "A conta é obrigatória")
    private Long contaId;

    private Long categoriaId;

    private String observacoes;

    private Boolean parcelado;
    private Integer totalParcelas;
}