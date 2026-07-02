package com.ydevluisdias.AiDriveEtl.exception;

/**
 * Exceção lançada quando o arquivo CSV fornecido para processamento
 * está vazio ou não contém linhas de dados válidas.
 *
 * <p>Esta exceção é tratada pelo {@code GlobalExceptionHandler} e resulta
 * em uma resposta HTTP {@code 400 Bad Request}.
 *
 * @author ydevluisdias
 */
public class CsvEmptyException extends RuntimeException {

    /**
     * Cria uma nova instância com a mensagem de erro padrão.
     */
    public CsvEmptyException() {
        super("O arquivo CSV está vazio ou não contém dados válidos.");
    }

    /**
     * Cria uma nova instância com uma mensagem de erro customizada.
     *
     * @param message Mensagem descritiva do erro
     */
    public CsvEmptyException(String message) {
        super(message);
    }
}
