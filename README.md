# 🎙️ API Inteligente de Orçamento com Spring Boot e Spring AI

> **Bootcamp CI&T - Java AI Copilot** · Java 27 · Spring Boot 4.2 · Spring AI 2.1 · Gradle · H2 / MySQL 8.4

<img width="120" height="120" alt="Bootcamp CI&T - Java AI Copilot" src="https://github.com/user-attachments/assets/21424bbb-9cad-455d-ad9a-b916433a2487" />

---

## 1. Visão geral e problema de negócio

Registrar uma despesa no dia a dia exige abrir uma planilha ou formulário, identificar os campos e digitar cada informação. Quando a pessoa está em movimento, esse atrito vira esquecimento ou desistência.

Este projeto é uma **API REST de controle financeiro pessoal** que recebe despesas por **JSON, texto ou áudio** em português do Brasil. A IA transcreve a fala, identifica a intenção e aciona **ferramentas Java explícitas** (`@Tool`). Quem valida e grava os dados é a aplicação, nunca o modelo. A resposta volta em texto ou em áudio MP3.

---

## 2. Objetivo do projeto

Construí este projeto para praticar a integração entre **IA Generativa e uma arquitetura Java em camadas** no ecossistema Spring. Os objetivos foram:

- Receber entradas por JSON, texto ou áudio (`multipart/form-data`).
- Usar `ChatClient` e *tool calling* para que o modelo acione somente operações autorizadas: `registerExpense` e `listExpenses`.
- Manter o domínio independente de JPA e do provedor de IA.
- Persistir em **H2** (início rápido) ou **MySQL 8.4** via Docker Compose.

> Projeto educacional de portfólio. Não há uso em produção nem métricas de produção publicadas.

---

## 3. Contexto, baseline e premissas

### Contexto
O cenário é o registro de despesas pessoais. A pessoa envia um JSON, uma frase digitada ou um áudio; a aplicação persiste a transação e, no fluxo de voz, devolve uma confirmação falada.

### Baseline
O ponto de comparação é o **lançamento manual** em formulário ou planilha: a pessoa informa descrição, valor e categoria diretamente. Esse baseline é **qualitativo**. O projeto não mediu tempo de preenchimento, volume de lançamentos nem redução de erros, e por isso não atribui ganhos numéricos à automação.

### Premissas da implementação
- **Dinheiro em centavos inteiros:** `long amountCents`. R$ 60,00 é `6000`, o que evita erros de ponto flutuante.
- **Despesa sempre positiva:** `Transaction` rejeita descrição vazia e `amountCents <= 0`.
- **Categorias fechadas:** enum `Category` com `GROCERIES`, `FARMA`, `AUTO` e `OTHER`. Se a ferramenta receber categoria nula, assume `OTHER`.
- **Sem escrita mágica pelo modelo:** o LLM sugere parâmetros; só as ferramentas Java gravam ou consultam. O *system prompt* manda pedir esclarecimento quando faltar informação essencial e não confirmar um registro antes de a ferramenta retornar sucesso.
- **Protótipo sem multiusuário:** não há autenticação nem isolamento por pessoa. Não use com dados financeiros reais.

---

## 4. Decisões técnicas e trade-offs

| Decisão | Escolha | Alternativa considerada | Justificativa e trade-off |
| :--- | :--- | :--- | :--- |
| Linguagem e framework | Java 27, Spring Boot 4.2, Spring AI 2.1, Gradle | Maven ou versões anteriores | Alinhado ao `build.gradle`. Usa `record` para um domínio imutável. Exige JDK 27. |
| Estilo da API | Spring MVC (síncrono) + Bean Validation | Stack reativa (WebFlux) | Modelo e banco são chamados de forma síncrona neste escopo. O custo é que a chamada à IA ocupa a requisição HTTP até o modelo responder, aumentando a latência. |
| Persistência | Spring Data JPA com H2 (padrão) e MySQL 8.4 | Banco NoSQL | O modelo relacional simples de transações não precisa de NoSQL. O domínio expõe a porta `TransactionRepository`, implementada pelo `TransactionRepositoryAdapter`. |
| IA e áudio | Spring AI (`ChatClient`, `@Tool`, `TranscriptionModel`, `TextToSpeechModel`) com OpenAI | SDK direto do provedor ou STT/TTS no cliente | Mantém chat, transcrição e voz atrás de abstrações do ecossistema Spring e as regras financeiras fora do controller. O custo é a dependência de chave, rede e limites do provedor. |
| Modelo de dados | `TransactionEntity` (JPA) separada do `Transaction` (domínio) | Anotar o próprio domínio com JPA | O domínio não importa JPA nem Spring AI. O custo é o mapeamento extra entre as duas classes. |

---

## 5. Estratégia da solução e arquitetura

O fluxo tem quatro partes: entrada HTTP, transcrição e interpretação com Spring AI, execução de casos de uso por ferramentas explícitas, e persistência com resposta em texto ou áudio.

```text
Pessoa usuária
  ├─ JSON ──────────────────────┐
  ├─ Texto ─────────────────────┼─> API REST / ChatClient
  └─ Áudio -> Transcrição ──────┘          │
                                           ├─> TransactionTools (@Tool)
                                           │     └─> CreateTransactionUseCase / ListTransactionsUseCase
                                           │           └─> TransactionRepositoryAdapter -> H2 ou MySQL 8.4
                                           └─> Resposta em texto
                                                 └─> TTS -> MP3 (áudio)
```

### Estrutura do código

```text
src/main/java/br/com/ciandt/budget/
  api/                          # Controllers, DTOs e ApiExceptionHandler
  application/                  # Casos de uso e TransactionTools (@Tool)
  domain/                       # Transaction, Category e porta TransactionRepository
  infrastructure/persistence/   # TransactionEntity, SpringDataTransactionRepository e adaptador
src/main/resources/
  application.properties        # H2 e configuração comum
  application-mysql.properties  # Perfil MySQL
src/test/java/...               # Testes de domínio, casos de uso, ferramentas, API e persistência
compose.yaml                    # MySQL 8.4 para desenvolvimento (porta 3307)
```

---

## 6. Metodologia aplicada

O roteiro de 11 passos de projetos preditivos inclui EDA, *scaling* e MAE/RMSE. Esta API não tem dataset analítico nem modelo preditivo, então adaptei a sequência mantendo o foco em **problema, baseline, evidência e resultado**:

1. **Problema de negócio:** reduzir o atrito para registrar e consultar despesas.
2. **Baseline:** lançamento manual em formulário ou planilha, sem medição quantitativa.
3. **Planejamento:** definir endpoints, casos de uso, ferramentas do modelo, persistência e fluxo de áudio.
4. **Premissas e dados:** centavos inteiros, categorias fechadas e confirmação somente após persistência.
5. **Desenho:** separar `api`, `application`, `domain` e `infrastructure/persistence`.
6. **Interpretação:** transcrever a fala e usar `ChatClient` para selecionar uma ferramenta permitida.
7. **Persistência:** mapear o domínio para JPA com H2 e MySQL.
8. **Avaliação:** os 21 testes automatizados cobrem regras de domínio, casos de uso, ferramentas, contrato REST e persistência. A avaliação do comportamento da IA (compreensão de comandos, acerto de campos, latência) **ainda está pendente**.
9. **Desempenho de negócio:** impacto financeiro ainda não calculado (veja a seção 9).
10. **Execução:** rodar localmente com Gradle, H2 ou Docker Compose + MySQL.
11. **Apresentação:** demonstrar lançamento por texto e por voz, com limites e próximos passos.

---

## 7. Tecnologias utilizadas

- **Java 27** (`java.toolchain.languageVersion = 27`)
- **Spring Boot 4.2** (`webmvc`, `data-jpa`, `validation`, `test`)
- **Spring AI 2.1** (`spring-ai-bom` e `spring-ai-starter-model-openai`)
- **Gradle 9.8.0** via wrapper (`gradlew` / `gradlew.bat`)
- **H2** em memória e **MySQL 8.4** via Docker Compose
- **JUnit 5**, Mockito e MockMvc nos testes

---

## 8. Como executar e testar

### Pré-requisitos
- JDK 27.
- Nada além do wrapper incluído no repositório (`./gradlew`), sem Gradle instalado.
- Docker apenas para o perfil MySQL.
- `OPENAI_API_KEY` para inicializar a integração Spring AI. Os endpoints REST de transações não chamam o modelo.

### Variáveis de ambiente
Use o `.env.example` como referência (`OPENAI_API_KEY`, `OPENAI_CHAT_MODEL`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`). **Nunca comite chaves reais.**

### Com H2 (padrão, sem banco externo)

```powershell
$env:OPENAI_API_KEY = "sua-chave"
./gradlew bootRun
```

### Com MySQL 8.4 via Docker Compose

```powershell
docker compose up -d mysql
$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:OPENAI_API_KEY = "sua-chave"
./gradlew bootRun
```

O MySQL sobe em `localhost:3307`, com database `budget`, usuário `budget` e senha `budget`. Esses valores servem **apenas para desenvolvimento local**.

### Testes

```powershell
./gradlew test
```

---

## 9. Endpoints

### Criar transação: `POST /api/v1/transactions`

```json
{
  "description": "Estacionamento",
  "amountCents": 6000,
  "category": "AUTO"
}
```

Retorna `201 Created`. Dados inválidos retornam `400` com uma mensagem de erro.

### Listar e filtrar: `GET /api/v1/transactions`

Lista os registros mais recentes primeiro. Use `?category=AUTO` para filtrar.

### Conversar por texto: `POST /api/v1/assistant/text`

```json
{ "message": "Paguei R$ 60 de estacionamento hoje" }
```

Resposta: `{ "answer": "..." }`. Mensagem em branco retorna `400`.

### Conversar por áudio: `POST /api/v1/assistant/voice`

Recebe `multipart/form-data` com o arquivo no campo `audio`, transcreve, consulta o assistente e devolve `audio/mpeg`.

```powershell
curl.exe -X POST http://localhost:8080/api/v1/assistant/voice `
  -F "audio=@.\minha-mensagem.wav" `
  --output resposta.mp3
```

Os formatos aceitos dependem do provedor configurado. O limite HTTP da aplicação é de 20 MB.

---

## 10. Resultados entregues

- Contrato REST para criar e listar transações, com filtro por categoria.
- Conversa por texto que registra ou consulta despesas via *tool calling*.
- Fluxo de voz completo: transcrição, interpretação e resposta em MP3.
- Persistência configurável entre H2 e MySQL, sem alterar o domínio.
- 21 casos de teste em 7 classes (domínio, casos de uso, ferramentas, controllers e adaptador de persistência).

Esses são resultados de **implementação**. Não há avaliação com usuários, dados operacionais nem ambiente publicado, então não apresento taxas de acerto, redução de tempo ou economia financeira.

### Desempenho de negócio

O impacto financeiro **não pode ser calculado com os dados atuais**. Para estimá-lo, seria preciso medir:

- o custo médio do registro manual;
- o custo das chamadas de chat, transcrição e voz por interação;
- a taxa de correções manuais após o lançamento por IA;
- o tempo realmente economizado.

Com isso, a comparação seria: custo por lançamento manual versus custo por lançamento automatizado mais o custo das correções.

---

## 11. Limites atuais e boas práticas

- A IA só executa o que está exposto em `TransactionTools`; não tem acesso direto ao banco.
- **Sem autenticação, autorização ou isolamento por usuário.** Não exponha a API publicamente.
- Áudio e transcrição podem conter dados financeiros pessoais. A aplicação não arquiva o arquivo recebido; o provedor de IA o processa conforme a configuração e os termos da conta.
- `ddl-auto=update` serve ao desenvolvimento. Antes de produção, use migrações versionadas.
- Revise idempotência e confirmação de intenção antes de qualquer uso financeiro real, pois chamadas a modelos não são determinísticas.

---

## 12. Aprendizados

- **Separar regras de negócio da IA:** o modelo escolhe entre ferramentas restritas, mas os casos de uso validam e gravam. Isso torna o comportamento financeiro previsível mesmo com linguagem natural.
- **Dinheiro sem ponto flutuante:** centavos em `long` eliminam uma classe inteira de erros de arredondamento.
- **Domínio isolado da infraestrutura:** o mesmo contrato roda em H2 e MySQL, e os casos de uso são testáveis com repositórios em memória, sem Spring.
- **Validar antes de confiar:** o que mais exige cuidado é o comportamento real da IA com áudios e frases ambíguas. Quero medi-lo com dados de teste antes de qualquer decisão de produção.

---

## 13. Próximos passos

1. Adicionar identidade de usuário e associar cada transação ao seu proprietário.
2. Criar migrações com Flyway ou Liquibase e uma política de retenção para dados sensíveis.
3. Registrar metadados de auditoria com consentimento.
4. Instrumentar latência, falhas de transcrição e acerto de categoria, sem registrar áudio ou valores em logs.
5. Montar um conjunto de frases e áudios de teste para avaliar a compreensão dos comandos.
6. Ampliar a cobertura de testes de integração.

---

**Autor:** Sérgio Santos — Cientista de Dados | Ambientes Críticos e Governança de Dados

[![Portfólio Sérgio Santos](https://img.shields.io/badge/Portfólio-Sérgio_Santos-111827?style=for-the-badge&logo=githubpages&logoColor=00eaff)](https://portfoliosantossergio.vercel.app)
[![LinkedIn Sérgio Santos](https://img.shields.io/badge/LinkedIn-Sérgio_Santos-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/santossergioluiz)
