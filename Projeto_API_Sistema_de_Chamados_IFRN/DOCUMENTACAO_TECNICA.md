# Documentação Técnica — Sistema de Chamados IFRN

Este documento destina-se a desenvolvedores, analistas e mantenedores da aplicação. Ele descreve a arquitetura, o modelo de dados, as regras de negócio, a API REST, os endpoints, o tratamento de exceções, as migrations, a configuração e a implantação com Docker.

---

## 1. Visão Geral

O **Sistema de Chamados IFRN** é uma API REST para registrar, acompanhar e gerenciar chamados de suporte técnico dentro do IFRN. A aplicação permite que solicitantes abram chamados e que técnicos dos setores responsáveis assumam, resolvam e cancelem essas solicitações, mantendo um histórico de interações por chamado.

### 1.1 Objetivo

Padronizar o atendimento de problemas de infraestrutura e tecnologia (computadores, rede, elétrica, projetores, etc.) por meio de um sistema centralizado, com controle de status, prioridades, responsáveis e rastreabilidade de ações.

### 1.2 Principais Atores

| Ator | Descrição | Permissões principais |
|------|-----------|----------------------|
| **Solicitante** | Aluno, professor ou servidor que comunica um problema | Abrir chamados, consultar próprios chamados, enviar interações, cancelar chamado aberto |
| **Técnico** | Funcionário de um setor responsável (ex: COTIC, MANUT) | Consultar chamados, assumir chamados de seu setor, resolver chamados atribuídos, enviar interações |

---

## 2. Stack de Tecnologia

| Camada | Tecnologia | Versão | Finalidade |
|--------|------------|--------|------------|
| Linguagem | Java | 21 | Desenvolvimento da aplicação |
| Framework | Spring Boot | 4.0.0 | API REST, injeção de dependência, configuração automática |
| Web | Spring MVC (`spring-boot-starter-web`) | — | Controllers REST, serialização JSON |
| Validação | Spring Validation (`spring-boot-starter-validation`) | — | Validação de entradas (`@Valid`, `@NotBlank`, `@Email`, etc.) |
| Persistência | Spring Data JPA (`spring-boot-starter-data-jpa`) | — | Mapeamento ORM, repositórios, JPA Specifications |
| Banco de Dados | PostgreSQL | 16 | Armazenamento relacional dos dados |
| Migrations | Flyway (`spring-boot-starter-flyway`, `flyway-database-postgresql`) | — | Versionamento e evolução do esquema do banco |
| Serialização | Jackson | 3 | Conversão objeto ↔ JSON |
| Containerização | Docker / Docker Compose | — | Build da imagem, orquestração API + banco |
| Build | Apache Maven | 3.9 | Gerenciamento de dependências e build |

---

## 3. Arquitetura e Camadas

A aplicação segue uma arquitetura em camadas, com separação clara de responsabilidades:

```
br.edu.ifrn.chamados
├── SistemaChamadosApplication.java   # Classe principal (@SpringBootApplication)
├── controller/                       # Camada de apresentação (REST Controllers)
│   ├── CadastroController.java       # Setores, Categorias, Usuários (CRUD + filtros)
│   ├── ChamadoController.java        # Chamados (abrir, listar, detalhar, assumir, resolver, cancelar, interações)
│   └── UsuarioChamadoController.java # Chamados por usuário (GET /usuarios/{id}/chamados)
├── dto/                              # Objetos de transferência (requests/responses)
│   └── Dtos.java                     # Registros (records) com validação de entrada
├── exception/                        # Tratamento global de exceções
│   ├── ApiExceptionHandler.java      # @RestControllerAdvice — mapeia exceções para HTTP
│   └── Erros.java                    # Exceções de negócio (NaoEncontrado, RegraNegocio, Conflito)
├── model/                            # Entidades JPA
│   ├── Categoria.java
│   ├── Chamado.java
│   ├── Interacao.java
│   ├── Prioridade.java               # enum
│   ├── Setor.java
│   ├── StatusChamado.java            # enum
│   ├── TipoUsuario.java              # enum
│   └── Usuario.java
├── repository/                       # Camada de acesso a dados (Spring Data JPA)
│   ├── CategoriaRepository.java
│   ├── ChamadoRepository.java
│   ├── InteracaoRepository.java
│   ├── SetorRepository.java
│   └── UsuarioRepository.java
└── service/                          # Camada de negócio
    ├── CadastroService.java          # Regras de cadastro (setores, categorias, usuários)
    └── ChamadoService.java           # Regras de ciclo de vida dos chamados
```

### Fluxo de uma requisição

1. O cliente envia uma requisição HTTP para um endpoint exposto por um `@RestController`.
2. O Controller valida o corpo da requisição com `@Valid` e delega ao `Service`.
3. O `Service` aplica as regras de negócio, acessa o banco via `Repository` e lança exceções de negócio quando necessário.
4. O `Repository` executa consultas JPA (com `JpaSpecificationExecutor` para filtros dinâmicos).
5. Em caso de exceção, o `ApiExceptionHandler` intercepta e retorna uma resposta JSON padronizada com o código HTTP adequado.
6. O Controller retorna o DTO de resposta serializado em JSON.

---

## 4. Modelo de Dados (Entidades)

### 4.1 Diagrama Entidade-Relacionamento (texto)

```
SETOR (1) ----< (N) CATEGORIA
SETOR (1) ----< (N) USUARIO (técnicos)
CATEGORIA (1) ----< (N) CHAMADO
USUARIO (1) ----< (N) CHAMADO (como solicitante)
USUARIO (1) ----< (N) CHAMADO (como técnico, opcional)
CHAMADO (1) ----< (N) INTERACAO
USUARIO (1) ----< (N) INTERACAO (como autor)
```

### 4.2 Entidades

#### Setor
| Atributo | Tipo | Restrições | Descrição |
|----------|------|------------|-----------|
| `id` | Long | PK, autoincremento | Identificador |
| `nome` | String(120) | NOT NULL | Nome do setor (ex: Coordenação de Tecnologia da Informação) |
| `sigla` | String(20) | NOT NULL, UNIQUE | Sigla (ex: COTIC) |

Relacionamentos: possui muitas `Categoria`s e muitos `Usuario`s.

#### Categoria
| Atributo | Tipo | Restrições | Descrição |
|----------|------|------------|-----------|
| `id` | Long | PK, autoincremento | Identificador |
| `nome` | String(120) | NOT NULL | Nome da categoria (ex: Computadores) |
| `descricao` | String(500) | — | Descrição opcional |
| `setor_id` | Long | NOT NULL, FK → setores(id) | Setor ao qual pertence |

Relacionamentos: pertence a um `Setor`.

#### Usuario
| Atributo | Tipo | Restrições | Descrição |
|----------|------|------------|-----------|
| `id` | Long | PK, autoincremento | Identificador |
| `nome` | String(150) | NOT NULL | Nome completo |
| `email` | String(180) | NOT NULL, UNIQUE | E-mail institucional |
| `matricula` | String(30) | NOT NULL | Matrícula / SIAPE |
| `tipo` | TipoUsuario | NOT NULL, CHECK | `SOLICITANTE` ou `TECNICO` |
| `setor_id` | Long | FK → setores(id) | Setor (obrigatório para técnicos) |

Restrição de negócio: técnicos devem possuir um setor vinculado.

#### Chamado
| Atributo | Tipo | Restrições | Descrição |
|----------|------|------------|-----------|
| `id` | Long | PK, autoincremento | Identificador |
| `titulo` | String(180) | NOT NULL | Título do chamado |
| `descricao` | String(2000) | NOT NULL | Descrição detalhada do problema |
| `local` | String(180) | NOT NULL | Local onde ocorreu o problema |
| `prioridade` | Prioridade | NOT NULL, CHECK | `BAIXA`, `MEDIA`, `ALTA` (padrão: `MEDIA`) |
| `status` | StatusChamado | NOT NULL, CHECK | `ABERTO`, `EM_ATENDIMENTO`, `RESOLVIDO`, `CANCELADO` (padrão: `ABERTO`) |
| `data_abertura` | TIMESTAMP | NOT NULL | Data/hora de abertura (auto: agora) |
| `data_fechamento` | TIMESTAMP | — | Data/hora de fechamento (preenchido ao resolver/cancelar) |
| `solucao` | String(2000) | — | Descrição da solução aplicada |
| `solicitante_id` | Long | NOT NULL, FK → usuarios(id) | Quem abriu o chamado |
| `categoria_id` | Long | NOT NULL, FK → categorias(id) | Categoria do problema |
| `tecnico_id` | Long | FK → usuarios(id) | Técnico responsável (opcional até assumir) |

Relacionamentos: pertence a um solicitante, uma categoria e opcionalmente a um técnico; possui muitas `Interacao`s.

#### Interacao
| Atributo | Tipo | Restrições | Descrição |
|----------|------|------------|-----------|
| `id` | Long | PK, autoincremento | Identificador |
| `texto` | String(2000) | NOT NULL | Mensagem/interação |
| `data_hora` | TIMESTAMP | NOT NULL | Data/hora (auto: agora) |
| `chamado_id` | Long | NOT NULL, FK → chamados(id) | Chamado ao qual pertence |
| `autor_id` | Long | NOT NULL, FK → usuarios(id) | Quem escreveu a interação |

Restrições de negócio: apenas o solicitante ou o técnico atribuído podem interagir; chamados `RESOLVIDO` ou `CANCELADO` não aceitam novas interações.

---

## 5. Enums

| Enum | Valores | Uso |
|------|---------|-----|
| `Prioridade` | `BAIXA`, `MEDIA`, `ALTA` | Prioridade do chamado |
| `StatusChamado` | `ABERTO`, `EM_ATENDIMENTO`, `RESOLVIDO`, `CANCELADO` | Ciclo de vida do chamado |
| `TipoUsuario` | `SOLICITANTE`, `TECNICO` | Tipo de usuário |

---

## 6. Regras de Negócio

### 6.1 Cadastros (Setor, Categoria, Usuário)

- **Setor**: sigla deve ser única (case-insensitive).
- **Categoria**: deve pertencer a um setor existente.
- **Usuário**: e-mail deve ser único (case-insensitive). Técnico deve possuir `setorId` informado.
- **Exclusão de setor**: não permitido se existir chamado vinculado a alguma categoria do setor.
- **Exclusão de categoria**: não permitido se existir chamado vinculado à categoria.

### 6.2 Chamados

| Ação | Pré-condições | Regras |
|------|--------------|--------|
| **Abrir** | Usuário existe | Apenas `SOLICITANTE` pode abrir. Categoria deve existir. Status inicia como `ABERTO`. Prioridade padrão `MEDIA`. |
| **Assumir** | Chamado `ABERTO`, usuário é `TECNICO` | Técnico deve pertencer ao mesmo setor da categoria do chamado. |
| **Resolver** | Chamado `EM_ATENDIMENTO` | Apenas o técnico atribuído pode resolver. Exige texto da solução. Define `dataFechamento`. |
| **Cancelar** | Chamado `ABERTO` | Apenas o solicitante proprietário pode cancelar. Define `dataFechamento`. |
| **Adicionar interação** | Chamado não encerrado | Autor deve ser o solicitante ou o técnico atribuído. Chamados `RESOLVIDO` ou `CANCELADO` bloqueiam novas interações. |

### 6.3 Fluxo de Estados do Chamado

```
ABERTO  →  EM_ATENDIMENTO  →  RESOLVIDO
   │                         ▲
   └──────────→  CANCELADO   ┘
```

- Não é permitido voltar a um estado anterior.
- Chamados `RESOLVIDO` ou `CANCELADO` não aceitam novas interações.

---

## 7. API REST — Endpoints

Base URL: `http://localhost:8080`

Todas as respostas de listagem são paginadas com o envelope:

```json
{
  "itens": [...],
  "pagina": 0,
  "tamanho": 10,
  "totalElementos": 25,
  "totalPaginas": 3
}
```

### 7.1 Setores (`/setores`)

| Método | Endpoint | Descrição | Body de entrada | Resposta |
|--------|----------|-----------|-----------------|----------|
| `POST` | `/setores` | Criar setor | `SetorRequest` | `Setor` (201) |
| `GET` | `/setores` | Listar setores (filtro opcional por nome, paginado) | — | `Pagina<Setor>` (200) |
| `GET` | `/setores/{id}` | Consultar setor por ID | — | `Setor` (200) |
| `PUT` | `/setores/{id}` | Atualizar setor | `SetorRequest` | `Setor` (200) |
| `DELETE` | `/setores/{id}` | Excluir setor | — | 204 No Content |

**`SetorRequest`**: `{ "nome": "string", "sigla": "string" }` (ambos obrigatórios)

### 7.2 Categorias (`/categorias`)

| Método | Endpoint | Descrição | Body de entrada | Resposta |
|--------|----------|-----------|-----------------|----------|
| `POST` | `/categorias` | Criar categoria | `CategoriaRequest` | `Categoria` (200) |
| `GET` | `/categorias` | Listar categorias (filtros: nome, setorId, paginado) | — | `Pagina<Categoria>` (200) |
| `GET` | `/setores/{id}/categorias` | Listar categorias de um setor | — | `Pagina<Categoria>` (200) |
| `GET` | `/categorias/{id}` | Consultar categoria por ID | — | `Categoria` (200) |
| `PUT` | `/categorias/{id}` | Atualizar categoria | `CategoriaRequest` | `Categoria` (200) |
| `DELETE` | `/categorias/{id}` | Excluir categoria | — | 204 No Content |

**`CategoriaRequest`**: `{ "nome": "string", "descricao": "string", "setorId": number }` (nome e setorId obrigatórios)

### 7.3 Usuários (`/usuarios`)

| Método | Endpoint | Descrição | Body de entrada | Resposta |
|--------|----------|-----------|-----------------|----------|
| `POST` | `/usuarios` | Criar usuário | `UsuarioRequest` | `Usuario` (200) |
| `GET` | `/usuarios` | Listar usuários (filtros: nome, tipo, setorId, paginado) | — | `Pagina<Usuario>` (200) |
| `GET` | `/usuarios/{id}` | Consultar usuário por ID | — | `Usuario` (200) |
| `PUT` | `/usuarios/{id}` | Atualizar usuário | `UsuarioRequest` | `Usuario` (200) |

**`UsuarioRequest`**: `{ "nome": "string", "email": "string", "matricula": "string", "tipo": "SOLICITANTE\|TECNICO", "setorId": number }`

### 7.4 Chamados (`/chamados`)

| Método | Endpoint | Descrição | Body de entrada | Resposta |
|--------|----------|-----------|-----------------|----------|
| `POST` | `/chamados` | Abrir chamado | `ChamadoRequest` | `ChamadoResponse` (201) |
| `GET` | `/chamados` | Listar chamados (muitos filtros, paginado) | — | `Pagina<ChamadoResponse>` (200) |
| `GET` | `/chamados/{id}` | Detalhar chamado | — | `ChamadoResponse` (200) |
| `PATCH` | `/chamados/{id}/assumir` | Assumir chamado | `AcaoRequest` (`usuarioId`) | `ChamadoResponse` (200) |
| `PATCH` | `/chamados/{id}/resolver` | Resolver chamado | `SolucaoRequest` (`tecnicoId`, `solucao`) | `ChamadoResponse` (200) |
| `PATCH` | `/chamados/{id}/cancelar` | Cancelar chamado | `AcaoRequest` (`usuarioId`) | `ChamadoResponse` (200) |
| `POST` | `/chamados/{id}/interacoes` | Adicionar interação | `InteracaoRequest` (`texto`, `autorId`) | `InteracaoResponse` (201) |

**Filtros disponíveis em `GET /chamados`**: `status`, `prioridade`, `setorId`, `categoriaId`, `solicitanteId`, `tecnicoId`, `dataInicio`, `dataFim`, `page`, `size`, `sort`.

**`ChamadoRequest`**: `{ "titulo": "string", "descricao": "string", "local": "string", "prioridade": "BAIXA\|MEDIA\|ALTA", "solicitanteId": number, "categoriaId": number }`

**`SolucaoRequest`**: `{ "tecnicoId": number, "solucao": "string" }`

**`AcaoRequest`**: `{ "usuarioId": number }`

**`InteracaoRequest`**: `{ "texto": "string", "autorId": number }`

### 7.5 Chamados por Usuário (`/usuarios/{id}/chamados`)

| Método | Endpoint | Descrição | Parâmetros | Resposta |
|--------|----------|-----------|------------|----------|
| `GET` | `/usuarios/{id}/chamados` | Listar chamados de um usuário (solicitante ou técnico) | `status` (opcional), `page`, `size` | `Pagina<ChamadoResponse>` (200) |

### 7.6 `ChamadoResponse`

```json
{
  "id": 1,
  "titulo": "Computador não liga",
  "descricao": "...",
  "local": "Laboratório 1",
  "prioridade": "ALTA",
  "status": "ABERTO",
  "dataAbertura": "2026-10-26T10:00:00",
  "dataFechamento": null,
  "solucao": null,
  "solicitanteId": 1,
  "categoriaId": 1,
  "setorId": 1,
  "tecnicoId": null,
  "interacoes": [
    {
      "id": 1,
      "texto": "Chamado aberto e aguardando atendimento.",
      "dataHora": "2026-10-26T10:00:00",
      "autorId": 1,
      "autorNome": "Ana Solicitante"
    }
  ]
}
```

---

## 8. Tratamento de Exceções e Códigos HTTP

A classe `ApiExceptionHandler` (`@RestControllerAdvice`) centraliza o tratamento de erros:

| Exceção / Situação | Código HTTP | Mensagem de erro |
|--------------------|-------------|------------------|
| `Erros.NaoEncontrado` (entidade não existe) | `404 Not Found` | Mensagem da exceção |
| `Erros.RegraNegocio` (regra de negócio violada) | `409 Conflict` | Mensagem da exceção |
| `Erros.Conflito` (duplicidade, ex: e-mail/sigla já existe) | `409 Conflict` | Mensagem da exceção |
| `MethodArgumentNotValidException` (falha de validação `@Valid`) | `400 Bad Request` | Lista de campos com erro |
| `ConstraintViolationException` / `IllegalArgumentException` | `400 Bad Request` | Mensagem da exceção |

Formato da resposta de erro:

```json
{
  "timestamp": "2026-10-27T10:15:30.123Z",
  "status": 409,
  "mensagem": "Sigla já cadastrada.",
  "caminho": "/setores"
}
```

> Nota: `server.error.include-message=never` está configurado para evitar vazamento de stack traces do Spring; as mensagens são controladas pelo `ApiExceptionHandler`.

---

## 9. Camada de Repositório

Todos os repositórios estendem `JpaRepository<Entidade, Long>` e `JpaSpecificationExecutor<Entidade>`, permitindo consultas dinâmicas via JPA Specifications.

| Repositório | Métodos personalizados |
|-------------|----------------------|
| `CategoriaRepository` | — (herda todos do JpaRepository) |
| `ChamadoRepository` | `existsByCategoriaSetorId(Long)`, `existsByCategoriaId(Long)`, `existsBySolicitanteId(Long)` |
| `InteracaoRepository` | — |
| `SetorRepository` | `existsBySiglaIgnoreCase(String)` |
| `UsuarioRepository` | `existsByEmailIgnoreCase(String)` |

Os métodos `existsBy*` são usados pelos serviços para validar regras de exclusão e duplicidade sem carregar entidades desnecessariamente.

---

## 10. Migrations do Flyway

As migrations ficam em `src/main/resources/db/migration/` e são executadas na inicialização da aplicação.

| Versão | Arquivo | Conteúdo |
|--------|---------|----------|
| `V1` | `V1__criar_tabelas.sql` | Cria tabelas `setores`, `categorias`, `usuarios`, `chamados`, `interacoes` com chaves primárias, estrangeiras e constraints CHECK |
| `V2` | `V2__inserir_dados_iniciais.sql` | Insere 2 setores (COTIC, MANUT), 4 categorias e 4 usuários iniciais |
| `V3` | `V3__popular_cenarios_de_teste.sql` | Insere usuários adicionais, 4 chamados em diferentes estados do ciclo de vida e interações iniciais; ajusta sequences |

### Regras importantes sobre Flyway

- `spring.jpa.hibernate.ddl-auto=validate`: o Hibernate apenas valida que o esquema corresponde às entidades; não cria nem altera tabelas automaticamente.
- Nunca edite uma migration já aplicada: alterar o checksum faz o Flyway abortar a inicialização para proteger a integridade do banco.
- Correções devem ser adicionadas como uma nova versão (ex: `V4__corrigir_dados.sql`).
- O histórico de migrations é armazenado na tabela `flyway_schema_history`.

---

## 11. Configuração

Arquivo: `src/main/resources/application.properties`

```properties
spring.application.name=sistema-chamados
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/chamados}
spring.datasource.username=${DB_USERNAME:chamados}
spring.datasource.password=${DB_PASSWORD:chamados}
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
server.error.include-message=never
```

As credenciais do banco devem ser injetadas por variáveis de ambiente (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`), nunca hardcoded no código. Valores padrão são usados apenas em desenvolvimento local.

---

## 12. Docker e Implantação

### 12.1 Dockerfile (multi-stage)

1. **Estágio de build** (`maven:3.9-eclipse-temurin-21`): baixa dependências offline, compila o projeto e gera o `.jar`.
2. **Estágio de runtime** (`eclipse-temurin:21-jre`): imagem menor contendo apenas o JRE e o `.jar` compilado. Expõe a porta 8080.

### 12.2 docker-compose.yml

Cria dois serviços:

| Serviço | Imagem | Porta | Variáveis de ambiente |
|---------|--------|-------|----------------------|
| `db` | `postgres:16-alpine` | 5432 | `POSTGRES_DB=chamados`, `POSTGRES_USER=chamados`, `POSTGRES_PASSWORD=chamados` |
| `api` | build a partir do `Dockerfile` | 8080 | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (vinculados ao serviço `db`) |

Um volume `postgres_data` preserva os dados do banco entre reinicializações.

### Comandos úteis

```bash
# Build e iniciar (primeira vez ou após alterações)
docker compose up --build

# Iniciar em segundo plano
docker compose up --build -d

# Acompanhar logs da API
docker compose logs -f api

# Parar containers (preserva dados)
docker compose down

# Recriar banco do zero (APAGA DADOS)
docker compose down -v
docker compose up --build
```

---

## 13. Estrutura de Pacotes e Responsabilidades

| Pacote | Classes principais | Responsabilidade |
|--------|-------------------|------------------|
| `controller` | `CadastroController`, `ChamadoController`, `UsuarioChamadoController` | Expor endpoints REST, receber requisições, retornar respostas |
| `dto` | `Dtos` (records) | Definir contratos de entrada/saída com validação de bean |
| `exception` | `ApiExceptionHandler`, `Erros` | Tratamento global de exceções e mapeamento para códigos HTTP |
| `model` | Entidades JPA + enums | Modelo de domínio e mapeamento relacional |
| `repository` | `*Repository` interfaces | Acesso a dados via Spring Data JPA |
| `service` | `CadastroService`, `ChamadoService` | Regras de negócio e orquestração entre controller e repository |

---

## 14. Cenários de Teste Disponíveis (V3)

Após executar as migrations em um banco vazio, estão disponíveis:

| Chamado | Status | Uso sugerido |
|---------|--------|--------------|
| 1 | `ABERTO` | Assumir, interagir ou cancelar pelo solicitante 1 |
| 2 | `EM_ATENDIMENTO` | Interagir ou resolver pelo técnico 3 |
| 3 | `RESOLVIDO` | Confirmar bloqueio de novas alterações/interações |
| 4 | `CANCELADO` | Confirmar bloqueio de novas alterações/interações |

Casos de erro esperados (HTTP 409):

- Assumir chamado 1 com técnico 4 (outro setor).
- Resolver chamado 2 com técnico diferente do 3.
- Cancelar chamado 2 (não está mais aberto).
- Registrar interação no chamado 3 (RESOLVIDO).
- Criar usuário com e-mail já existente.

---

## 15. Segurança e Boas Práticas Implementadas

1. **Validação de entrada**: todos os DTOs de request utilizam `@NotBlank`, `@NotNull`, `@Email` e validação via `@Valid` nos controllers.
2. **Sem vazamento de stack traces**: `server.error.include-message=never` evita expor detalhes internos do Spring em erros.
3. **Versionamento de banco**: Flyway controla a evolução do esquema; `ddl-auto=validate` impede alterações implícitas pelo Hibernate.
4. **Credenciais por variável de ambiente**: nenhuma senha está hardcoded no código-fonte.
5. **Relacionamentos bidirecionais controlados**: `@JsonIgnore` evita loops de serialização infinita entre entidades relacionadas.
6. **Paginação com limite máximo**: o serviço limita o `pageSize` a 50 registros por página, evitando consultas pesadas.

---

## 16. Limitações Conhecidas

- Não há autenticação/autorização (Spring Security) implementada; o acesso aos endpoints é aberto.
- Não há endpoint de exclusão de chamados.
- Não há endpoint de listagem de interações isolada (elas são retornadas dentro do `ChamadoResponse`).
- A atualização de usuário permite alterar o tipo (SOLICITANTE ↔ TECNICO) sem revalidar chamados já associados.

---

## 17. Arquivos Importantes

| Caminho | Descrição |
|---------|-----------|
| `README.md` | Guia de instalação e uso rápido |
| `DOCUMENTACAO.md` | Explicação simples (não-técnica) e técnica resumida |
| `DOCUMENTACAO_TECNICA.md` | Este documento |
| `requisicoes.http` | Requisições prontas para o VS Code REST Client |
| `pom.xml` | Dependências e build Maven |
| `Dockerfile` | Imagem da aplicação (multi-stage) |
| `docker-compose.yml` | Orquestração API + PostgreSQL |
| `application.properties` | Configurações da aplicação |
| `V1__criar_tabelas.sql` | Estrutura inicial do banco |
| `V2__inserir_dados_iniciais.sql` | Cadastros iniciais |
| `V3__popular_cenarios_de_teste.sql` | Cenários para testes |

---

## 18. Glossário

| Termo | Significado |
|-------|-------------|
| Chamado | Registro de um problema ou solicitação de suporte |
| Solicitante | Usuário que abre o chamado |
| Técnico | Usuário de um setor responsável que atende chamados |
| Setor | Equipe responsável por um grupo de categorias (ex: COTIC, MANUT) |
| Categoria | Tipo de problema, vinculada a um único setor |
| Interação | Mensagem ou atualização dentro de um chamado, criando histórico |
| Prioridade | Nível de urgência: BAIXA, MEDIA, ALTA |
| Status | Estado do chamado no ciclo de vida: ABERTO, EM_ATENDIMENTO, RESOLVIDO, CANCELADO |

---

*Documentação gerada com base na versão 0.0.1-SNAPSHOT do projeto (Java 21, Spring Boot 4.0.0, PostgreSQL 16).*