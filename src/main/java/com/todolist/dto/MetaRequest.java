package com.todolist.dto;

import jakarta.validation.constraints.DecimalMax;
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
public class MetaRequest {

    @NotBlank(message = "O título da meta é obrigatório")
    @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
    private String titulo;

    private String descricao;

    @Size(max = 50, message = "A categoria deve ter no máximo 50 caracteres")
    private String categoria;

    @NotNull(message = "O valor alvo é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor alvo deve ser maior que zero")
    private BigDecimal valorAlvo;

    private BigDecimal valorAtual;

    @Size(max = 20, message = "A unidade deve ter no máximo 20 caracteres")
    private String unidade;

    private LocalDate prazo;

    @Size(max = 10, message = "A cor deve ter no máximo 10 caracteres")
    private String cor;

    @Size(max = 50, message = "O ícone deve ter no máximo 50 caracteres")
    private String icone;

    /** % da receita paga que vira aporte automático nesta meta (ex: 10 = 10%). */
    @DecimalMin(value = "0.00", message = "O percentual não pode ser negativo")
    @DecimalMax(value = "100.00", message = "O percentual não pode passar de 100")
    private BigDecimal autoAportePercentual;

    private Boolean autoAporteAtivo;
}
