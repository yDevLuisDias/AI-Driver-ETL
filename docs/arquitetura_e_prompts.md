# Arquitetura e Prompts em Camadas (AiDriveEtl)

Este documento organiza a arquitetura, regras de negócio e os respectivos prompts de desenvolvimento do projeto **AiDriveEtl** em 5 camadas lógicas, com todas as especificações e regras de qualificação refinadas.

---

## Camada 1: Planejamento / Arquitetura / Conceito

### 1.1. Arquitetura e Conceito
O projeto consiste em um pipeline de ETL para qualificação de leads recebidos em arquivos de texto/CSV, utilizando Inteligência Artificial (OpenAI via Spring AI) para enriquecimento de dados e persistindo os resultados no PostgreSQL.
* **Origem dos Dados (Fase Inicial):** O processamento é acionado localmente a partir de um diretório fixo.
* **Comportamento do Pipeline:** O pipeline processa **apenas 1 arquivo CSV** por execução. Após a leitura e conclusão do processamento desse único arquivo, o processo de ETL é finalizado (parado) para fins de teste e economia de tokens.

### 1.2. Prompt de Contextualização
> "Aja como um Arquiteto de Software e Desenvolvedor Java Sênior. 
> Analise a estrutura atual do projeto `AiDriveEtl` e leia o arquivo `arquitetura.md` (na raiz) para entender o fluxo de dados local (leitura de um único arquivo de uma pasta local fixa por execução).
> Forneça um breve resumo arquitetural do projeto descrevendo como as camadas devem interagir, identificando possíveis gargalos no fluxo de ETL e propondo boas práticas de design."

---

## Camada 2: Validação dos Campos

### 2.1. Arquitetura e Conceito
Antes de extrair qualquer dado, o arquivo CSV no diretório deve ser validado:
- **Verificação de Arquivo Vazio:** Se o arquivo estiver vazio ou sem linhas de dados, o sistema interrompe o processamento imediatamente.
- **Validação de Formato:** Validar se a extensão é `.csv` e se a estrutura do cabeçalho é condizente com o esperado (`id`, `inputUser`).
- **Tratamento de Erros:** Retornar códigos REST adequados de falha na validação, como `400 Bad Request` ou `404 Not Found` (caso o arquivo não exista).

### 2.2. Prompt de Validação
> "Implemente as validações necessárias para a leitura do arquivo CSV.
> 1. Verifique se o arquivo no diretório fixo está vazio. Se estiver, retorne um status HTTP adequado (como `400 Bad Request` ou `404 Not Found`) com uma mensagem de erro estruturada.
> 2. Valide se o arquivo tem extensão `.csv`.
> 3. Crie testes unitários para validar este comportamento (cenário de sucesso com CSV válido, cenário de falha com arquivo vazio e extensão inválida)."

---

## Camada 3: Extract (Extração)

### 3.1. Arquitetura e Conceito
Após a validação, o arquivo CSV é parseado e depois arquivado:
- **Colunas Esperadas:** A extração espera as colunas `id` (ID da conversa/chat; se nulo, o JPA criará automaticamente via UUID no banco de dados) e `inputUser` (mensagem recebida).
- **Leitura do CSV:** A extração utiliza a biblioteca OpenCSV para processar o arquivo linha por linha localmente.
- **Estruturação por Roles:** Os registros do CSV são convertidos em DTOs contendo o ID e a mensagem.
  - *Exemplo de formato estruturado:* `id: UUID (ou nulo); inputUser: 'x ap, esstá disponivel?;'`
  - *Referência de testes:* O arquivo `docs/exemplo.csv` contém um modelo desse leiaute.
- **Movimentação do Arquivo:** Após a conclusão bem-sucedida do processamento, o arquivo CSV original deve ser movido para uma pasta chamada `/processados` para evitar reprocessamento.

### 3.2. Prompt de Extração
> "Desenvolva o `CsvService` (ou classe equivalente) responsável por realizar o parser (Extração) dos dados do arquivo CSV usando a biblioteca OpenCSV.
> Após a leitura, formate cada linha em objetos de transporte específicos (DTOs). Os dados devem ser mapeados estruturando o identificador da conversa (`id`) e a mensagem (`inputUser`).
> O `id` pode vir em branco no CSV e, nesse caso, deve ser tratado como nulo para que o JPA o gere como UUID no banco.
> Após o processamento bem-sucedido, o arquivo original deve ser movido para uma pasta chamada `/processados` localizada no diretório fixo."

---

## Camada 4: Transform (Transformação / Spring AI)

### 4.1. Arquitetura e Conceito
Os dados estruturados de cada lead são encaminhados para processamento cognitivo:
- **Integração Spring AI:** Uso do starter do Spring AI com o modelo da OpenAI para interpretar as mensagens de texto livre dos leads.
- **Regras de Qualificação (Temperatura do Lead):** Injetadas via System Prompt para a IA com base nos seguintes critérios:
  - **HOT (Quente):** Contatos que possuem uma intenção de compra clara. São aqueles que solicitaram mais informações, demonstrando interesse claro e direto de aquisição. Devem ser mapeados para o enum `LeadScore.HOT`.
  - **WARM (Morno):** Leads que mostram interesse em materiais que sua empresa disponibiliza ou tiram dúvidas gerais, mas ainda não estão claras suas reais intenções de compra. Devem ser mapeados para o enum `LeadScore.WARM`.
  - **COLD (Frio):** Leads que não estão engajados o suficiente com sua empresa, interagindo de forma muito superficial, e que devem ser desconsiderados pela equipe de vendas. Devem ser mapeados para o enum `LeadScore.COLD`.
  - **INVALID (Inválido):** Mensagens de spam, publicidade, links suspeitos ou mensagens totalmente fora de contexto. Devem ser mapeados para o enum `LeadScore.INVALID`.
- **Extração Semântica:** A IA deve preencher apenas: `tipo_imovel`, `orcamento_estimado`, `condicoes_especiais` e `temperatura_lead`.

### 4.2. Prompt de Transformação
> "Configure o Spring AI na aplicação usando o modelo da OpenAI.
> Crie a lógica no Service que recebe as mensagens do CSV (`inputUser`) e as envia para o modelo da OpenAI.
> O prompt da IA deve instruí-la, utilizando as seguintes regras de qualificação de leads, a ler a mensagem e extrair de forma estruturada (usando `BeanOutputConverter`) os atributos mapeáveis:
> 
> Regras de Qualificação:
> - **HOT:** Contatos com intenção de compra clara (ex: solicitou mais informações específicas de venda, demonstrou urgência ou interesse direto de compra).
> - **WARM:** Leads com interesse, mas sem intenção de compra clara ainda (ex: dúvidas gerais ou estágio inicial de pesquisa).
> - **COLD:** Leads pouco engajados com interações superficiais ou pontuais que devem ser desconsiderados.
> - **INVALID:** Mensagens de spam, publicidade ou fora de contexto.
> 
> Atributos a extrair:
> - `tipo_imovel` (ex: Casa, Apartamento, Terreno, Comercial)
> - `orcamento_estimado` (ex: R$ 500.000,00)
> - `condicoes_especiais` (ex: Aceita financiamento, Permuta)
> - `temperatura_lead` (mapeada para o enum `LeadScore`: HOT, WARM, COLD, INVALID)"

---

## Camada 5: Load (Persistência / PostgreSQL e Resposta HTTP)

### 5.1. Arquitetura e Conceito
Conclusão do pipeline ETL:
- **Persistência Relacional:** Montagem completa da `LeadEntity` contendo o ID (UUID gerado se nulo), os dados originais do CSV, a data do processamento e os campos qualificados pela OpenAI, persistindo-os na tabela do PostgreSQL.
- **Resposta HTTPS (200 OK):** Ao final do processamento síncrono e com sucesso, o endpoint deve retornar uma resposta HTTPS com o status HTTP `200 OK`.
- **Corpo do Retorno:** Deve ser apresentado um JSON com um resumo do processamento contendo o ID gerado, a mensagem original e as informações tratadas do `role`/lead.

### 5.2. Prompt de Persistência e Resposta
> "Implemente a fase de Load (carregamento) e finalização do ciclo de vida da requisição no Controller e Service de Leads:
> 1. Receba os dados transformados pela IA, crie a instância de `LeadEntity` com o ID UUID gerado pelo JPA se vier nulo no CSV, os dados originais, a data de processamento (`processado_em`), e salve-os na tabela correspondente do PostgreSQL usando o JPA.
> 2. Retorne uma resposta síncrona via protocolo seguro HTTPS contendo o status HTTP `200 OK`.
> 3. No corpo da resposta, inclua um JSON com o resumo do processamento, listando o ID gerado e as informações básicas da 'role' tratada (ex: `{ "conversaId": "uuid", "status": "Processado com sucesso", "temperatura": "HOT" }`)."
