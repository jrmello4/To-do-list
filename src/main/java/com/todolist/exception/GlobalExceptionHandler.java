package com.todolist.exception;

import com.todolist.dto.ErrorResponse;
import com.todolist.repository.OrdenacaoDeTarefas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> erros = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "Erro de validação",
                        erros
                ));
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErrorResponse> handleEmailDuplicado(EmailJaCadastradoException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler({NomeDeProjetoEmUsoException.class, NomeDeEtiquetaEmUsoException.class,
            NomeDeHabitoEmUsoException.class})
    public ResponseEntity<ErrorResponse> handleNomeDeProjeto(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    /**
     * Ordenação recusada pela lista branca de OrdenacaoDeTarefas.
     *
     * A lista de campos válidos vem de lá, e não escrita à mão aqui: eram dois
     * lugares para manter iguais, e já estavam diferentes — `ordem`, que a
     * própria interface usa, faltava nesta mensagem.
     */
    @ExceptionHandler(OrdenacaoInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleOrdenacaoRecusada(OrdenacaoInvalidaException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        List.of("Campos ordenáveis: " + OrdenacaoDeTarefas.listados())));
    }

    /**
     * Rede de baixo para as demais listagens (projetos, etiquetas, hábitos),
     * que não passam pela lista branca: ?sort=campoQueNaoExiste chegaria ao
     * Spring Data e explodiria em 500. É erro de quem chama, não do servidor.
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handleOrdenacaoInvalida(PropertyReferenceException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "Não é possível ordenar por \"" + ex.getPropertyName() + "\""));
    }

    /** Prioridade ou data em formato inválido no parâmetro da URL. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleParametroInvalido(
            MethodArgumentTypeMismatchException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "Valor inválido para o parâmetro \"" + ex.getName() + "\"",
                        List.of(String.valueOf(ex.getValue()))));
    }

    /**
     * Devolve Retry-After junto: sem ele o cliente não tem como saber quanto
     * esperar e volta a tentar em laço, que é o que acabou de ser barrado.
     */
    @ExceptionHandler(TentativasExcedidasException.class)
    public ResponseEntity<ErrorResponse> handleTentativasExcedidas(TentativasExcedidasException ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getEspera().toSeconds()))
                .body(ErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS.value(), ex.getMessage()));
    }

    @ExceptionHandler(SenhaAtualIncorretaException.class)
    public ResponseEntity<ErrorResponse> handleSenhaAtualIncorreta(SenhaAtualIncorretaException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleFalhaDeAutenticacao(AuthenticationException ex) {
        // Mensagem única para e-mail inexistente e senha errada: distinguir os
        // dois permitiria descobrir quais e-mails têm conta cadastrada.
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        "E-mail ou senha incorretos"
                ));
    }

    /**
     * Duas gravações concorrentes na mesma tarefa.
     *
     * 409 e não 500: não houve falha do servidor, houve uma edição feita em
     * cima de uma versão que já não era a atual. A mensagem diz o que fazer,
     * porque a informação que o cliente tem na tela está velha.
     */
    @ExceptionHandler(ConflitoDeVersaoException.class)
    public ResponseEntity<ErrorResponse> handleConflitoDeVersao(ConflitoDeVersaoException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleConcorrencia(
            ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "Esta tarefa foi alterada em outro lugar. Recarregue e refaça a edição."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage()
                ));
    }

    /**
     * Rota que não existe.
     *
     * Sem este tratamento a exceção cairia no genérico abaixo e um endereço
     * digitado errado voltaria como 500, dizendo que o servidor quebrou quando
     * quem errou foi o cliente. Registrar como erro também poluiria o log.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleRotaInexistente(NoResourceFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "Recurso não encontrado: " + ex.getResourcePath()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // A resposta é genérica de propósito, para não vazar detalhe interno ao
        // cliente. Sem este log o erro não deixaria rastro nenhum.
        log.error("Erro não tratado ao processar a requisição", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Erro interno do servidor"
                ));
    }
}
