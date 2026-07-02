---
config:
  layout: elk
---
graph TD
    subgraph cliente["👤 Cliente / Sistema Origem"]
        A["Envia POST /api/upload com CSV"]
    end

    subgraph apiCore["⚙️ API Core (Spring Boot)"]
        B("Controller: Recebe o payload")
        C{"CSV é válido?"}
        D["Extract: Lê CSV em Memória/Lote"]
        
        subgraph motor["🧠 Motor de Transformação (Spring AI)"]
            E["Prepara o Prompt por Linha"]
            F["ChatClient: Dispara para a LLM"]
            H["Output Parser: Mapeia JSON para Record/Entity"]
        end
        
        I["Load: Prepara a Entidade Enriquecida"]
    end

    subgraph infra["☁️ Infra & IA Externa"]
        G(("Antigravity / LLM"))
        J[("PostgreSQL")]
    end

    A --> B
    B --> C
    C -->|Não| Z["Retorna 400 Bad Request"]
    C -->|Sim| D
    D -->|Para cada linha/lote| E
    E --> F
    F -->|Request HTTP| G
    G -->|Response JSON| H
    H --> I
    I -->|Spring Data JPA| J
    J --> K["Controller: Retorna 200 OK - Resumo da Carga"]
    
    classDef clientGroup fill:#f0f9ff,stroke:#38bdf8
    classDef apiGroup fill:#eef2ff,stroke:#818cf8
    classDef motorGroup fill:#f5f3ff,stroke:#a78bfa
    classDef infraGroup fill:#ecfeff,stroke:#22d3ee
    classDef process fill:#f0fdf4,stroke:#4ade80
    classDef decision fill:#fff7ed,stroke:#fb923c
    classDef error fill:#fef2f2,stroke:#f87171
    classDef success fill:#f0fdf4,stroke:#4ade80
    
    class cliente clientGroup
    class apiCore apiGroup
    class motor motorGroup
    class infra infraGroup
    class A,B,D,E,F,H,I process
    class C decision
    class Z error
    class K success