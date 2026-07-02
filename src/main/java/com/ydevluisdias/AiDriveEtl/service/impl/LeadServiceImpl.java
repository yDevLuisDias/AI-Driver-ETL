package com.ydevluisdias.AiDriveEtl.service.impl;

import com.ydevluisdias.AiDriveEtl.dto.EtlResponseDto;
import com.ydevluisdias.AiDriveEtl.dto.LeadAiResponseDto;
import com.ydevluisdias.AiDriveEtl.dto.LeadRoleDto;
import com.ydevluisdias.AiDriveEtl.entity.LeadEntity;
import com.ydevluisdias.AiDriveEtl.repository.LeadRepository;
import com.ydevluisdias.AiDriveEtl.service.CsvService;
import com.ydevluisdias.AiDriveEtl.service.LeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementação do orquestrador do pipeline ETL completo para qualificação de leads.
 *
 * <p>Esta classe coordena todas as fases do pipeline na ordem correta:
 * <ol>
 *   <li><b>Extract:</b> Delega ao {@link CsvService} a leitura, validação e extração do CSV</li>
 *   <li><b>Transform:</b> Envia cada lead para o {@link AiAnalysisService} para análise semântica pela IA</li>
 *   <li><b>Load:</b> Persiste cada {@link LeadEntity} completa no PostgreSQL via {@link LeadRepository}</li>
 *   <li><b>Retorno:</b> Retorna um {@link EtlResponseDto} com o resumo do último lead processado</li>
 * </ol>
 *
 * <p><b>Limitação de escopo atual:</b> O pipeline processa um único arquivo CSV por execução.
 * Após a extração e processamento de todos os leads do arquivo, o processo é encerrado.
 *
 * @author ydevluisdias
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final CsvService csvService;
    private final AiAnalysisService aiAnalysisService;
    private final LeadRepository leadRepository;

    /**
     * {@inheritDoc}
     *
     * <p>Executa as 3 fases do ETL (Extract, Transform, Load) de forma síncrona
     * para cada linha do arquivo CSV encontrado no diretório de entrada.
     *
     * @return {@link EtlResponseDto} com o resumo do último lead persistido
     * @throws Exception se qualquer etapa do pipeline falhar
     */
    @Override
    @Transactional
    public EtlResponseDto processLeadPipeline() throws Exception {
        log.info("[ETL] === Iniciando Pipeline de ETL de Leads ===");

        // ===== FASE 1: EXTRACT =====
        log.info("[ETL] Fase 1: Extract - Iniciando extração do CSV...");
        List<LeadRoleDto> leads = csvService.extractFromCsv();
        log.info("[ETL] Fase 1: Extract - {} leads extraídos com sucesso.", leads.size());

        LeadEntity lastSaved = null;

        for (LeadRoleDto roleDto : leads) {
            // ===== FASE 2: TRANSFORM =====
            log.info("[ETL] Fase 2: Transform - Enviando lead id={} para IA...", roleDto.id());
            LeadAiResponseDto aiResponse = aiAnalysisService.analyze(roleDto);

            // ===== FASE 3: LOAD =====
            log.info("[ETL] Fase 3: Load - Persistindo lead qualificado no PostgreSQL...");
            LeadEntity entity = buildEntity(roleDto, aiResponse);
            lastSaved = leadRepository.save(entity);
            log.info("[ETL] Fase 3: Load - Lead persistido com sucesso. UUID={} | Temperatura={}",
                    lastSaved.getId(), lastSaved.getTemperaturaLead());
        }

        log.info("[ETL] === Pipeline de ETL concluído. Total de leads salvos: {} ===", leads.size());

        return buildResponse(lastSaved);
    }

    /**
     * Constrói uma instância de {@link LeadEntity} combinando os dados extraídos do CSV
     * com os dados qualificados pela IA.
     *
     * <p>O campo {@code id} é definido como {@code null} quando não fornecido no CSV,
     * permitindo que o JPA gere o UUID automaticamente via {@code @GeneratedValue}.
     *
     * @param roleDto    DTO com os dados brutos extraídos do CSV
     * @param aiResponse DTO com os dados qualificados pela IA
     * @return A entidade pronta para ser persistida
     */
    private LeadEntity buildEntity(LeadRoleDto roleDto, LeadAiResponseDto aiResponse) {
        return LeadEntity.builder()
                .mensagemOriginal(roleDto.inputUser())
                .tipoImovel(aiResponse.tipoImovel())
                .orcamentoEstimado(aiResponse.orcamentoEstimado())
                .condicoesEspeciais(aiResponse.condicoesEspeciais())
                .temperaturaLead(aiResponse.temperaturaLead())
                .build();
        // Nota: id é omitido intencionalmente quando nulo; o @GeneratedValue do JPA o cria.
        // processadoEm é preenchido pelo @PrePersist na entidade.
    }

    /**
     * Constrói o DTO de resposta HTTP a ser retornado ao cliente.
     *
     * @param entity Última entidade salva no banco de dados
     * @return {@link EtlResponseDto} com o resumo do processamento
     */
    private EtlResponseDto buildResponse(LeadEntity entity) {
        if (entity == null) {
            return new EtlResponseDto(null, null, null, "Nenhum lead foi processado.");
        }
        return new EtlResponseDto(
                entity.getId(),
                entity.getMensagemOriginal(),
                entity.getTemperaturaLead(),
                "Processado com sucesso"
        );
    }
}
