package com.ydevluisdias.AiDriveEtl.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum que representa a temperatura/qualificação de um Lead no pipeline de ETL.
 *
 * <p>A classificação é feita pela IA (OpenAI via Spring AI) com base na análise
 * semântica da mensagem original ({@code inputUser}) do lead, seguindo as
 * regras de qualificação padrão de mercado.
 *
 * <ul>
 *   <li>{@link #HOT} - Intenção de compra clara e imediata. Lead pronto para vendas.</li>
 *   <li>{@link #WARM} - Interesse demonstrado, sem intenção clara. Requer nutrição de marketing.</li>
 *   <li>{@link #COLD} - Pouco engajamento. Lead a ser desconsiderado pela equipe de vendas.</li>
 *   <li>{@link #INVALID} - Spam, publicidade ou mensagem totalmente fora de contexto.</li>
 * </ul>
 *
 * @author ydevluisdias
 */
@Getter
@AllArgsConstructor
public enum LeadScore {

    /**
     * Lead Quente: Contato com intenção de compra clara e imediata.
     * Solicitou mais informações específicas, demonstrou urgência ou interesse direto de compra.
     * Deve ser repassado para a equipe de vendas.
     */
    HOT("Hot"),

    /**
     * Lead Morno: Contato com interesse demonstrado, mas sem intenção de compra clara.
     * Tirou dúvidas gerais ou está em fase inicial de pesquisa.
     * Deve ser trabalhado pelo marketing antes de ir para vendas.
     */
    WARM("Warm"),

    /**
     * Lead Frio: Contato com pouco engajamento ou interação superficial.
     * Interagiu apenas uma vez ou de forma muito genérica.
     * Deve ser desconsiderado pela equipe de vendas.
     */
    COLD("Cold"),

    /**
     * Lead Inválido: Mensagem de spam, publicidade, links suspeitos ou
     * totalmente fora do contexto de negócios da empresa.
     */
    INVALID("Invalid");

    /** Rótulo legível por humanos da temperatura do lead. */
    private final String score;
}
