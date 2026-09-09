package com.todolist.controller;

import com.todolist.dto.DadosExportados;
import com.todolist.dto.ErrorResponse;
import com.todolist.dto.ImportacaoResponse;
import com.todolist.security.UsuarioAutenticado;
import com.todolist.service.DadosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dados")
@RequiredArgsConstructor
@Tag(name = "Dados", description = "Exportar e importar tudo o que a conta tem")
public class DadosController {

    private final DadosService dadosService;

    @GetMapping("/exportar")
    @Operation(
            summary = "Exportar a conta inteira",
            description = """
                    Devolve projetos, etiquetas, tarefas e hábitos num único JSON, com
                    `Content-Disposition: attachment` para o navegador salvar como arquivo.

                    Projetos e etiquetas são referenciados por nome, não por id: ids só valem
                    dentro do banco de origem, e por nome o arquivo pode ser importado noutra
                    instalação — que é o ponto de existir uma exportação.
                    """
    )
    public ResponseEntity<DadosExportados> exportar(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        String arquivo = "todolist-" + LocalDate.now() + ".json";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + arquivo + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dadosService.exportar(usuario.getId()));
    }

    @PostMapping("/importar")
    @Operation(
            summary = "Importar um arquivo exportado",
            description = """
                    Soma ao que já existe, em vez de substituir — substituir exigiria apagar
                    tudo antes, e um arquivo errado levaria a conta inteira junto.

                    Projetos, etiquetas e hábitos com nome já existente são reaproveitados e
                    aparecem na lista `reaproveitados` da resposta. Tarefas são sempre criadas:
                    não há como saber se uma tarefa de mesmo título é a mesma ou outra parecida.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Importado"),
            @ApiResponse(responseCode = "400",
                    description = "Versão incompatível ou arquivo acima do limite",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ImportacaoResponse> importar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody DadosExportados dados) {
        return ResponseEntity.ok(dadosService.importar(usuario.getId(), dados));
    }
}
