package com.ydevluisdias.AiDriveEtl.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.ydevluisdias.AiDriveEtl.dto.LeadRoleDto;
import com.ydevluisdias.AiDriveEtl.exception.CsvEmptyException;
import com.ydevluisdias.AiDriveEtl.exception.InvalidCsvFormatException;
import com.ydevluisdias.AiDriveEtl.service.CsvService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementação da <b>Camada de Extração (Extract)</b> do pipeline ETL.
 *
 * <p>Responsável por:
 * <ol>
 *   <li>Localizar o único arquivo CSV no diretório fixo de entrada</li>
 *   <li>Validar extensão, existência e conteúdo do arquivo</li>
 *   <li>Parsear as linhas do CSV usando OpenCSV, mapeando para {@link LeadRoleDto}</li>
 *   <li>Mover o arquivo processado para a pasta {@code /processados}</li>
 * </ol>
 *
 * <p><b>Formato esperado do CSV:</b>
 * <pre>
 *   id,inputUser
 *   ,Mensagem do lead aqui...
 *   3a8e9d30-...,Outra mensagem aqui...
 * </pre>
 *
 * @author ydevluisdias
 */
@Slf4j
@Service
public class CsvServiceImpl implements CsvService {

    private static final String[] EXPECTED_HEADERS = {"id", "inputUser"};

    /** Diretório de entrada fixo configurado em application.yaml */
    @Value("${etl.csv.input-dir}")
    private String inputDir;

    /** Diretório para onde os arquivos processados são movidos */
    @Value("${etl.csv.processed-dir}")
    private String processedDir;

    /**
     * {@inheritDoc}
     *
     * <p>Localiza o primeiro (e único) arquivo {@code .csv} no diretório de entrada,
     * valida seu conteúdo e extrai os dados como uma lista de {@link LeadRoleDto}.
     *
     * @return Lista de DTOs extraídos do CSV
     * @throws FileNotFoundException     se nenhum arquivo CSV for encontrado
     * @throws InvalidCsvFormatException se a extensão ou colunas forem inválidas
     * @throws CsvEmptyException         se o arquivo não tiver linhas de dados
     * @throws IOException               se houver erro na leitura do arquivo
     * @throws CsvValidationException    se o OpenCSV não conseguir parsear o arquivo
     */
    @Override
    public List<LeadRoleDto> extractFromCsv() throws Exception {
        File csvFile = findCsvFile();
        log.info("[ETL - Extract] Arquivo encontrado: {}", csvFile.getName());

        validateCsvFile(csvFile);

        List<LeadRoleDto> leads = parseCsvFile(csvFile);

        moveToProcessed(csvFile.getName());
        log.info("[ETL - Extract] Extração concluída. {} leads extraídos.", leads.size());

        return leads;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Move o arquivo do diretório de entrada para o diretório {@code /processados}.
     * Cria o diretório de destino se ele não existir.
     *
     * @param fileName Nome do arquivo a ser movido
     * @throws IOException se houver erro na operação de movimentação
     */
    @Override
    public void moveToProcessed(String fileName) throws Exception {
        Path source = Paths.get(inputDir, fileName);
        Path targetDir = Paths.get(processedDir);
        Files.createDirectories(targetDir);

        Path target = targetDir.resolve(fileName);
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        log.info("[ETL - Extract] Arquivo '{}' movido para '{}'.", fileName, processedDir);
    }

    /**
     * Localiza o primeiro arquivo {@code .csv} no diretório de entrada.
     *
     * @return O arquivo CSV encontrado
     * @throws FileNotFoundException se o diretório estiver vazio ou não houver arquivos .csv
     */
    private File findCsvFile() throws FileNotFoundException {
        File dir = new File(inputDir);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new FileNotFoundException("Diretório de entrada não encontrado: " + inputDir);
        }

        File[] csvFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            throw new FileNotFoundException("Nenhum arquivo .csv encontrado em: " + inputDir);
        }

        return csvFiles[0];
    }

    /**
     * Valida o arquivo CSV quanto à extensão e ao conteúdo.
     * Lança exceções específicas para cada tipo de falha.
     *
     * @param file Arquivo a ser validado
     * @throws InvalidCsvFormatException se a extensão não for .csv
     * @throws CsvEmptyException         se o arquivo estiver vazio (0 bytes)
     */
    private void validateCsvFile(File file) {
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            throw new InvalidCsvFormatException("O arquivo '" + file.getName() + "' não possui extensão .csv.");
        }
        if (file.length() == 0) {
            throw new CsvEmptyException("O arquivo '" + file.getName() + "' está vazio (0 bytes).");
        }
    }

    /**
     * Faz o parse do arquivo CSV usando OpenCSV, valida o cabeçalho e mapeia
     * cada linha para um {@link LeadRoleDto}.
     *
     * @param file Arquivo CSV a ser parseado
     * @return Lista de DTOs extraídos
     * @throws InvalidCsvFormatException se o cabeçalho não contiver as colunas esperadas
     * @throws CsvEmptyException         se o arquivo não tiver linhas de dados (apenas cabeçalho)
     * @throws IOException               se houver erro na leitura
     * @throws CsvValidationException    se o OpenCSV não conseguir processar o arquivo
     */
    private List<LeadRoleDto> parseCsvFile(File file) throws IOException, CsvValidationException {
        List<LeadRoleDto> leads = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            // Lê e valida o cabeçalho
            String[] header = reader.readNext();
            validateHeader(header);

            // Lê as linhas de dados
            String[] line;
            while ((line = reader.readNext()) != null) {
                String id = (line[0] == null || line[0].isBlank()) ? null : line[0].trim();
                String inputUser = line.length > 1 ? line[1].trim() : "";

                if (!inputUser.isBlank()) {
                    leads.add(new LeadRoleDto(id, inputUser));
                }
            }
        }

        if (leads.isEmpty()) {
            throw new CsvEmptyException("O arquivo CSV não contém linhas de dados válidas (apenas cabeçalho ou linhas em branco).");
        }

        return leads;
    }

    /**
     * Valida se o cabeçalho do CSV contém as colunas esperadas: {@code id} e {@code inputUser}.
     *
     * @param header Array de strings representando o cabeçalho lido do CSV
     * @throws InvalidCsvFormatException se o cabeçalho for nulo ou as colunas estiverem ausentes
     */
    private void validateHeader(String[] header) {
        if (header == null || header.length < 2) {
            throw new InvalidCsvFormatException("O CSV não possui o cabeçalho esperado com as colunas: " + Arrays.toString(EXPECTED_HEADERS));
        }

        boolean hasId = Arrays.stream(header).anyMatch(h -> h.trim().equalsIgnoreCase("id"));
        boolean hasInputUser = Arrays.stream(header).anyMatch(h -> h.trim().equalsIgnoreCase("inputUser"));

        if (!hasId || !hasInputUser) {
            throw new InvalidCsvFormatException(
                    "Cabeçalho inválido. Esperado: " + Arrays.toString(EXPECTED_HEADERS) +
                    " | Encontrado: " + Arrays.toString(header)
            );
        }
    }
}
