package com.ydevluisdias.AiDriveEtl.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Spring AI para o projeto AiDriveEtl.
 *
 * <p>Registra o {@link ChatClient} como um bean gerenciado pelo Spring,
 * utilizando o builder auto-configurado pelo Spring AI com as propriedades
 * definidas em {@code application.yaml} (modelo, temperatura, API key).
 *
 * @author ydevluisdias
 */
@Configuration
public class AiConfig {

    /**
     * Cria e registra um {@link ChatClient} configurado para uso com a OpenAI.
     *
     * <p>O builder é injetado automaticamente pelo Spring AI com base nas
     * propriedades {@code spring.ai.openai.*} do {@code application.yaml}.
     *
     * @param builder Builder auto-configurado pelo Spring AI
     * @return Instância de {@link ChatClient} pronta para uso
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
