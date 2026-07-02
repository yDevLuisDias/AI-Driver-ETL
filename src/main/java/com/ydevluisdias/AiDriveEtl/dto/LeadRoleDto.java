package com.ydevluisdias.AiDriveEtl.dto;

/**
 * DTO (Data Transfer Object) que representa uma linha extraída do arquivo CSV de entrada.
 *
 * <p>Este record transporta os dados brutos lidos pelo {@code CsvService} após a fase de
 * <b>Extração (Extract)</b> do pipeline ETL, antes de serem enviados para análise da IA.
 *
 * <p><b>Campos:</b>
 * <ul>
 *   <li>{@code id} - Identificador da conversa, lido da coluna {@code id} do CSV.
 *       Pode ser {@code null} quando não fornecido; o JPA gerará um UUID automaticamente.</li>
 *   <li>{@code inputUser} - Mensagem original do usuário/lead, lida da coluna {@code inputUser}
 *       do CSV. Este é o texto que será analisado pela IA.</li>
 * </ul>
 *
 * <p><b>Exemplo de linha CSV correspondente:</b>
 * <pre>
 *   ,Olá, gostaria de saber se o apartamento no centro está disponível.
 * </pre>
 * <p><b>Exemplo de instância gerada:</b>
 * <pre>
 *   new LeadRoleDto(null, "Olá, gostaria de saber se o apartamento no centro está disponível.");
 * </pre>
 *
 * @param id        ID da conversa (pode ser null se não informado no CSV)
 * @param inputUser Mensagem original do lead
 *
 * @author ydevluisdias
 */
public record LeadRoleDto(String id, String inputUser) {
}
