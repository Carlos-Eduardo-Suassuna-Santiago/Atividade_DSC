# Documentação do Sistema de Chamados IFRN

Este documento explica como o sistema funciona em duas perspectivas:

1. uma explicação simples, para quem não trabalha com programação;
2. uma explicação técnica, para quem precisa entender ou manter o projeto.

O sistema é uma API REST para registrar pedidos de suporte do IFRN. Um exemplo
de pedido é: "o projetor da sala 10 não está funcionando" ou "o Wi-Fi do
laboratório está instável".

---

## 1. Explicação simples

### 1.1 O que é um chamado?

Um chamado é um registro de um problema ou solicitação. Ele reúne:

- o título do problema;
- uma descrição mais detalhada;
- o local onde o problema aconteceu;
- a prioridade;
- a pessoa que abriu a solicitação;
- a categoria do problema;
- o técnico responsável;
- o histórico das mensagens e ações realizadas.

Em vez de alguém informar um problema apenas por WhatsApp ou pessoalmente, o
sistema registra tudo em um único lugar.

### 1.2 Quem usa o sistema?

Existem dois tipos de usuário:

#### Solicitante

É quem comunica o problema. Pode:

- abrir chamados;
- consultar os próprios chamados;
- acompanhar o histórico;
- enviar interações;
- cancelar um chamado enquanto ele ainda estiver aberto.

Exemplos: aluno, professor ou servidor que identificou um problema.

#### Técnico

É quem atende o problema. Pode:

- consultar chamados;
- assumir chamados do seu setor;
- registrar interações;
- resolver chamados que assumiu;
- informar a solução aplicada.

Um técnico só pode atender categorias que pertencem ao setor ao qual ele está
vinculado. Por exemplo, um técnico da COTIC pode assumir chamados de
computadores e rede, mas não chamados de manutenção elétrica.

### 1.3 O que é um setor?

Um setor é a equipe responsável por um grupo de atendimentos.

Exemplos:

- COTIC: problemas de tecnologia, computadores e rede;
- MANUT: problemas de infraestrutura, elétrica e projetores.

Um setor pode possuir várias categorias e vários técnicos.

### 1.4 O que é uma categoria?

Categoria é o tipo do problema. Ela ajuda a encaminhar o chamado para o setor
correto.

Exemplos:

- Computadores;
- Rede e Wi-Fi;
- Elétrica;
- Projetores.

Cada categoria pertence a um único setor.

### 1.5 Como um chamado percorre o sistema?

O chamado segue um fluxo controlado:

```text
ABERTO -> EM_ATENDIMENTO -> RESOLVIDO
ABERTO -> CANCELADO
```

#### Aberto

O solicitante acabou de registrar o chamado. Ainda não existe técnico
responsável.

#### Em atendimento

Um técnico compatível com o setor da categoria assumiu o chamado.

#### Resolvido

O técnico atribuído informou uma solução. O sistema registra automaticamente a
data de fechamento.

#### Cancelado

O próprio solicitante cancelou o chamado enquanto ele ainda estava aberto.

Não é permitido voltar para um estado anterior. Um chamado resolvido ou
cancelado também não aceita novas interações.

### 1.6 O que é uma interação?

Uma interação é uma mensagem ou atualização dentro do chamado. Ela cria um
histórico do atendimento.

Exemplo:

1. o solicitante informa que o computador não liga;
2. o técnico responde que vai verificar o equipamento;
3. o solicitante informa que o problema continua;
4. o técnico registra a solução aplicada.

Somente o solicitante do chamado e o técnico atribuído podem registrar
interações.

### 1.7 Exemplo completo

1. Ana, uma solicitante, abre um chamado sobre um computador.
2. O sistema cria o chamado com status `ABERTO`.
3. O chamado fica associado à categoria `Computadores`.
4. Carlos, técnico da COTIC, assume o chamado.
5. O status muda para `EM_ATENDIMENTO`.
6. Carlos registra uma interação dizendo que está verificando o equipamento.
7. Carlos informa que trocou o cabo e resolve o chamado.
8. O status muda para `RESOLVIDO`.
9. O sistema preenche a data de fechamento.
10. O histórico fica disponível para consulta.

---

## 2. Como acessar a aplicação

A aplicação é executada em containers Docker:

- um container executa a API Java;
- outro container executa o PostgreSQL;
- um volume mantém os dados do banco.

Para iniciar:

```bash
docker compose up --build
```

Depois que a aplicação iniciar, a API estará disponível em:

```text
http://localhost:8080
```

Para consultar uma coleção pelo navegador, por exemplo:

```text
http://localhost:8080/setores?page=0&size=10
```

A barra do navegador é adequada para requisições `GET`. Para criar ou alterar
dados, use o arquivo [requisicoes.http](./requisicoes.http), Postman,
Insomnia ou `curl.exe`.

---

## 3. Visão técnica da aplicação

### 3.1 Tecnologias utilizadas

| Tecnologia | Responsabilidade |
|---|---|
| Java 21 | Linguagem da aplicação |
| Spring Boot 4 | Configuração e execução da API |
| Spring Web | Criação dos endpoints HTTP |
| Spring Data JPA | Acesso orientado a objetos ao banco |
| Hibernate | Implementação do JPA e validação do esquema |
| PostgreSQL | Banco de dados relacional |
| Flyway | Controle versionado das alterações do banco |
| Maven | Gerenciamento de dependências e build |
| Docker | Empacotamento e execução isolada |
| Docker Compose | Orquestração da API e do banco |

### 3.2 Estrutura de diretórios

```text
src/main/java/br/edu/ifrn/chamados/
├── SistemaChamadosApplication.java
├── controller/
├── dto/
├── exception/
├── model/
├── repository/
└── service/

src/main/resources/
├── application.properties
└── db/migration/
    ├── V1__criar_tabelas.sql
    ├── V2__inserir_dados_iniciais.sql
    └── V3__popular_cenarios_de_teste.sql
```

### 3.3 Camadas da arquitetura

O projeto usa uma arquitetura em camadas. Cada camada possui uma
responsabilidade específica.

```text
Cliente HTTP
    |
    v
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
PostgreSQL
```

#### Controller

Os controllers recebem requisições HTTP, validam o formato básico dos dados,
chamam os services e devolvem respostas.

Exemplos:

- [CadastroController.java](./src/main/java/br/edu/ifrn/chamados/controller/CadastroController.java)
- [ChamadoController.java](./src/main/java/br/edu/ifrn/chamados/controller/ChamadoController.java)
- [UsuarioChamadoController.java](./src/main/java/br/edu/ifrn/chamados/controller/UsuarioChamadoController.java)

Os controllers não devem decidir se um técnico pode ou não assumir um chamado.
Essa decisão pertence ao service.

#### Service

Os services concentram as regras de negócio:

- validação do tipo do usuário;
- validação do setor do técnico;
- controle das transições de status;
- autorização do cancelamento;
- autorização da resolução;
- autorização das interações;
- regras para exclusão de dados relacionados.

Exemplos:

- [CadastroService.java](./src/main/java/br/edu/ifrn/chamados/service/CadastroService.java)
- [ChamadoService.java](./src/main/java/br/edu/ifrn/chamados/service/ChamadoService.java)

#### Repository

Os repositories fazem a comunicação com o banco por meio do Spring Data JPA.
Eles estendem `JpaRepository` e, quando necessário,
`JpaSpecificationExecutor`.

Exemplos:

- [ChamadoRepository.java](./src/main/java/br/edu/ifrn/chamados/repository/ChamadoRepository.java)
- [UsuarioRepository.java](./src/main/java/br/edu/ifrn/chamados/repository/UsuarioRepository.java)

#### Model

As classes do pacote `model` representam as tabelas e relacionamentos do banco.
As entidades utilizam anotações JPA como `@Entity`, `@ManyToOne`,
`@OneToMany` e `@Enumerated(EnumType.STRING)`.

---

## 4. Modelo de dados

### 4.1 Relacionamentos

```text
Setor 1 -------- N Categoria
Setor 1 -------- N Usuario técnico
Usuario 1 ------ N Chamado como solicitante
Usuario 1 ------ N Chamado como técnico
Categoria 1 ---- N Chamado
Chamado 1 ------ N Interacao
Usuario 1 ------ N Interacao como autor
```

### 4.2 Entidades

#### Setor

Campos principais:

- `id`;
- `nome`;
- `sigla`, que deve ser única.

#### Categoria

Campos principais:

- `id`;
- `nome`;
- `descricao`;
- `setor_id`.

#### Usuario

Campos principais:

- `id`;
- `nome`;
- `email`, que deve ser único;
- `matricula`;
- `tipo`;
- `setor_id`, usado principalmente para técnicos.

#### Chamado

Campos principais:

- `id`;
- `titulo`;
- `descricao`;
- `local`;
- `prioridade`;
- `status`;
- `data_abertura`;
- `data_fechamento`;
- `solucao`;
- `solicitante_id`;
- `categoria_id`;
- `tecnico_id`.

#### Interacao

Campos principais:

- `id`;
- `texto`;
- `data_hora`;
- `chamado_id`;
- `autor_id`.

### 4.3 Enumerações

Os valores são armazenados como texto no PostgreSQL:

```text
TipoUsuario: SOLICITANTE, TECNICO
Prioridade: BAIXA, MEDIA, ALTA
StatusChamado: ABERTO, EM_ATENDIMENTO, RESOLVIDO, CANCELADO
```

Isso facilita a leitura dos dados diretamente no banco e evita que a alteração
da ordem de um enum altere o significado dos registros.

---

## 5. Funcionamento de uma requisição

Considere a requisição:

```http
PATCH /chamados/1/assumir
Content-Type: application/json

{
  "usuarioId": 3
}
```

O processamento ocorre assim:

1. O cliente envia a requisição para o `ChamadoController`.
2. O controller lê o ID do chamado e o ID do usuário.
3. O controller chama `ChamadoService.assumir`.
4. O service busca o chamado e o usuário nos repositories.
5. O service verifica se:
   - o usuário existe;
   - é um técnico;
   - possui setor;
   - o setor é o mesmo da categoria;
   - o chamado está `ABERTO`.
6. Se tudo estiver correto, o service altera o técnico e o status.
7. O repository grava a alteração no PostgreSQL.
8. O controller devolve o chamado atualizado com HTTP `200`.
9. Se alguma regra falhar, o service lança uma exceção própria.
10. O `ApiExceptionHandler` transforma a exceção em JSON padronizado.

Essa separação mantém os controllers pequenos e permite que as regras sejam
reutilizadas ou testadas independentemente da camada HTTP.

---

## 6. Endpoints disponíveis

### Setores

| Método | Rota | Finalidade |
|---|---|---|
| `POST` | `/setores` | Criar setor |
| `GET` | `/setores` | Listar setores com filtro por nome |
| `GET` | `/setores/{id}` | Consultar setor |
| `PUT` | `/setores/{id}` | Atualizar setor |
| `DELETE` | `/setores/{id}` | Excluir setor |

### Categorias

| Método | Rota | Finalidade |
|---|---|---|
| `POST` | `/categorias` | Criar categoria |
| `GET` | `/categorias` | Listar com filtros |
| `GET` | `/categorias/{id}` | Consultar categoria |
| `PUT` | `/categorias/{id}` | Atualizar categoria |
| `DELETE` | `/categorias/{id}` | Excluir categoria |
| `GET` | `/setores/{id}/categorias` | Listar categorias de um setor |

### Usuários

| Método | Rota | Finalidade |
|---|---|---|
| `POST` | `/usuarios` | Criar usuário |
| `GET` | `/usuarios` | Listar com filtros |
| `GET` | `/usuarios/{id}` | Consultar usuário |
| `PUT` | `/usuarios/{id}` | Atualizar usuário |
| `GET` | `/usuarios/{id}/chamados` | Consultar chamados do usuário |

### Chamados

| Método | Rota | Finalidade |
|---|---|---|
| `POST` | `/chamados` | Abrir chamado |
| `GET` | `/chamados` | Listar com filtros e paginação |
| `GET` | `/chamados/{id}` | Consultar chamado e interações |
| `PATCH` | `/chamados/{id}/assumir` | Técnico assumir chamado |
| `PATCH` | `/chamados/{id}/resolver` | Resolver chamado |
| `PATCH` | `/chamados/{id}/cancelar` | Cancelar chamado |
| `POST` | `/chamados/{id}/interacoes` | Registrar interação |

---

## 7. Paginação e filtros

As listagens retornam uma estrutura com os itens e metadados:

```json
{
  "itens": [],
  "pagina": 0,
  "tamanho": 10,
  "totalElementos": 0,
  "totalPaginas": 0
}
```

Parâmetros comuns:

- `page`: número da página, iniciando em `0`;
- `size`: quantidade de registros;
- `sort`: campo e direção, por exemplo `dataAbertura,desc`.

Filtros de chamados:

- `status`;
- `prioridade`;
- `setorId`;
- `categoriaId`;
- `solicitanteId`;
- `tecnicoId`;
- `dataInicio`;
- `dataFim`.

Exemplo:

```text
GET /chamados?status=ABERTO&prioridade=ALTA&setorId=1&page=0&size=10&sort=dataAbertura,desc
```

O tamanho máximo de uma página é 50 itens.

---

## 8. Validação e tratamento de erros

Os DTOs utilizam Bean Validation para verificar o formato dos dados:

- campos obrigatórios usam `@NotBlank` ou `@NotNull`;
- e-mails usam `@Email`;
- enumerações são convertidas a partir de seus nomes textuais.

O [ApiExceptionHandler.java](./src/main/java/br/edu/ifrn/chamados/exception/ApiExceptionHandler.java)
converte as falhas em uma resposta uniforme:

```json
{
  "timestamp": "2026-10-19T12:00:00Z",
  "status": 409,
  "mensagem": "Somente o técnico atribuído pode resolver.",
  "caminho": "/chamados/1/resolver"
}
```

Significado dos códigos:

| Código | Significado |
|---:|---|
| `200` | Consulta ou alteração concluída |
| `201` | Registro criado |
| `204` | Registro excluído sem conteúdo de resposta |
| `400` | Dados inválidos |
| `404` | Registro não encontrado |
| `409` | Regra de negócio violada ou conflito |

---

## 9. Banco de dados e migrations

O Hibernate não cria nem altera tabelas. A configuração
`spring.jpa.hibernate.ddl-auto=validate` faz apenas a validação de que as
entidades Java correspondem ao banco.

As alterações são controladas pelo Flyway:

### V1 - criar tabelas

Cria as tabelas, chaves primárias, chaves estrangeiras, restrições de
unicidade e validações dos enums.

### V2 - inserir dados iniciais

Insere:

- 2 setores;
- 4 categorias;
- 4 usuários.

### V3 - popular cenários de teste

Insere:

- usuários adicionais;
- chamados abertos;
- chamados em atendimento;
- chamados resolvidos;
- chamados cancelados;
- interações de histórico.

O Flyway registra as migrations aplicadas na tabela
`flyway_schema_history`. Uma migration já aplicada não deve ser editada. Caso
seja necessário corrigir o banco, crie uma nova versão, como
`V4__corrigir_dados.sql`.

Se o checksum de uma migration aplicada mudar, o Flyway interrompe a
inicialização para evitar que o banco fique diferente do histórico versionado.

---

## 10. Docker e inicialização

O [Dockerfile](./Dockerfile) utiliza dois estágios:

1. imagem Maven com Java 21 para baixar dependências e gerar o `.jar`;
2. imagem JRE 21 menor para executar somente a aplicação compilada.

O [docker-compose.yml](./docker-compose.yml) cria:

- serviço `db`, baseado no PostgreSQL;
- serviço `api`, baseado na imagem construída pelo Dockerfile;
- volume `postgres_data`, que preserva o banco.

A API depende do serviço de banco e recebe a conexão por variáveis de ambiente:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Para recriar completamente o banco local e executar todas as migrations:

```bash
docker compose down -v
docker compose up --build
```

O parâmetro `-v` remove o volume e, consequentemente, apaga os dados locais.
Não use esse comando em um banco que contenha dados importantes.

---

## 11. Cenários de teste disponíveis

Depois de executar as migrations em um banco vazio, a aplicação possui:

| Chamado | Status | Como utilizar |
|---:|---|---|
| 1 | `ABERTO` | Assumir, interagir ou cancelar pelo solicitante 1 |
| 2 | `EM_ATENDIMENTO` | Interagir ou resolver pelo técnico 3 |
| 3 | `RESOLVIDO` | Confirmar bloqueio de novas alterações/interações |
| 4 | `CANCELADO` | Confirmar bloqueio de novas alterações/interações |

Exemplos:

```text
GET http://localhost:8080/chamados/1
GET http://localhost:8080/chamados?status=ABERTO
GET http://localhost:8080/chamados?status=EM_ATENDIMENTO
GET http://localhost:8080/chamados?status=RESOLVIDO
GET http://localhost:8080/chamados?status=CANCELADO
```

Para testar regras de erro:

- tente assumir o chamado `1` com o técnico `4`, que pertence a outro setor;
- tente resolver o chamado `2` usando um técnico diferente do técnico `3`;
- tente cancelar o chamado `2`, que não está mais aberto;
- tente registrar uma interação no chamado `3`;
- tente criar outro usuário com o e-mail já existente.

Esses casos devem produzir respostas HTTP `409`.

---

## 12. Arquivos importantes

- [README.md](./README.md): instalação e roteiro de uso rápido;
- [requisicoes.http](./requisicoes.http): requisições prontas para o VS Code;
- [pom.xml](./pom.xml): dependências e build Maven;
- [Dockerfile](./Dockerfile): imagem da aplicação;
- [docker-compose.yml](./docker-compose.yml): API, banco e volume;
- [application.properties](./src/main/resources/application.properties):
  configurações da aplicação;
- [V1__criar_tabelas.sql](./src/main/resources/db/migration/V1__criar_tabelas.sql):
  estrutura do banco;
- [V2__inserir_dados_iniciais.sql](./src/main/resources/db/migration/V2__inserir_dados_iniciais.sql):
  cadastros iniciais;
- [V3__popular_cenarios_de_teste.sql](./src/main/resources/db/migration/V3__popular_cenarios_de_teste.sql):
  cenários para testes;
- [ChamadoService.java](./src/main/java/br/edu/ifrn/chamados/service/ChamadoService.java):
  regras dos chamados;
- [ApiExceptionHandler.java](./src/main/java/br/edu/ifrn/chamados/exception/ApiExceptionHandler.java):
  tratamento padronizado de erros.
