package com.ydevluisdias.AiDriveEtl.dto;

import com.ydevluisdias.AiDriveEtl.entity.LeadScore;

/**
 * DTO que representa a resposta estruturada retornada pela IA (OpenAI via Spring AI)
 * após a análise semântica da mensagem do lead.
 *
 * <p>Este record é utilizado na fase de <b>Transformação (Transform)</b> do pipeline ETL.
 * O Spring AI utiliza {@code BeanOutputConverter} para desserializar automaticamente
 * o JSON retornado pelo modelo para esta estrutura.
 *
 * <p><b>Campos preenchidos exclusivamente pela IA:</b>
 * <ul>
 *   <li>{@code tipoImovel} - Tipo de imóvel identificado na mensagem (ex: Casa, Apartamento)</li>
 *   <li>{@code orcamentoEstimado} - Orçamento mencionado pelo lead (ex: R$ 400.000,00)</li>
 *   <li>{@code condicoesEspeciais} - Condições especiais mencionadas (ex: Aceita financiamento)</li>
 *   <li>{@code temperaturaLead} - Qualificação do lead baseada nas regras de mercado ({@link LeadScore})</li>
 * </ul>
 *
 * @param tipoImovel         Tipo de imóvel identificado (pode ser null se não mencionado)
 * @param orcamentoEstimado  Orçamento estimado identificado (pode ser null se não mencionado)
 * @param condicoesEspeciais Condições especiais identificadas (pode ser null se não mencionadas)
 * @param temperaturaLead    Temperatura/qualificação do lead ({@link LeadScore})
 *
 * @author ydevluisdias
 */
public record LeadAiResponseDto(
        String tipoImovel,
        String orcamentoEstimado,
        String condicoesEspeciais,
        LeadScore temperaturaLead
) {
}
