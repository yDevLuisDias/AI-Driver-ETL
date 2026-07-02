package com.ydevluisdias.AiDriveEtl.service;

import com.ydevluisdias.AiDriveEtl.dto.LeadRoleDto;

import java.util.List;

/**
 * Contrato do serviço responsável pela <b>Camada de Extração (Extract)</b> do pipeline ETL.
 *
 * <p>Define as operações de leitura, validação e arquivamento do arquivo CSV de entrada.
 * A implementação concreta desta interface é {@code CsvServiceImpl}.
 *
 * <p><b>Responsabilidades:</b>
 * <ol>
 *   <li>Localizar o arquivo CSV no diretório fixo de entrada</li>
 *   <li>Validar se o arquivo existe, não está vazio e tem formato correto</li>
 *   <li>Extrair as linhas do CSV como uma lista de {@link LeadRoleDto}</li>
 *   <li>Mover o arquivo processado para o diretório {@code /processados}</li>
 * </ol>
 *
 * @author ydevluisdias
 */
public interface CsvService {

    /**
     * Executa o pipeline de extração do arquivo CSV:
     * lê, valida e retorna os dados estruturados como uma lista de DTOs.
     *
     * <p>Em caso de arquivo vazio, lança {@link com.ydevluisdias.AiDriveEtl.exception.CsvEmptyException}.
     * Em caso de formato inválido, lança {@link com.ydevluisdias.AiDriveEtl.exception.InvalidCsvFormatException}.
     * Em caso de arquivo inexistente, lança {@link java.io.FileNotFoundException}.
     *
     * @return Lista de {@link LeadRoleDto} extraídos do arquivo CSV
     * @throws Exception se houver erro na leitura ou validação do arquivo
     */
    List<LeadRoleDto> extractFromCsv() throws Exception;

    /**
     * Move o arquivo CSV original para a pasta de processados,
     * evitando reprocessamento futuro do mesmo arquivo.
     *
     * @param fileName Nome do arquivo a ser movido
     * @throws Exception se houver erro na movimentação do arquivo
     */
    void moveToProcessed(String fileName) throws Exception;
}
