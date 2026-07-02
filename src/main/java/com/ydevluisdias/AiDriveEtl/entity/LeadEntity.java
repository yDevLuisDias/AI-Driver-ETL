package com.ydevluisdias.AiDriveEtl.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA que representa um Lead qualificado pelo pipeline de ETL.
 *
 * <p>Um Lead é uma mensagem de conversa recebida via CSV que passa pelo pipeline:
 * <ol>
 *   <li>Validação e Extração do CSV</li>
 *   <li>Transformação e análise semântica via OpenAI (Spring AI)</li>
 *   <li>Persistência nesta tabela no PostgreSQL</li>
 * </ol>
 *
 * <p><b>Campos de responsabilidade do CSV:</b> {@code id}, {@code mensagemOriginal}
 * <p><b>Campos de responsabilidade da IA:</b> {@code tipoImovel}, {@code orcamentoEstimado},
 * {@code condicoesEspeciais}, {@code temperaturaLead}
 * <p><b>Campos de responsabilidade do sistema:</b> {@code processadoEm}
 *
 * @author ydevluisdias
 */
@Entity
@Table(name = "tb_leads_qualificados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadEntity {

    /**
     * Identificador único da conversa/lead.
     * Se não fornecido no CSV, é gerado automaticamente pelo JPA via UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Mensagem original recebida no CSV, correspondente ao campo {@code inputUser}.
     * Este campo é obrigatório e armazenado como TEXT no banco.
     */
    @Column(name = "mensagem_original", columnDefinition = "TEXT", nullable = false)
    private String mensagemOriginal;
 
    /**
     * Tipo de imóvel identificado pela IA a partir da mensagem do lead.
     * Exemplos: Casa, Apartamento, Terreno, Comercial.
     * Preenchido exclusivamente pelo motor de IA.
     */
    @Column(name = "tipo_imovel")
    private String tipoImovel;

    /**
     * Orçamento estimado identificado pela IA com base na mensagem do lead.
     * Exemplo: R$ 500.000,00.
     * Preenchido exclusivamente pelo motor de IA.
     */
    @Column(name = "orcamento_estimado")
    private String orcamentoEstimado;

    /**
     * Condições especiais identificadas pela IA (ex: Aceita financiamento, Permuta, FGTS).
     * Armazenado como TEXT para comportar múltiplas condições.
     * Preenchido exclusivamente pelo motor de IA.
     */
    @Column(name = "condicoes_especiais", columnDefinition = "TEXT")
    private String condicoesEspeciais;

    /**
     * Temperatura/qualificação do lead, classificada pela IA.
     * Mapeada para o enum {@link LeadScore}:
     * <ul>
     *   <li>{@code HOT} - Intenção de compra clara e imediata</li>
     *   <li>{@code WARM} - Interesse demonstrado, mas sem intenção clara de compra</li>
     *   <li>{@code COLD} - Pouco engajamento, interação superficial</li>
     *   <li>{@code INVALID} - Spam, publicidade ou mensagem fora de contexto</li>
     * </ul>
     * Preenchido exclusivamente pelo motor de IA.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "temperatura_lead")
    private LeadScore temperaturaLead;

    /**
     * Data e hora em que o lead foi processado pelo pipeline ETL.
     * Preenchido automaticamente pelo JPA no momento da persistência via {@link #onCreate()}.
     */
    @Column(name = "processado_em", nullable = false, updatable = false)
    private LocalDateTime processadoEm;

    /**
     * Callback do JPA executado automaticamente antes de inserir o registro.
     * Define o timestamp de processamento com o instante atual.
     */
    @PrePersist
    protected void onCreate() {
        this.processadoEm = LocalDateTime.now();
    }
}