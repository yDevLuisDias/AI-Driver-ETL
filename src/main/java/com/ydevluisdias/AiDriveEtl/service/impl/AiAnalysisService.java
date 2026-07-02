package com.ydevluisdias.AiDriveEtl.service.impl;

import com.ydevluisdias.AiDriveEtl.dto.LeadAiResponseDto;
import com.ydevluisdias.AiDriveEtl.dto.LeadRoleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável pela <b>Camada de Transformação (Transform)</b> do pipeline ETL.
 *
 * <p>Recebe um {@link LeadRoleDto} com a mensagem do lead e a envia para o modelo
 * da OpenAI (via Spring AI) para análise semântica e qualificação.
 *
 * <p><b>Responsabilidade da IA:</b> A IA preenche <em>exclusivamente</em> os campos cognitivos:
 * <ul>
 *   <li>{@code tipoImovel} - Tipo de imóvel identificado na mensagem</li>
 *   <li>{@code orcamentoEstimado} - Orçamento mencionado pelo lead</li>
 *   <li>{@code condicoesEspeciais} - Condições especiais mencionadas</li>
 *   <li>{@code temperaturaLead} - Qualificação do lead (HOT, WARM, COLD, INVALID)</li>
 * </ul>
 *
 * <p>O {@link BeanOutputConverter} do Spring AI é utilizado para garantir a desserialização
 * segura do JSON retornado pelo modelo para o record {@link LeadAiResponseDto}.
 *
 * @author ydevluisdias
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    private final ChatClient chatClient;

    /**
     * System Prompt injetado na IA com as regras de negócio e qualificação de leads.
     *
     * <p>Define o papel da IA, o domínio de atuação (mercado imobiliário) e as regras
     * de classificação de temperatura de leads baseadas em padrões de mercado.
     */
    private static final String SYSTEM_PROMPT = """
            Você é um analista de qualificação de leads do mercado imobiliário.
            Sua única responsabilidade é analisar mensagens de potenciais clientes e extrair informações estruturadas.

            Regras de Qualificação (temperaturaLead):
            - HOT: Contato com intenção de compra clara e imediata. Solicitou informações específicas de venda, demonstrou urgência ou interesse direto de aquisição.
            - WARM: Lead com interesse demonstrado, mas sem intenção clara de compra. Está em fase de descoberta ou tirou dúvidas gerais.
            - COLD: Lead com pouco engajamento. Interação superficial ou pontual. Deve ser desconsiderado pela equipe de vendas.
            - INVALID: Mensagem de spam, publicidade, links suspeitos ou totalmente fora do contexto imobiliário.

            Instruções de extração:
            - tipoImovel: Tipo de imóvel mencionado (ex: Casa, Apartamento, Terreno, Comercial). Use null se não mencionado.
            - orcamentoEstimado: Valor financeiro mencionado (ex: R$ 400.000,00). Use null se não mencionado.
            - condicoesEspeciais: Condições especiais mencionadas (ex: "Aceita financiamento", "Permuta", "FGTS"). Use null se não mencionadas.
            - temperaturaLead: Classifique obrigatoriamente com um dos valores: HOT, WARM, COLD ou INVALID.

            Responda APENAS com o JSON estruturado no formato especificado. Sem texto adicional.
            """;

    /**
     * Analisa semanticamente a mensagem de um lead usando o modelo da OpenAI e
     * retorna os dados estruturados extraídos.
     *
     * <p>Utiliza o {@link BeanOutputConverter} do Spring AI para garantir que o
     * modelo retorne um JSON compatível com o record {@link LeadAiResponseDto}.
     *
     * @param lead DTO com o ID e a mensagem original do lead a ser analisado
     * @return {@link LeadAiResponseDto} com os campos extraídos e a qualificação da IA
     */
    public LeadAiResponseDto analyze(LeadRoleDto lead) {
        log.info("[ETL - Transform] Enviando lead para análise da IA. id={}", lead.id());

        BeanOutputConverter<LeadAiResponseDto> converter = new BeanOutputConverter<>(LeadAiResponseDto.class);

        String userMessage = """
                Analise a seguinte mensagem de um potencial cliente do mercado imobiliário e extraia as informações no formato JSON especificado.

                Mensagem: "%s"

                %s
                """.formatted(lead.inputUser(), converter.getFormat());

        String rawResponse = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .call()
                .content();

        log.debug("[ETL - Transform] Resposta bruta da IA: {}", rawResponse);

        LeadAiResponseDto response = converter.convert(rawResponse);
        log.info("[ETL - Transform] Lead qualificado. id={} | temperatura={}", lead.id(), response.temperaturaLead());

        return response;
    }
}
