package com.ydevluisdias.AiDriveEtl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Handler global de exceções para a API REST do AiDriveEtl.
 *
 * <p>Centraliza o tratamento de exceções do pipeline ETL, convertendo-as
 * em respostas HTTP semânticas e estruturadas em JSON.
 *
 * <p><b>Mapeamento de exceções para status HTTP:</b>
 * <ul>
 *   <li>{@link CsvEmptyException} → {@code 400 Bad Request}</li>
 *   <li>{@link InvalidCsvFormatException} → {@code 400 Bad Request}</li>
 *   <li>{@link java.io.FileNotFoundException} → {@code 404 Not Found}</li>
 *   <li>{@link Exception} (genérica) → {@code 500 Internal Server Error}</li>
 * </ul>
 *
 * @author ydevluisdias
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata a exceção de CSV vazio.
     *
     * @param ex Exceção capturada
     * @return ResponseEntity com status 400 e corpo JSON descritivo
     */
    @ExceptionHandler(CsvEmptyException.class)
    public ResponseEntity<Map<String, Object>> handleCsvEmpty(CsvEmptyException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Trata a exceção de formato de CSV inválido.
     *
     * @param ex Exceção capturada
     * @return ResponseEntity com status 400 e corpo JSON descritivo
     */
    @ExceptionHandler(InvalidCsvFormatException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCsvFormat(InvalidCsvFormatException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Trata exceções de arquivo não encontrado.
     *
     * @param ex Exceção capturada
     * @return ResponseEntity com status 404 e corpo JSON descritivo
     */
    @ExceptionHandler(java.io.FileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFileNotFound(java.io.FileNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Nenhum arquivo CSV encontrado no diretório de entrada: " + ex.getMessage());
    }

    /**
     * Trata exceções genéricas não capturadas pelos handlers específicos.
     *
     * @param ex Exceção capturada
     * @return ResponseEntity com status 500 e corpo JSON descritivo
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no processamento do pipeline ETL: " + ex.getMessage());
    }

    /**
     * Constrói um corpo de resposta de erro padronizado em JSON.
     *
     * @param status  Código de status HTTP
     * @param message Mensagem de erro descritiva
     * @return ResponseEntity com o corpo padronizado
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );
        return ResponseEntity.status(status).body(body);
    }
}
