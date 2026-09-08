package com.todolist.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponse {

    private Long id;
    private String nomeOriginal;
    private String tipoConteudo;
    private Long tamanho;
    private LocalDateTime dataCriacao;
    private String urlDownload;
    private Boolean isImagem;
}
