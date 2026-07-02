package com.ydevluisdias.AiDriveEtl.service;

import com.ydevluisdias.AiDriveEtl.dto.EtlResponseDto;

/**
 * Contrato do serviço responsável pela orquestração do pipeline ETL completo.
 *
 * <p>Define a operação principal que coordena todas as fases do ETL:
 * Validação → Extração → Transformação (IA) → Load (PostgreSQL).
 *
 * <p>A implementação concreta desta interface é {@code LeadServiceImpl}.
 *
 * <p><b>Fluxo orquestrado:</b>
 * <ol>
 *   <li><b>Extract:</b> Delega ao {@code CsvService} a leitura e validação do CSV</li>
 *   <li><b>Transform:</b> Delega ao {@code AiAnalysisService} a análise semântica de cada lead</li>
 *   <li><b>Load:</b> Persiste a {@code LeadEntity} completa via {@code LeadRepository}</li>
 *   <li><b>Retorno:</b> Retorna um {@link EtlResponseDto} com o resumo do processamento</li>
 * </ol>
 *
 * @author ydevluisdias
 */
public interface LeadService {

    /**
     * Executa o pipeline de ETL completo para o arquivo CSV disponível no diretório de entrada.
     *
     * <p>O processamento é síncrono e limitado a <b>1 arquivo por execução</b>.
     * Ao concluir, o arquivo CSV original é movido para o diretório {@code /processados}.
     *
     * @return {@link EtlResponseDto} com o resumo do último lead processado e o status da operação
     * @throws Exception se qualquer etapa do pipeline falhar
     */
    EtlResponseDto processLeadPipeline() throws Exception;
}