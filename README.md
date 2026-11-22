## Bootcamp CI&T - Java AI Copilot.

<img width="120" height="120" alt="1000136548" src="https://github.com/user-attachments/assets/21424bbb-9cad-455d-ad9a-b916433a2487" />

---

## DESCRIÇÃO:

- Desenvolver uma API Inteligente com Spring Boot e Spring AI, capaz de interpretar comandos de voz, transformar áudio em texto, acionar funções reais da aplicação e gerar respostas em áudio para a pessoa usuária. 

- Neste projeto, você vai evoluir uma aplicação de orçamento financeiro que integra IA Generativa a uma arquitetura Java bem organizada, explorando conceitos como ChatClient, Tool Calling, transcrição de áudio, síntese de voz, persistência de dados e exposição de endpoints REST. 

-  Ao final, você terá uma solução prática que demonstra como conectar modelos de IA a fluxos reais de negócio, mantendo boas práticas de desenvolvimento, separação de responsabilidades e uma base sólida para novas evoluções.

---


# API Inteligente de Orçamento com Spring Boot

API REST de controle financeiro pessoal que recebe despesas por JSON ou por voz, interpreta pedidos em português com Spring AI, executa operações reais de consulta e gravação por *tool calling* e pode responder em áudio.

## Problema e objetivo

Registrar uma despesa falada costuma exigir que a pessoa abra uma planilha ou formulário, identifique os campos e digite cada informação. Este projeto demonstra um fluxo conversacional que reduz esse atrito sem deixar a IA gravar dados por conta própria: o modelo identifica a intenção e invoca ferramentas Java explícitas, que passam pelos casos de uso e pelo repositório da aplicação.

O repositório é um projeto educacional de portfólio. Não há métricas de produção publicadas; os indicadores adequados para uma validação futura são taxa de transcrições compreendidas, precisão de categoria/valor, taxa de correções manuais, latência e custo por interação.

## Contexto, baseline e premissas

O cenário tratado é o registro de despesas pessoais. A pessoa pode enviar um JSON estruturado, uma frase digitada ou um áudio; a aplicação persiste a transação e, no fluxo de voz, devolve uma confirmação falada.

O baseline funcional é o lançamento manual por formulário ou planilha: a pessoa informa descrição, valor e categoria diretamente. Esse baseline é qualitativo. O projeto não mediu tempo de preenchimento, volume de lançamentos nem redução de erros, então não atribui ganhos numéricos à automação.

Premissas usadas na implementação:

- Valores são registrados em reais, representados internamente por centavos inteiros (`amountCents`).
- Cada transação representa uma despesa positiva e pertence a uma categoria do enum `Category`.
- A frase falada expressa a intenção da pessoa; o modelo pode sugerir os campos, mas somente as ferramentas Java autorizadas alteram dados.
- O assistente deve pedir esclarecimentos quando faltarem informações essenciais e não deve afirmar que salvou uma despesa antes de a ferramenta retornar sucesso.
- O protótipo ainda não separa dados por usuário e, portanto, não serve para uso público com dados financeiros reais.

## Planejamento da solução

O fluxo foi organizado em quatro partes: entrada HTTP; transcrição e interpretação com Spring AI; execução de casos de uso por ferramentas explícitas; persistência relacional e resposta em texto ou áudio. O contrato de domínio independe do JPA e do provedor de IA para que essas integrações possam evoluir sem mover as regras financeiras para o controller.

## Como funciona

```text
Pessoa usuária
  ├─ JSON ──────────────────────┐
  ├─ Texto ─────────────────────┼─> API REST / ChatClient
  └─ Áudio -> Transcrição ──────┘          │
                                             ├─> Ferramentas Java
                                             │      └─> Casos de uso
                                             │             └─> H2 ou MySQL
                                             └─> Resposta em texto
                                                    └─> TTS -> MP3 (áudio)
```

## Stack e decisões técnicas

- Java 27, Spring Boot 4.2 e Spring AI 2.1: versões alinhadas para a configuração do projeto.
- Gradle: dependências e build declarados em `build.gradle`.
- Spring MVC, Bean Validation e Spring Data JPA: API síncrona, validação nas bordas e persistência relacional.
- H2 em memória por padrão para inicialização rápida; MySQL 8.4 via Docker Compose para desenvolvimento com banco persistente.
- OpenAI via Spring AI para chat, transcrição e voz. As abstrações de modelos ficam no controlador/orquestrador; regras financeiras e persistência não dependem de SDKs do provedor.
- `amountCents` usa inteiro longo: R$ 60,00 é `6000`. Isso evita erros de ponto flutuante em valores monetários.
- Domínio, aplicação, API e infraestrutura de persistência ficam em pacotes separados. O domínio não importa JPA nem Spring AI.

As alternativas foram escolhidas pelos requisitos deste protótipo: Spring MVC foi preferido a uma stack reativa porque os modelos e a persistência são chamados de forma síncrona; JPA com H2/MySQL atende ao registro relacional simples, enquanto um banco NoSQL acrescentaria decisões sem necessidade atual; Spring AI mantém as integrações de chat, transcrição e voz atrás de interfaces conhecidas no ecossistema Spring. O trade-off é que a chamada síncrona de IA pode aumentar a latência e ocupa a requisição HTTP até o modelo responder.

## Metodologia de projeto aplicada

O roteiro de 11 passos usado em projetos preditivos inclui EDA, *feature scaling*, MAE/RMSE e previsão. Como esta API não tem conjunto de dados analítico nem modelo preditivo, a sequência abaixo adapta o foco em problema, baseline, evidência e resultado ao escopo de engenharia de software:

1. **Problema de negócio:** reduzir o atrito para registrar e consultar despesas.
2. **Baseline:** lançamento manual em formulário ou planilha; sem medição quantitativa disponível.
3. **Planejamento:** definir endpoints, casos de uso, ferramentas do modelo, armazenamento e fluxo de áudio.
4. **Premissas e dados:** centavos inteiros, categorias fechadas e confirmação somente após persistência.
5. **Desenho:** separar API, aplicação, domínio e infraestrutura.
6. **Interpretação:** transcrever fala e usar `ChatClient` para selecionar uma ferramenta permitida.
7. **Persistência:** mapear o domínio para JPA e H2/MySQL.
8. **Avaliação:** ainda pendente; medir compreensão, acerto de campos, latência e falhas com dados de teste definidos.
9. **Desempenho de negócio:** impacto financeiro ainda não calculado; exige custo real do processo manual e resultados observados.
10. **Execução:** iniciar a API localmente com Gradle, H2 ou Docker Compose/MySQL.
11. **Apresentação:** demonstrar o lançamento por texto e voz e explicar limites e próximos passos.

Assim, o método acompanha o que este projeto realmente constrói e mede, sem atribuir a ele análises ou métricas que pertencem a outro tipo de solução.

## Requisitos

- JDK 27.
- Gradle 9.2 ou superior.
- Docker Desktop apenas para o perfil de banco MySQL.
- Uma chave OpenAI (`OPENAI_API_KEY`) para inicializar a integração Spring AI. Os endpoints REST de transações não fazem chamadas ao modelo.

## Executar localmente

Com H2 em memória, a aplicação não precisa de um servidor de banco externo. Configure a chave OpenAI usada pela integração Spring AI:

```powershell
$env:OPENAI_API_KEY = "sua-chave"
gradle bootRun
```

Para usar MySQL, suba o banco e configure as variáveis de conexão antes de iniciar a aplicação:

```powershell
docker compose up -d mysql
$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:OPENAI_API_KEY = "sua-chave"
gradle bootRun
```

Os valores locais de usuário e senha estão no `compose.yaml`; substitua-os fora do ambiente de desenvolvimento. Também é possível copiar `.env.example` como referência e exportar `OPENAI_API_KEY`, `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` no ambiente. Não comite chaves reais.

## Endpoints

### Criar uma transação

`POST /api/v1/transactions` com JSON:

```json
{
  "description": "Estacionamento",
  "amountCents": 6000,
  "category": "AUTO"
}
```

Categorias aceitas: `GROCERIES`, `FARMA`, `AUTO` e `OTHER`.

### Listar e filtrar transações

`GET /api/v1/transactions` lista registros mais recentes primeiro. Use `GET /api/v1/transactions?category=AUTO` para filtrar.

### Conversar por texto

`POST /api/v1/assistant/text` com `{"message":"Paguei R$ 60 de estacionamento hoje"}`.

O assistente recebe ferramentas para registrar uma despesa declarada ou consultar despesas já salvas. Se faltarem dados essenciais, a instrução do sistema pede esclarecimento em vez de inventar um lançamento.

### Conversar por áudio

`POST /api/v1/assistant/voice` recebe `multipart/form-data`, com o arquivo no campo `audio`, transcreve, consulta o assistente e devolve `audio/mpeg`.

```powershell
curl.exe -X POST http://localhost:8080/api/v1/assistant/voice `
  -F "audio=@.\minha-mensagem.wav" `
  --output resposta.mp3
```

Formatos aceitos e limites finais dependem da API do provedor configurado. O limite HTTP desta aplicação é 20 MB por arquivo/requisição.

## Resultados entregues

- Contrato REST para criar e listar transações, com filtro por categoria.
- Fluxo de conversa por texto que pode registrar ou consultar dados usando tool calling.
- Fluxo de voz com transcrição, interpretação e resposta MP3.
- Persistência configurável para H2 local ou MySQL em Docker Compose.
- README com exemplos de uso, decisões técnicas, premissas e limites conhecidos.

Esses são resultados de implementação. Ainda não há avaliação com usuários, dados operacionais ou ambiente publicado, portanto não são apresentadas taxas de acerto, redução de tempo ou economia financeira.

## Desempenho de negócio

O impacto financeiro residual não pode ser calculado com os dados atuais. Para estimá-lo, seria necessário medir o custo médio do registro manual, o custo das chamadas aos modelos, a taxa de correções e o tempo efetivamente economizado. Uma avaliação futura pode comparar esses valores por interação e verificar se o fluxo automatizado reduz o custo sem aumentar lançamentos incorretos.

## Estrutura do código

```text
src/main/java/br/com/ciandt/budget/
  api/                         # Controllers, DTOs e erros HTTP
  application/                 # Casos de uso e ferramentas do modelo
  domain/                      # Transaction, Category e porta de repositório
  infrastructure/persistence/ # Entidade JPA, Spring Data e adaptador
src/main/resources/
  application.properties       # H2 e configuração comum
  application-mysql.properties # Perfil MySQL
compose.yaml                   # Banco MySQL de desenvolvimento
```


## Testes Automatizados

A suíte de testes foi estruturada com **JUnit 5** (`useJUnitPlatform()`)[span_1](start_span)[span_1](end_span) para validar as camadas centrais da aplicação sem depender de rede externa ou banco de dados de produção[span_2](start_span)[span_2](end_span):

- **Domínio (`src/test/java/br/com/ciandt/budget/domain/TransactionTest.java`):** valida a criação normalizada via `Transaction.create(...)` (geração de `UUID`, `trim` na `description`, `amountCents` e `occurredAt`) e o bloqueio de valores inválidos (`description` em branco ou `amountCents <= 0`)[span_3](start_span)[span_3](end_span).
- **Casos de uso (`src/test/java/br/com/ciandt/budget/application/CreateTransactionUseCaseTest.java` e `ListTransactionsUseCaseTest.java`):** verificam o registro de despesas, a não persistência quando a validação de domínio falha e a listagem completa ou filtrada por `Category` (`GROCERIES`, `FARMA`, `AUTO`, `OTHER`)[span_4](start_span)[span_4](end_span)[span_5](start_span)[span_5](end_span)[span_6](start_span)[span_6](end_span)[span_7](start_span)[span_7](end_span)[span_8](start_span)[span_8](end_span).
- **Ferramentas do assistente (`src/test/java/br/com/ciandt/budget/application/TransactionToolsTest.java`):** testam `registerExpense` (incluindo fallback para `Category.OTHER` quando a categoria é nula) e `listExpenses` expostas ao modelo via `@Tool`[span_9](start_span)[span_9](end_span)[span_10](start_span)[span_10](end_span)[span_11](start_span)[span_11](end_span)[span_12](start_span)[span_12](end_span)[span_13](start_span)[span_13](end_span).
- **Camada HTTP (`src/test/java/br/com/ciandt/budget/api/TransactionControllerTest.java` e `AssistantControllerTest.java`):** validam `POST /api/v1/transactions`, `GET /api/v1/transactions` (com filtro por categoria), `POST /api/v1/assistant/text`, `POST /api/v1/assistant/voice` (upload `multipart/form-data` retornando `audio/mpeg`) e o tratamento de erros em `ApiExceptionHandler`[span_14](start_span)[span_14](end_span)[span_15](start_span)[span_15](end_span)[span_16](start_span)[span_16](end_span).
- **Persistência JPA com H2 (`src/test/java/br/com/ciandt/budget/infrastructure/persistence/TransactionRepositoryAdapterTest.java` e `src/test/resources/application-test.properties`):** testam `save`, `findById`, `findAll` e `findByCategory` com ordenação decrescente por `occurredAt` sobre banco H2 em memória[span_17](start_span)[span_17](end_span)[span_18](start_span)[span_18](end_span)[span_19](start_span)[span_19](end_span)[span_20](start_span)[span_20](end_span).

### Como executar os testes


## Testes Automatizados

A suíte de testes foi estruturada com **JUnit 5** (`useJUnitPlatform()`)[span_1](start_span)[span_1](end_span) para validar as camadas centrais da aplicação sem depender de rede externa ou banco de dados de produção[span_2](start_span)[span_2](end_span):

- **Domínio (`src/test/java/br/com/ciandt/budget/domain/TransactionTest.java`):** valida a criação normalizada via `Transaction.create(...)` (geração de `UUID`, `trim` na `description`, `amountCents` e `occurredAt`) e o bloqueio de valores inválidos (`description` em branco ou `amountCents <= 0`)[span_3](start_span)[span_3](end_span).
- **Casos de uso (`src/test/java/br/com/ciandt/budget/application/CreateTransactionUseCaseTest.java` e `ListTransactionsUseCaseTest.java`):** verificam o registro de despesas, a não persistência quando a validação de domínio falha e a listagem completa ou filtrada por `Category` (`GROCERIES`, `FARMA`, `AUTO`, `OTHER`)[span_4](start_span)[span_4](end_span)[span_5](start_span)[span_5](end_span)[span_6](start_span)[span_6](end_span)[span_7](start_span)[span_7](end_span)[span_8](start_span)[span_8](end_span).
- **Ferramentas do assistente (`src/test/java/br/com/ciandt/budget/application/TransactionToolsTest.java`):** testam `registerExpense` (incluindo fallback para `Category.OTHER` quando a categoria é nula) e `listExpenses` expostas ao modelo via `@Tool`[span_9](start_span)[span_9](end_span)[span_10](start_span)[span_10](end_span)[span_11](start_span)[span_11](end_span)[span_12](start_span)[span_12](end_span)[span_13](start_span)[span_13](end_span).
- **Camada HTTP (`src/test/java/br/com/ciandt/budget/api/TransactionControllerTest.java` e `AssistantControllerTest.java`):** validam `POST /api/v1/transactions`, `GET /api/v1/transactions` (com filtro por categoria), `POST /api/v1/assistant/text`, `POST /api/v1/assistant/voice` (upload `multipart/form-data` retornando `audio/mpeg`) e o tratamento de erros em `ApiExceptionHandler`[span_14](start_span)[span_14](end_span)[span_15](start_span)[span_15](end_span)[span_16](start_span)[span_16](end_span).
- **Persistência JPA com H2 (`src/test/java/br/com/ciandt/budget/infrastructure/persistence/TransactionRepositoryAdapterTest.java` e `src/test/resources/application-test.properties`):** testam `save`, `findById`, `findAll` e `findByCategory` com ordenação decrescente por `occurredAt` sobre banco H2 em memória[span_17](start_span)[span_17](end_span)[span_18](start_span)[span_18](end_span)[span_19](start_span)[span_19](end_span)[span_20](start_span)[span_20](end_span).





## Boas práticas e limites atuais

- A IA só consegue executar as operações expostas em `TransactionTools`; não recebe acesso direto ao banco.
- A API não implementa autenticação nem autorização. Não a exponha publicamente sem controles de identidade e isolamento de dados por pessoa.
- Áudio e transcrição podem conter informações financeiras pessoais. O projeto não arquiva o arquivo recebido; o provedor de IA processa os dados enviados conforme a configuração/termos da conta.
- `ddl-auto=update` serve para desenvolvimento. Evolua o schema com migrações versionadas antes de produção.
- Valores de saída da ferramenta são retornados ao modelo para compor uma resposta. Revise limites, tratamento de repetição/idempotência e confirmação de intenção antes de uso financeiro real.
- Auditoria de áudio, autenticação, limites por usuário, observabilidade e migrações são caminhos de evolução, não funcionalidades já implementadas.



---

## Testes Automatizados

A suíte de testes foi estruturada com **JUnit 5** (`useJUnitPlatform()`)[span_1](start_span)[span_1](end_span) para validar as camadas centrais da aplicação sem depender de rede externa ou banco de dados de produção[span_2](start_span)[span_2](end_span):

- **Domínio (`src/test/java/br/com/ciandt/budget/domain/TransactionTest.java`):** valida a criação normalizada via `Transaction.create(...)` (geração de `UUID`, `trim` na `description`, `amountCents` e `occurredAt`) e o bloqueio de valores inválidos (`description` em branco ou `amountCents <= 0`)[span_3](start_span)[span_3](end_span).
- **Casos de uso (`src/test/java/br/com/ciandt/budget/application/CreateTransactionUseCaseTest.java` e `ListTransactionsUseCaseTest.java`):** verificam o registro de despesas, a não persistência quando a validação de domínio falha e a listagem completa ou filtrada por `Category` (`GROCERIES`, `FARMA`, `AUTO`, `OTHER`)[span_4](start_span)[span_4](end_span)[span_5](start_span)[span_5](end_span)[span_6](start_span)[span_6](end_span)[span_7](start_span)[span_7](end_span)[span_8](start_span)[span_8](end_span).
- **Ferramentas do assistente (`src/test/java/br/com/ciandt/budget/application/TransactionToolsTest.java`):** testam `registerExpense` (incluindo fallback para `Category.OTHER` quando a categoria é nula) e `listExpenses` expostas ao modelo via `@Tool`[span_9](start_span)[span_9](end_span)[span_10](start_span)[span_10](end_span)[span_11](start_span)[span_11](end_span)[span_12](start_span)[span_12](end_span)[span_13](start_span)[span_13](end_span).
- **Camada HTTP (`src/test/java/br/com/ciandt/budget/api/TransactionControllerTest.java` e `AssistantControllerTest.java`):** validam `POST /api/v1/transactions`, `GET /api/v1/transactions` (com filtro por categoria), `POST /api/v1/assistant/text`, `POST /api/v1/assistant/voice` (upload `multipart/form-data` retornando `audio/mpeg`) e o tratamento de erros em `ApiExceptionHandler`[span_14](start_span)[span_14](end_span)[span_15](start_span)[span_15](end_span)[span_16](start_span)[span_16](end_span).
- **Persistência JPA com H2 (`src/test/java/br/com/ciandt/budget/infrastructure/persistence/TransactionRepositoryAdapterTest.java` e `src/test/resources/application-test.properties`):** testam `save`, `findById`, `findAll` e `findByCategory` com ordenação decrescente por `occurredAt` sobre banco H2 em memória[span_17](start_span)[span_17](end_span)[span_18](start_span)[span_18](end_span)[span_19](start_span)[span_19](end_span)[span_20](start_span)[span_20](end_span).

### Como executar os testes

Na raiz do repositório, execute:

```bash
./gradlew test
```




---




## Aprendizados técnicos

O projeto exercita a separação entre regras de negócio, integração de IA e persistência: o modelo escolhe entre ferramentas restritas, mas os casos de uso validam e gravam transações; o domínio representa dinheiro sem ponto flutuante; e a API pode trocar H2 por MySQL sem alterar o contrato do domínio. Uma evolução importante é validar o fluxo com testes e interações reais antes de tomar decisões de produção, especialmente por envolver dados financeiros e chamadas não determinísticas a modelos.



## Próximos passos

1. Adicionar identidade de usuário e associar cada transação ao seu proprietário.
2. Criar migrações com Flyway ou Liquibase e regras de retenção para dados sensíveis.
3. Guardar metadados de auditoria de cada interação com consentimento e política de retenção definida.
4. Instrumentar latência, falhas de transcrição e precisão das categorias sem registrar áudio ou dados financeiros em logs.
5. Criar cobertura de testes para domínio, casos de uso, API e integração de persistência.


---

**Autor:** Sérgio Santos — Cientista de Dados | Ambientes Críticos e Governança de Dados

[![Portfólio Sérgio Santos](https://img.shields.io/badge/Portfólio-Sérgio_Santos-111827?style=for-the-badge&logo=githubpages&logoColor=00eaff)](https://portfoliosantossergio.vercel.app)
[![LinkedIn Sérgio Santos](https://img.shields.io/badge/LinkedIn-Sérgio_Santos-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/santossergioluiz)

