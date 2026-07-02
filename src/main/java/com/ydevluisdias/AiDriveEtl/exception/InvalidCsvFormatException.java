package com.ydevluisdias.AiDriveEtl.exception;

/**
 * Exceção lançada quando o arquivo encontrado no diretório de entrada
 * não possui a extensão {@code .csv} esperada ou sua estrutura de colunas
 * é incompatível com o formato esperado ({@code id}, {@code inputUser}).
 *
 * <p>Esta exceção é tratada pelo {@code GlobalExceptionHandler} e resulta
 * em uma resposta HTTP {@code 400 Bad Request}.
 *
 * @author ydevluisdias
 */
public class InvalidCsvFormatException extends RuntimeException {

    /**
     * Cria uma nova instância com a mensagem de erro padrão.
     */
    public InvalidCsvFormatException() {
        super("O arquivo fornecido não possui um formato CSV válido ou as colunas esperadas (id, inputUser) estão ausentes.");
    }

    /**
     * Cria uma nova instância com uma mensagem de erro customizada.
     *
     * @param message Mensagem descritiva do erro
     */
    public InvalidCsvFormatException(String message) {
        super(message);
    }
}
