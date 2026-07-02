package com.ydevluisdias.AiDriveEtl.dto;

import com.ydevluisdias.AiDriveEtl.entity.LeadScore;

import java.util.UUID;

/**
 * DTO que representa o resumo da resposta HTTP retornada ao cliente após
 * o processamento bem-sucedido do pipeline ETL.
 *
 * <p>Este record é utilizado na fase de <b>Load</b> do pipeline ETL, sendo serializado
 * como JSON no corpo da resposta HTTP {@code 200 OK}.
 *
 * <p><b>Exemplo de resposta JSON:</b>
 * <pre>
 * {
 *   "conversaId": "3a8e9d30-b18c-4f8e-8a18-d7e7d6cf628a",
 *   "mensagemOriginal": "Quero um apartamento no centro, aceito financiamento.",
 *   "temperaturaLead": "HOT",
 *   "status": "Processado com sucesso"
 * }
 * </pre>
 *
 * @param conversaId       UUID do lead/conversa persistido no banco
 * @param mensagemOriginal Mensagem original extraída do CSV
 * @param temperaturaLead  Qualificação final do lead ({@link LeadScore})
 * @param status           Mensagem de status do processamento
 *
 * @author ydevluisdias
 */
public record EtlResponseDto(
        UUID conversaId,
        String mensagemOriginal,
        LeadScore temperaturaLead,
        String status
) {
}
