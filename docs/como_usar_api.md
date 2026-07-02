# Como Usar a API - AiDriveEtl

Este guia demonstra como preparar o ambiente, testar a API e verificar os resultados do processamento de qualificação de leads via IA.

## 1. Preparação do Ambiente

### 1.1. Subindo o Banco de Dados
A aplicação depende do PostgreSQL para salvar os leads qualificados. Use o Docker Compose incluído no projeto para iniciá-lo:
```bash
docker compose up -d
```
*(O banco ficará acessível em `localhost:5433` usando o usuário/senha: `postgres`)*

### 1.2. Configurando a Chave da OpenAI
O Spring AI precisa de uma chave válida da OpenAI. Exporte-a no seu terminal antes de rodar a aplicação:
```bash
export OPENAI_API_KEY=sua-chave-aqui
```

### 1.3. Iniciando a Aplicação
Inicie a aplicação Spring Boot pelo terminal:
```bash
./mvnw spring-boot:run
```
*(A aplicação subirá na porta `8080`)*

---

## 2. Preparando os Dados (O CSV)

O pipeline ETL está configurado para ler arquivos localmente de um diretório fixo e processar **1 arquivo por execução**.

1. Crie um arquivo com a extensão `.csv` (ex: `leads.csv`).
2. Adicione os cabeçalhos `id` e `inputUser`.
3. Coloque esse arquivo no diretório: `data/input/`.

**Exemplo de CSV (`data/input/leads.csv`):**
```csv
id,inputUser
,Gostaria de saber se a casa no bairro verde está disponível. Aceito pagar à vista!
3a8e9d30-b18c-4f8e-8a18-d7e7d6cf628a,Estou apenas pesquisando por enquanto.
```

---

## 3. Disparando o Pipeline (Endpoint)

### `POST /api/leads/process`
Este endpoint aciona a leitura do diretório, a transformação na OpenAI e a persistência no banco. Não é necessário enviar nenhum corpo de requisição (Body), pois a origem dos dados é a pasta local.

**Exemplo via cURL:**
```bash
curl -X POST http://localhost:8080/api/leads/process
```

### 3.1. Cenário de Sucesso (200 OK)
Se o pipeline rodar com sucesso, ele retornará os dados do último lead processado.

```json
{
  "conversaId": "4f8c9d31-b18c-4f8e-8a18-d7e7d6cf628a",
  "mensagemOriginal": "Gostaria de saber se a casa no bairro verde está disponível. Aceito pagar à vista!",
  "temperaturaLead": "HOT",
  "status": "Processado com sucesso"
}
```
**O que aconteceu em background?**
1. O CSV original foi movido para a pasta `data/processados/` (para não ser lido de novo).
2. O banco de dados PostgreSQL foi preenchido com a entidade contendo as colunas: `tipo_imovel`, `orcamento_estimado`, `condicoes_especiais` e a qualificação da temperatura (`HOT`, `WARM`, `COLD`, `INVALID`).

---

## 4. Tratamento de Erros e Exceções

A API está programada para retornar mensagens amigáveis em caso de falhas na validação local.

### 4.1. Diretório Vazio (404 Not Found)
Se você chamar o endpoint e não houver nenhum arquivo `.csv` na pasta `data/input/`:
```json
{
  "timestamp": "2026-07-02T13:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Nenhum arquivo CSV encontrado no diretório de entrada: ..."
}
```

### 4.2. CSV Vazio ou Inválido (400 Bad Request)
Se o arquivo estiver em branco ou o cabeçalho não contiver as colunas `id` e `inputUser`:
```json
{
  "timestamp": "2026-07-02T13:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Cabeçalho inválido. Esperado: [id, inputUser] | Encontrado: [...]"
}
```

---

## 5. Como Verificar no Banco de Dados

Você pode verificar os resultados salvos diretamente no PostgreSQL. Use o DBeaver, pgAdmin ou o próprio terminal Docker:

```bash
# Entrando no container do PostgreSQL
docker exec -it aidrivedb psql -U postgres -d aidrivedb

# Consultando a tabela de leads
SELECT id, tipo_imovel, temperatura_lead, condicoes_especiais FROM tb_leads_qualificados;
```
