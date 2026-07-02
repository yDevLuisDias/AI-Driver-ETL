package com.ydevluisdias.AiDriveEtl.controller;

import com.ydevluisdias.AiDriveEtl.dto.EtlResponseDto;
import com.ydevluisdias.AiDriveEtl.service.LeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST responsável por expor o endpoint de acionamento do pipeline
 * ETL de Leads.
 *
 * <p>
 * Recebe a requisição HTTPS e delega o processamento completo ao
 * {@link LeadService},
 * retornando uma resposta estruturada com o resumo da operação.
 *
 * <p>
 * <b>Endpoints disponíveis:</b>
 * <ul>
 * <li>{@code POST /api/leads/process} - Aciona o pipeline ETL completo</li>
 * </ul>
 *
 * <p>
 * O pipeline executa as seguintes fases de forma síncrona:
 * <ol>
 * <li>Validação e Extração do arquivo CSV do diretório local</li>
 * <li>Transformação e análise semântica de cada lead via OpenAI</li>
 * <li>Persistência dos leads qualificados no PostgreSQL</li>
 * </ol>
 *
 * <p>
 * Erros de validação são tratados globalmente pelo
 * {@code GlobalExceptionHandler}.
 *
 * @author ydevluisdias
 */
@Slf4j
@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    /**
     * Aciona o pipeline ETL completo de qualificação de leads.
     *
     * <p>
     * Lê o único arquivo CSV disponível no diretório de entrada local,
     * processa cada lead através da IA e persiste os resultados no PostgreSQL.
     * Após o processamento, o arquivo é movido para a pasta {@code /processados}.
     *
     * @return {@code 200 OK} com um {@link EtlResponseDto} contendo o resumo do
     *         processamento,
     *         ou um código de erro adequado em caso de falha (tratado pelo
     *         {@code GlobalExceptionHandler})
     */
    @PostMapping("/process")
    public ResponseEntity<EtlResponseDto> processLeads() throws Exception {
        log.info("[Controller] Recebida requisição para iniciar o pipeline ETL.");
        EtlResponseDto response = leadService.processLeadPipeline();
        log.info("[Controller] Pipeline ETL concluído. Retornando 200 OK.");
        return ResponseEntity.ok(response);
    }
}
