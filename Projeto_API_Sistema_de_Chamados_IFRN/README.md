# Sistema de Chamados IFRN

API REST para registro e gerenciamento de chamados de suporte técnico do IFRN. Desenvolvida para a disciplina de Desenvolvimento de Sistemas Corporativos (DSC).

---

## 1. Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 4** (Spring MVC, Spring Data JPA, Spring Validation)
- **PostgreSQL 16**
- **Flyway** (gerenciamento e versionamento de banco de dados)
- **Docker** e **Docker Compose**

---

## 2. Como Executar

O projeto está totalmente conteinerizado. O único pré-requisito é ter o **Docker** e o **Docker Compose** instalados na máquina.

1. Inicie a aplicação e o banco de dados:

   ```bash
   docker compose up --build
   ```

2. Para executar em segundo plano:

   ```bash
   docker compose up --build -d
   ```

3. Acompanhar os logs da API:

   ```bash
   docker compose logs -f api
   ```

4. Parar a aplicação:

   ```bash
   docker compose down
   ```

5. Parar e resetar o volume do banco de dados (recriação limpa):

   ```bash
   docker compose down -v
   ```

A API estará acessível em: **`http://localhost:8080`**

### Variáveis de Ambiente (padrão do Compose)

| Variável | Valor Padrão Local |
|---|---|
| `DB_URL` | `jdbc:postgresql://db:5432/chamados` |
| `DB_USERNAME` | `chamados` |
| `DB_PASSWORD` | `chamados` |

---

## 3. Banco de Dados e Dados Iniciais

O Flyway aplica automaticamente as migrations ao iniciar:
- `V1`: Criação das tabelas (`setores`, `categorias`, `usuarios`, `chamados`, `interacoes`).
- `V2`: Carga de dados iniciais (setores, categorias e usuários).
- `V3`: Cenários completos de teste (chamados e interações com diferentes status).

### Dados Pré-cadastrados

- **Setores**:
  - `1`: Coordenação de Tecnologia da Informação (`COTIC`)
  - `2`: Setor de Manutenção (`MANUT`)

- **Categorias**:
  - `1`: Computadores (Setor: COTIC)
  - `2`: Rede e Wi-Fi (Setor: COTIC)
  - `3`: Elétrica (Setor: MANUT)
  - `4`: Projetores (Setor: MANUT)

- **Usuários**:
  - `1`: Ana Solicitante (`SOLICITANTE`)
  - `2`: Bruno Solicitante (`SOLICITANTE`)
  - `3`: Carlos Técnico (`TECNICO` - Setor COTIC)
  - `4`: Diana Técnica (`TECNICO` - Setor MANUT)
  - `5`: Eduardo Solicitante (`SOLICITANTE`)
  - `6`: Fernanda Técnica (`TECNICO` - Setor COTIC)

- **Chamados de Teste**:
  - `1`: Status `ABERTO`, Prioridade `ALTA`
  - `2`: Status `EM_ATENDIMENTO`, Prioridade `MEDIA` (atribuído ao técnico Carlos)
  - `3`: Status `RESOLVIDO`, Prioridade `ALTA` (atribuído à técnica Diana)
  - `4`: Status `CANCELADO`, Prioridade `BAIXA`

---

## 4. Ciclo de Vida e Regras de Negócio

### Transições de Estado

```text
ABERTO -> EM_ATENDIMENTO -> RESOLVIDO
ABERTO -> CANCELADO
```

### Regras Principais

1. **Abertura**: Todo chamado nasce com status `ABERTO`, prioridade `MEDIA` (caso não informada) e somente usuários do tipo `SOLICITANTE` podem abrir chamados.
2. **Atendimento**: Um técnico só pode assumir chamados com status `ABERTO` e cuja categoria pertença ao mesmo setor do técnico. O status muda para `EM_ATENDIMENTO`.
3. **Resolução**: Apenas o técnico atribuído ao chamado pode resolvê-lo. A descrição da `solucao` é obrigatória.
4. **Cancelamento**: Apenas o próprio solicitante pode cancelar o chamado, e exclusivamente enquanto o status for `ABERTO`.
5. **Interações**: Somente o solicitante e o técnico atribuído podem registrar mensagens. Chamados `RESOLVIDO` ou `CANCELADO` não aceitam novas interações nem alterações.
6. **Integridade**: Não é permitido excluir setores ou categorias com chamados associados (retorna `409 Conflict`). E-mail de usuário e sigla de setor devem ser únicos.

---

## 5. Endpoints da API

Todas as rotas de listagem (`GET` de coleção) são paginadas (`page`, `size`, `sort`) e suportam filtros via query parameter.

| Método | Rota | Descrição | Status Sucesso |
|---|---|---|:---:|
| `POST` | `/setores` | Cadastra um novo setor | `200` |
| `GET` | `/setores` | Lista setores (filtro: `nome`) | `200` |
| `GET` | `/setores/{id}` | Busca setor por ID | `200` |
| `PUT` | `/setores/{id}` | Atualiza um setor | `200` |
| `DELETE` | `/setores/{id}` | Exclui um setor sem dependências | `204` |
| `POST` | `/categorias` | Cadastra uma nova categoria | `200` |
| `GET` | `/categorias` | Lista categorias (filtros: `nome`, `setorId`) | `200` |
| `GET` | `/categorias/{id}` | Busca categoria por ID | `200` |
| `GET` | `/setores/{id}/categorias` | Lista categorias de um setor específico | `200` |
| `PUT` | `/categorias/{id}` | Atualiza uma categoria | `200` |
| `DELETE` | `/categorias/{id}` | Exclui uma categoria sem dependências | `204` |
| `POST` | `/usuarios` | Cadastra um usuário (`SOLICITANTE` ou `TECNICO`) | `200` |
| `GET` | `/usuarios` | Lista usuários (filtros: `nome`, `tipo`, `setorId`) | `200` |
| `GET` | `/usuarios/{id}` | Busca usuário por ID | `200` |
| `PUT` | `/usuarios/{id}` | Atualiza um usuário | `200` |
| `POST` | `/chamados` | Abre um novo chamado | `201` |
| `GET` | `/chamados` | Lista chamados (filtros: `status`, `prioridade`, `setorId`, `categoriaId`, `solicitanteId`, `tecnicoId`, `dataInicio`, `dataFim`) | `200` |
| `GET` | `/chamados/{id}` | Detalha um chamado e seu histórico de interações | `200` |
| `GET` | `/usuarios/{id}/chamados` | Lista chamados abertos ou atribuídos a um usuário | `200` |
| `PATCH` | `/chamados/{id}/assumir` | Técnico assume um chamado | `200` |
| `PATCH` | `/chamados/{id}/resolver` | Técnico atribuído resolve o chamado com a solução | `200` |
| `PATCH` | `/chamados/{id}/cancelar` | Solicitante cancela um chamado aberto | `200` |
| `POST` | `/chamados/{id}/interacoes` | Registra mensagem no histórico do chamado | `201` |

---

## 6. Como Testar a API

O repositório já inclui coleções prontas e especificações:

- **VS Code REST Client**: Arquivo [`requisicoes.http`](./requisicoes.http) pronto para execução direta no editor.
- **OpenAPI / Swagger**: Arquivo [`openapi.yaml`](./openapi.yaml), que pode ser importado no [Swagger Editor](https://editor.swagger.io/) ou em ferramentas compatíveis.
- **Insomnia**: Arquivo [`insomnia-collection.json`](./insomnia-collection.json) com todos os cenários e variáveis de ambiente configurados.
- **Postman / Newman**: Arquivo [`postman-collection.json`](./postman-collection.json), cobrindo testes automatizados do fluxo completo e casos de erro.

### Exemplo de Fluxo via `curl`

```bash
# 1. Listar chamados
curl -X GET "http://localhost:8080/chamados?page=0&size=10"

# 2. Abrir um chamado
curl -X POST "http://localhost:8080/chamados" \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Computador não liga",
    "descricao": "Gabinete não dá sinal de energia",
    "local": "Lab 01",
    "prioridade": "ALTA",
    "solicitanteId": 1,
    "categoriaId": 1
  }'

# 3. Técnico (ID 3) assume o chamado (supondo ID 5)
curl -X PATCH "http://localhost:8080/chamados/5/assumir" \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 3}'

# 4. Adicionar interação
curl -X POST "http://localhost:8080/chamados/5/interacoes" \
  -H "Content-Type: application/json" \
  -d '{"texto": "Iniciando verificação da fonte de alimentação.", "autorId": 3}'

# 5. Resolver o chamado
curl -X PATCH "http://localhost:8080/chamados/5/resolver" \
  -H "Content-Type: application/json" \
  -d '{"tecnicoId": 3, "solucao": "Cabo de força e fonte substituídos."}'
```

---

## 7. Documentação Adicional

Para detalhes conceituais, diagramas e arquitetura interna, consulte:
- [Documentação Geral (`DOCUMENTACAO.md`)](./DOCUMENTACAO.md): Explicação de negócio e fluxos.
- [Documentação Técnica (`DOCUMENTACAO_TECNICA.md`)](./DOCUMENTACAO_TECNICA.md): Modelagem de dados, arquitetura em camadas, estrutura de DTOs e tratamento de exceções.
