# Sistema de Chamados IFRN

API REST para registrar e acompanhar chamados de suporte do IFRN. A aplicação foi
desenvolvida com Java 21, Spring Boot 4, Spring Data JPA, PostgreSQL e Flyway.

## 1. Pré-requisitos

Para executar a aplicação pelo caminho recomendado, instale:

- Docker Desktop com o Docker Compose habilitado;
- Git, caso o projeto seja obtido pelo repositório.

Não é necessário instalar Java, Maven ou PostgreSQL na máquina quando a aplicação
é executada pelo Docker Compose.

Ferramentas opcionais para testar a API:

- extensão REST Client do VS Code, usando o arquivo [requisicoes.http](./requisicoes.http);
- Postman, Insomnia ou `curl`.

## 2. Iniciar a aplicação

Abra um terminal na pasta do projeto:

```bash
cd Projeto_API_Sistema_de_Chamados_IFRN
```

Construa a imagem e inicie a API e o banco:

```bash
docker compose up --build
```

Na primeira execução, o Docker baixa as imagens, compila a aplicação no estágio
Maven do [Dockerfile](./Dockerfile) e inicia o PostgreSQL. Aguarde no terminal uma
mensagem indicando que o Spring Boot iniciou. A API estará disponível em:

```text
http://localhost:8080
```

O banco utiliza os seguintes valores no Compose:

| Variável | Valor |
|---|---|
| `DB_URL` | `jdbc:postgresql://db:5432/chamados` |
| `DB_USERNAME` | `chamados` |
| `DB_PASSWORD` | `chamados` |

Esses valores são apenas para o ambiente local de demonstração. Em outro
ambiente, defina `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` sem colocar credenciais
diretamente no código.

Para deixar os containers executando em segundo plano:

```bash
docker compose up --build -d
```

Para acompanhar os logs:

```bash
docker compose logs -f api
```

Se os logs exibirem `Schema-validation: missing table [categorias]` ou outra
tabela das migrations, a aplicação não conseguiu executar o Flyway. Primeiro
reconstrua a imagem sem usar camadas antigas:

```bash
docker compose down
docker compose build --no-cache
docker compose up
```

Se o banco local foi criado com uma execução anterior incompleta, recrie também
o volume de desenvolvimento:

```bash
docker compose down -v
docker compose up --build
```

O comando `down -v` apaga os dados locais e deve ser usado somente em ambiente
de teste. A aplicação utiliza o `spring-boot-starter-flyway`, e as migrations
precisam aparecer nos logs antes da validação do Hibernate.

Se aparecer `Unsupported Database: PostgreSQL`, confirme que a imagem foi
reconstruída depois da alteração do projeto. O suporte específico ao PostgreSQL
é fornecido pela dependência `flyway-database-postgresql` no [pom.xml](./pom.xml).
Depois execute novamente:

```bash
docker compose down
docker compose build --no-cache
docker compose up
```

Se aparecer `No enum constant tools.jackson.databind.SerializationFeature.write-dates-as-timestamps`,
reconstrua a imagem com a versão atual do projeto. Essa propriedade foi removida
porque o Spring Boot 4 utiliza Jackson 3; as datas da API continuam sendo
serializadas no formato ISO-8601 padrão.

Para parar os containers sem apagar os dados:

```bash
docker compose down
```

## 3. Banco e dados iniciais

Ao iniciar, o Flyway executa as migrations em
`src/main/resources/db/migration`. A migration `V1` cria as tabelas, a `V2`
insere os cadastros básicos e a `V3` adiciona os cenários completos de teste.

### Setores

| ID | Nome | Sigla |
|---:|---|---|
| 1 | Coordenação de Tecnologia da Informação | COTIC |
| 2 | Setor de Manutenção | MANUT |

### Categorias

| ID | Nome | Setor |
|---:|---|---:|
| 1 | Computadores | 1 - COTIC |
| 2 | Rede e Wi-Fi | 1 - COTIC |
| 3 | Elétrica | 2 - MANUT |
| 4 | Projetores | 2 - MANUT |

### Usuários

| ID | Nome | Tipo | Setor |
|---:|---|---|---|
| 1 | Ana Solicitante | SOLICITANTE | - |
| 2 | Bruno Solicitante | SOLICITANTE | - |
| 3 | Carlos Técnico | TECNICO | 1 - COTIC |
| 4 | Diana Técnica | TECNICO | 2 - MANUT |
| 5 | Eduardo Solicitante | SOLICITANTE | - |
| 6 | Fernanda Técnica | TECNICO | 1 - COTIC |

### Chamados de teste

A migration `V3` cria automaticamente quatro chamados para que todos os cenários
possam ser testados:

| ID | Título | Prioridade | Status | Solicitante | Técnico |
|---:|---|---|---|---:|---:|
| 1 | Computador não liga | ALTA | ABERTO | 1 | - |
| 2 | Wi-Fi instável | MEDIA | EM_ATENDIMENTO | 2 | 3 |
| 3 | Projetor sem imagem | ALTA | RESOLVIDO | 5 | 4 |
| 4 | Tomada danificada | BAIXA | CANCELADO | 1 | - |

Também são criadas interações nos quatro chamados. Assim é possível testar
listagem, filtros por todos os status e prioridades, consulta do histórico,
interação em chamado aberto ou em atendimento e bloqueio de interação em
chamados encerrados.

Os IDs acima são válidos quando o banco é criado pela primeira vez. Se forem
criados novos registros ou se o volume do PostgreSQL for preservado, consulte os
IDs pela API antes de executar os exemplos. A migration `V3` é executada apenas
uma vez por banco.

## 4. Teste rápido da aplicação

Os comandos abaixo usam curl. No Windows, para requisições com JSON, prefira Invoke-RestMethod conforme a seção 14. O curl.exe -d com JSON entre aspas simples pode resultar em Corpo JSON inválido ou ausente; a alternativa segura está na seção 15.

No PowerShell, não copie os exemplos antigos de curl.exe -d sem adaptar o corpo JSON.

### 4.1 Verificar setores

```bash
curl.exe "http://localhost:8080/setores?page=0&size=10"
```

A resposta é paginada:

```json
{
  "itens": [
    {"id": 1, "nome": "Coordenação de Tecnologia da Informação", "sigla": "COTIC"},
    {"id": 2, "nome": "Setor de Manutenção", "sigla": "MANUT"}
  ],
  "pagina": 0,
  "tamanho": 10,
  "totalElementos": 2,
  "totalPaginas": 1
}
```

### 4.2 Criar um setor

Para buscar um setor por ID:

```bash
curl.exe "http://localhost:8080/setores/1"
```

Para alterar um setor:

Não tente excluir os setores 1 ou 2 durante este roteiro, pois eles possuem
categorias e chamados vinculados. Essa operação deve retornar HTTP `409`.

### 4.3 Consultar categorias

Todas as categorias:

```bash
curl.exe "http://localhost:8080/categorias?page=0&size=10"
```

Categorias do setor COTIC:

```bash
curl.exe "http://localhost:8080/setores/1/categorias?page=0&size=10"
```

Com filtros combinados:

```bash
curl.exe "http://localhost:8080/categorias?nome=rede&setorId=1&page=0&size=10"
```

### 4.4 Consultar usuários

```bash
curl.exe "http://localhost:8080/usuarios?tipo=TECNICO&setorId=1&page=0&size=10"
```

Para criar um solicitante:

O e-mail deve ser único. Repetir o mesmo e-mail retorna HTTP `409`.

## 5. Roteiro completo de um chamado

Este roteiro utiliza:

- solicitante: usuário `1`;
- técnico: usuário `3`;
- categoria: categoria `1`, pertencente ao setor COTIC.

### 5.1 Abrir chamado

Somente usuários `SOLICITANTE` podem abrir chamados. A prioridade pode ser
`BAIXA`, `MEDIA` ou `ALTA`; quando omitida, o sistema usa `MEDIA`. O status
inicial é sempre `ABERTO`.

Guarde o `id` retornado. Nos exemplos seguintes, considere que ele seja `1`.
Uma resposta resumida terá esta estrutura:

```json
{
  "id": 1,
  "titulo": "Computador sem vídeo",
  "prioridade": "ALTA",
  "status": "ABERTO",
  "solicitanteId": 1,
  "categoriaId": 1,
  "setorId": 1,
  "tecnicoId": null,
  "interacoes": []
}
```

### 5.2 Listar chamados

```bash
curl.exe "http://localhost:8080/chamados?page=0&size=10&sort=dataAbertura,desc"
```

Com filtros:

```bash
curl.exe "http://localhost:8080/chamados?status=ABERTO&prioridade=ALTA&setorId=1&categoriaId=1&solicitanteId=1&page=0&size=10&sort=dataAbertura,desc"
```

Também é possível filtrar pelo período de abertura usando data e hora ISO:

```text
GET /chamados?dataInicio=2026-01-01T00:00:00&dataFim=2026-12-31T23:59:59
```

O tamanho máximo da página é 50 itens. Se `size` for maior, a API limita o
valor a 50.

### 5.3 Consultar detalhes e histórico

```bash
curl.exe "http://localhost:8080/chamados/1"
```

Esse endpoint retorna os dados do chamado e todas as interações ordenadas pela
data e hora.

### 5.4 Técnico assumir o chamado

O técnico `3` pertence ao mesmo setor da categoria `1`. A operação altera o
status para `EM_ATENDIMENTO`.

Um técnico de outro setor, como o usuário `4`, deve receber HTTP `409` ao tentar
assumir esse chamado.

### 5.5 Registrar uma interação

Depois de assumido, o técnico atribuído ou o próprio solicitante pode interagir:

Para registrar uma interação como solicitante:

### 5.6 Resolver o chamado

Somente o técnico atribuído pode resolver. A solução é obrigatória e o status
passa para `RESOLVIDO`; nesse momento `dataFechamento` é preenchida.

Depois disso, novas interações ou alterações no chamado devem ser recusadas com
HTTP `409`.

## 6. Testar cancelamento

Para testar o caminho de cancelamento, o chamado `4` já está disponível. Consulte
seu estado:

```bash
curl.exe "http://localhost:8080/chamados/4"
```

Para testar o caminho de cancelamento com um novo registro, abra outro chamado e
não o assuma:

Supondo que o novo chamado tenha ID `2`, o solicitante `2` pode cancelá-lo:

O status será `CANCELADO`. O solicitante `1`, outro usuário ou um chamado que já
não esteja `ABERTO` não pode executar essa operação.

## 7. Chamados de um usuário

Para listar os chamados abertos por um solicitante ou atribuídos a um técnico:

```bash
curl.exe "http://localhost:8080/usuarios/1/chamados?page=0&size=10"
```

Com filtro de status:

```bash
curl.exe "http://localhost:8080/usuarios/3/chamados?status=EM_ATENDIMENTO&page=0&size=10"
```

## 8. Estados e regras do chamado

As transições permitidas são:

```text
ABERTO -> EM_ATENDIMENTO -> RESOLVIDO
ABERTO -> CANCELADO
```

As enumerações aceitas são:

- prioridade: `BAIXA`, `MEDIA`, `ALTA`;
- status: `ABERTO`, `EM_ATENDIMENTO`, `RESOLVIDO`, `CANCELADO`;
- tipo de usuário: `SOLICITANTE`, `TECNICO`.

Regras importantes:

1. Campos obrigatórios vazios ou e-mail inválido retornam HTTP `400`.
2. Recurso inexistente retorna HTTP `404`.
3. Violação de regra de negócio retorna HTTP `409`.
4. Chamados resolvidos ou cancelados não aceitam novas interações.
5. Apenas solicitantes abrem chamados.
6. Apenas o técnico atribuído resolve o chamado.
7. Um setor ou categoria com chamados vinculados não pode ser excluído.

Os erros seguem o formato:

```json
{
  "timestamp": "2026-10-19T12:00:00Z",
  "status": 409,
  "mensagem": "Somente o técnico atribuído pode resolver.",
  "caminho": "/chamados/1/resolver"
}
```

## 9. Testar pelo VS Code, Postman ou Insomnia

1. Inicie a aplicação com `docker compose up --build`.
2. Abra [requisicoes.http](./requisicoes.http).
3. Execute primeiro as consultas de setores, categorias e usuários.
4. Execute o `POST /chamados`.
5. Substitua o ID do chamado nos exemplos seguintes, se necessário.
6. Execute assumir, interação, resolver e consulta de detalhes.
7. Para testar cancelamento, abra um segundo chamado e use o solicitante antes
   de atribuí-lo a um técnico.

No Postman ou Insomnia, use `Content-Type: application/json` nos métodos
`POST`, `PUT` e `PATCH`, e copie os mesmos corpos JSON apresentados neste guia.

## 10. Testar pelo navegador

O navegador consegue executar diretamente requisições `GET`. Portanto, use a
barra de endereços para consultar setores, categorias, usuários e chamados.
Antes de começar, confirme que a aplicação está em execução:

```text
http://localhost:8080
```

Como a API não possui uma página HTML inicial, é normal que essa URL retorne
erro ou uma resposta vazia. Para testar os endpoints, abra as URLs abaixo em
novas abas:

### Consultar setores

```text
http://localhost:8080/setores?page=0&size=10
```

Filtrar setores pelo nome:

```text
http://localhost:8080/setores?nome=tecnologia&page=0&size=10
```

Consultar um setor específico:

```text
http://localhost:8080/setores/1
```

### Consultar categorias

Todas as categorias:

```text
http://localhost:8080/categorias?page=0&size=10
```

Categorias do setor COTIC:

```text
http://localhost:8080/setores/1/categorias?page=0&size=10
```

Filtrar por nome e setor:

```text
http://localhost:8080/categorias?nome=rede&setorId=1&page=0&size=10
```

Consultar uma categoria específica:

```text
http://localhost:8080/categorias/1
```

### Consultar usuários

Todos os usuários:

```text
http://localhost:8080/usuarios?page=0&size=10
```

Somente técnicos do setor COTIC:

```text
http://localhost:8080/usuarios?tipo=TECNICO&setorId=1&page=0&size=10
```

Consultar um usuário específico:

```text
http://localhost:8080/usuarios/3
```

### Consultar chamados

Todos os chamados:

```text
http://localhost:8080/chamados?page=0&size=10&sort=dataAbertura,desc
```

Somente chamados abertos e de alta prioridade:

```text
http://localhost:8080/chamados?status=ABERTO&prioridade=ALTA&page=0&size=10
```

Consultar um chamado específico:

```text
http://localhost:8080/chamados/1
```

Consultar os chamados de um solicitante ou técnico:

```text
http://localhost:8080/usuarios/1/chamados?page=0&size=10
```

Filtrar os chamados de um técnico pelo status:

```text
http://localhost:8080/usuarios/3/chamados?status=EM_ATENDIMENTO&page=0&size=10
```

### Verificar o resultado no navegador

1. Abra uma das URLs `GET` acima.
2. O navegador exibirá a resposta JSON.
3. Procure os campos `itens`, `pagina`, `tamanho`, `totalElementos` e
   `totalPaginas` nas respostas de listagem.
4. Na resposta de um chamado, confira `status`, `prioridade`, `dataAbertura`,
   `tecnicoId` e `interacoes`.
5. Após executar uma alteração pelo Postman, pelo REST Client ou pelo `curl`,
   atualize a aba do navegador para consultar o novo estado.

### Limitação dos testes pela barra de endereços

A barra de endereços envia apenas requisições `GET`. Ela não permite, de forma
prática, testar `POST`, `PUT`, `PATCH` ou enviar os corpos JSON necessários para
abrir, assumir, resolver, cancelar chamados e registrar interações.

Para esses métodos, use uma destas opções:

- o arquivo [requisicoes.http](./requisicoes.http) no VS Code;
- Postman ou Insomnia;
- `curl.exe` no PowerShell, conforme os exemplos deste README.

Depois de executar a operação, volte ao navegador e atualize a URL `GET` do
recurso para confirmar o resultado.

## 11. Reiniciar o banco do zero

As migrations executadas ficam registradas em `flyway_schema_history` e o volume
preserva os dados entre reinicializações. Para apagar os dados locais e recriar
todas as tabelas e registros iniciais:

```bash
docker compose down -v
docker compose up --build
```

Use `down -v` somente quando quiser realmente perder os dados do ambiente local.

## 12. Migrations e produção

O Flyway registra cada migration aplicada em `flyway_schema_history`, incluindo
seu checksum. Alterar uma migration já aplicada muda o checksum e faz a
inicialização falhar, protegendo a integridade do histórico. Correções devem ser
adicionadas em uma nova versão, por exemplo `V3__corrigir_dados.sql`.

O projeto usa `spring.jpa.hibernate.ddl-auto=validate` para o Hibernate apenas
validar o esquema criado pelo Flyway. `ddl-auto=update` não é usado em produção
porque pode alterar o banco de forma implícita, sem revisão, sem histórico de
versionamento e sem uma estratégia segura de rollback.

## 13. Referência completa por sistema operacional

Os exemplos anteriores estão em sintaxe de PowerShell. Esta seção reúne os comandos equivalentes para testar todas as operações no PowerShell e no Linux.

### Preparação

PowerShell:

```powershell
$BASE_URL = "http://localhost:8080"
docker compose up --build -d
docker compose logs -f api
```

Linux:

```bash
BASE_URL="http://localhost:8080"
docker compose up --build -d
docker compose logs -f api
```

### Setores: listar, filtrar, consultar, criar, alterar e excluir

PowerShell:

Linux:

### Categorias: listar, filtrar, consultar, criar, alterar e excluir

PowerShell:

Linux:

Troque `5` pelo ID devolvido pelo `POST`. Setores ou categorias vinculados a chamados retornam `409` ao excluir.

### Usuários: listar, filtrar, consultar, criar e alterar

PowerShell:

Linux:

O e-mail deve ser único; um técnico precisa de `setorId`. Não existe `DELETE /usuarios` na API.

### Chamados: consultar e filtrar

PowerShell:

Linux:


### Testes negativos

PowerShell:

Linux:

```bash
curl -i -X POST "$BASE_URL/setores" -H 'Content-Type: application/json' -d '{"nome":""}' # 400
curl -i "$BASE_URL/chamados/99999" # 404
curl -i -X POST "$BASE_URL/setores" -H 'Content-Type: application/json' -d '{"nome":"Outro COTIC","sigla":"COTIC"}' # 409
curl -i -X POST "$BASE_URL/usuarios" -H 'Content-Type: application/json' -d '{"nome":"Duplicado","email":"ana@ifrn.edu.br","matricula":"99999999","tipo":"SOLICITANTE"}' # 409
curl -i -X POST "$BASE_URL/usuarios" -H 'Content-Type: application/json' -d '{"nome":"Sem setor","email":"semsetor@ifrn.edu.br","matricula":"99999998","tipo":"TECNICO"}' # 409
```

Regras cobertas: `ABERTO -> EM_ATENDIMENTO -> RESOLVIDO`, `ABERTO -> CANCELADO`, solicitante obrigatório para abertura, técnico do setor correto para assumir, técnico atribuído para resolver e bloqueio de interação em chamados encerrados.

## 14. PowerShell: envio correto de JSON

No PowerShell, prefira `Invoke-RestMethod` para enviar corpos JSON. Ele evita problemas de aspas e de conversão de argumentos do `curl.exe` para processos nativos.

Defina a URL e crie um auxiliar:

```powershell
$BASE_URL = "http://localhost:8080"

function Invoke-ApiJson {
  param(
    [string]$Method,
    [string]$Uri,
    [hashtable]$Body
  )
  $json = $Body | ConvertTo-Json -Compress
  Invoke-RestMethod -Method $Method -Uri $Uri -ContentType "application/json; charset=utf-8" -Body $json
}
```

Use os comandos abaixo com IDs numéricos reais. O `POST` devolve o objeto criado;
guarde o ID antes de chamar `PUT`, `PATCH` ou `DELETE`.

```powershell
$siglaSetor = "BIB$((Get-Date).ToString('HHmmss'))"
$setor = Invoke-ApiJson POST "$BASE_URL/setores" @{ nome = "Biblioteca"; sigla = $siglaSetor }
$setor | ConvertTo-Json
$setorId = $setor.id

Invoke-ApiJson PUT "$BASE_URL/setores/$setorId" @{ nome = "Biblioteca Central"; sigla = $siglaSetor }
Invoke-RestMethod -Method Delete -Uri "$BASE_URL/setores/$setorId"

$categoria = Invoke-ApiJson POST "$BASE_URL/categorias" @{ nome = "Impressoras"; descricao = "Falhas em impressoras"; setorId = 1 }
$categoriaId = $categoria.id
Invoke-ApiJson PUT "$BASE_URL/categorias/$categoriaId" @{ nome = "Impressoras e scanners"; descricao = "Equipamentos de impressão"; setorId = 1 }
Invoke-RestMethod -Method Delete -Uri "$BASE_URL/categorias/$categoriaId"

$usuario = Invoke-ApiJson POST "$BASE_URL/usuarios" @{ nome = "João da Silva"; email = "joao@ifrn.edu.br"; matricula = "20260003"; tipo = "SOLICITANTE" }
$usuarioId = $usuario.id
Invoke-ApiJson PUT "$BASE_URL/usuarios/$usuarioId" @{ nome = "João Atualizado"; email = "joao@ifrn.edu.br"; matricula = "20260003"; tipo = "SOLICITANTE" }

$chamado = Invoke-ApiJson POST "$BASE_URL/chamados" @{ titulo = "Computador sem vídeo"; descricao = "O monitor não liga"; local = "Laboratório 2"; prioridade = "ALTA"; solicitanteId = 1; categoriaId = 1 }
$chamadoId = $chamado.id

Invoke-ApiJson PATCH "$BASE_URL/chamados/$chamadoId/assumir" @{ usuarioId = 3 }
Invoke-ApiJson POST "$BASE_URL/chamados/$chamadoId/interacoes" @{ texto = "Estou verificando o equipamento."; autorId = 3 }
Invoke-ApiJson POST "$BASE_URL/chamados/$chamadoId/interacoes" @{ texto = "O problema continua."; autorId = 1 }
Invoke-ApiJson PATCH "$BASE_URL/chamados/$chamadoId/resolver" @{ tecnicoId = 3; solucao = "Cabo substituído e equipamento testado." }
Invoke-RestMethod -Uri "$BASE_URL/chamados/$chamadoId"
```

Para testar cancelamento, abra um novo chamado com `solicitanteId = 2`, não o
assuma e execute:

```powershell
$cancelavel = Invoke-ApiJson POST "$BASE_URL/chamados" @{ titulo = "Teste de cancelamento"; descricao = "Chamado temporário"; local = "Sala 1"; solicitanteId = 2; categoriaId = 2 }
Invoke-ApiJson PATCH "$BASE_URL/chamados/$($cancelavel.id)/cancelar" @{ usuarioId = 2 }
```

Com `curl.exe`, o JSON também precisa ser escapado no PowerShell:

No Linux, os comandos `curl` da seção anterior continuam válidos sem esse
escape adicional.

## 15. Importante: `curl.exe -d` no Windows PowerShell

No Windows PowerShell, não use JSON entre aspas simples diretamente em `-d`, por exemplo:

O formato recomendado para PowerShell é `Invoke-RestMethod`, conforme a seção
anterior:

```powershell
$siglaSetor = "BIB$((Get-Date).ToString('HHmmss'))"
$body = @{ nome = "Biblioteca"; sigla = $siglaSetor } | ConvertTo-Json -Compress
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/setores" `
  -ContentType "application/json; charset=utf-8" `
  -Body $body
```

Se for necessário usar `curl.exe`, grave o JSON em um arquivo UTF-8 e envie o
arquivo. Isso evita a conversão de aspas feita pelo PowerShell:

```powershell
$jsonFile = Join-Path $env:TEMP "setor-teste.json"
$siglaSetor = "BIB$((Get-Date).ToString('HHmmss'))"
$body = @{ nome = "Biblioteca"; sigla = $siglaSetor } | ConvertTo-Json -Compress
[System.IO.File]::WriteAllText($jsonFile, $body, [System.Text.UTF8Encoding]::new($false))

curl.exe -i -X POST "http://localhost:8080/setores" `
  -H "Content-Type: application/json; charset=utf-8" `
  --data-binary "@$jsonFile"

Remove-Item -LiteralPath $jsonFile
```

No Linux, gere uma sigla única para não repetir um cadastro existente:

```bash
SIGLA="BIB$(date +%s)"
curl -i -X POST "$BASE_URL/setores" \
  -H 'Content-Type: application/json' \
  -d "{\"nome\":\"Biblioteca\",\"sigla\":\"$SIGLA\"}"
```

## 16. Coleção do Insomnia

A coleção pronta está em [insomnia-collection.json](./insomnia-collection.json).

Para importar no Insomnia:

1. Abra o Insomnia.
2. Selecione **Import/Export** e depois **Import Data**.
3. Escolha **From File**.
4. Selecione `insomnia-collection.json`.
5. Abra o workspace **Sistema de Chamados IFRN**.
6. Confirme o ambiente **Base Environment**.

O ambiente já vem configurado com:

- `base_url`: `http://localhost:8080`;
- `setor_id`: `1`;
- `categoria_id`: `1`;
- `solicitante_id`: `1`;
- `tecnico_id`: `3`;
- `chamado_id`: `1`;
- `test_suffix`: `001`.

Se executar novamente os requests de criação, altere `test_suffix` no ambiente
para outro valor, como `002`, para evitar e-mails, siglas ou matrículas
repetidos.

Ordem recomendada para o roteiro de chamados:

1. `Listar setores`, `Listar categorias` e `Listar usuarios`;
2. `Abrir chamado`;
3. Copiar o ID retornado para `chamado_id` no ambiente;
4. `Assumir chamado`;
5. `Interacao do tecnico` e `Interacao do solicitante`;
6. `Resolver chamado`;
7. Consultar novamente o chamado.

Para testar cancelamento, use um chamado que ainda esteja `ABERTO` e execute
`Cancelar chamado` antes de assumir ou resolver. Os requests da pasta **Testes
negativos** devem retornar `400`, `404` ou `409` conforme o nome.
## 17. Swagger / OpenAPI

A especificação completa para Swagger está em [openapi.yaml](./openapi.yaml).

### Usar no Swagger Editor ou Swagger UI

1. Inicie a aplicação com `docker compose up --build -d`.
2. Abra o [Swagger Editor](https://editor.swagger.io/).
3. Use **File > Import File** e selecione `openapi.yaml`.
4. Na documentação carregada, confirme que o servidor está como
   `http://localhost:8080`.
5. Execute os endpoints pelo botão **Try it out**.

A especificação cobre todos os endpoints da API, incluindo CRUD de setores e
categorias, usuários, filtros, paginação, abertura de chamados, assumir,
interações, resolução, cancelamento e respostas de erro `400`, `404` e `409`.

O fluxo recomendado é:

1. Consultar setores, categorias e usuários;
2. Executar `POST /chamados` com um solicitante e uma categoria válidos;
3. Copiar o ID retornado;
4. Executar `PATCH /chamados/{id}/assumir`;
5. Registrar interações;
6. Executar resolver ou cancelar conforme o cenário.

O arquivo pode ser usado também em uma instalação local do Swagger UI,
configurando o parâmetro `url` para apontar para `openapi.yaml`.
## 18. Roteiro completo de testes no Swagger

Esta seção mostra exatamente como executar os exemplos pelo Swagger Editor ou
por uma instalação local do Swagger UI.

### 18.1 Iniciar a API

No PowerShell ou no Linux, execute na pasta do projeto:

```text
docker compose up --build -d
docker compose ps
```

Confirme que o serviço `api` está com status `Up`. A API deve responder em:

```text
http://localhost:8080
```

### 18.2 Importar a especificação

1. Abra [Swagger Editor](https://editor.swagger.io/).
2. Selecione **File > Import File**.
3. Escolha o arquivo [openapi.yaml](./openapi.yaml).
4. Aguarde a documentação ser renderizada no painel direito.
5. Confirme que o servidor selecionado é `http://localhost:8080`.
6. Se o navegador perguntar sobre conteúdo inseguro, permita o acesso ao
   servidor local ou abra o editor em uma instalação local.

A documentação apresenta os grupos **Setores**, **Categorias**, **Usuarios**,
**Chamados** e **Chamados por usuario**.

### 18.3 Primeiro teste: consultar dados iniciais

Abra **Setores > GET /setores**, clique em **Try it out** e use:

```text
page: 0
size: 10
```

Clique em **Execute**. A resposta esperada é `200` e contém `itens`,
`pagina`, `tamanho`, `totalElementos` e `totalPaginas`.

Repita o procedimento para:

```text
GET /categorias?page=0&size=10
GET /usuarios?page=0&size=10
GET /chamados?page=0&size=10&sort=dataAbertura,desc
```

Para consultar somente técnicos da COTIC, use:

```text
GET /usuarios?tipo=TECNICO&setorId=1&page=0&size=10
```

### 18.4 Criar e consultar um setor

Abra **Setores > POST /setores**, clique em **Try it out** e substitua o corpo
pelo exemplo abaixo. Use uma sigla ainda não cadastrada:

```json
{
  "nome": "Biblioteca Swagger",
  "sigla": "SWG"
}
```

Clique em **Execute**. A resposta esperada é `200`. Copie o campo `id` retornado.

Para consultar o registro, abra **GET /setores/{id}**, clique em **Try it out**,
informe o ID copiado e execute.

Para atualizar, use **PUT /setores/{id}** com:

```json
{
  "nome": "Biblioteca Swagger Atualizada",
  "sigla": "SWG"
}
```

Não use os IDs `3`, `5` ou `7` automaticamente. Eles podem não existir no seu
banco. Sempre use o ID devolvido pela API ou consulte a listagem primeiro.

### 18.5 Criar uma categoria

Abra **Categorias > POST /categorias** e use uma categoria ainda não cadastrada:

```json
{
  "nome": "Impressoras Swagger",
  "descricao": "Teste realizado pelo Swagger",
  "setorId": 1
}
```

A resposta esperada é `200`. Copie o ID retornado e teste:

```text
GET /categorias/{id}
PUT /categorias/{id}
DELETE /categorias/{id}
```

Não exclua categorias que já estejam vinculadas a chamados; nesse caso a API
retorna `409` por regra de negócio.

### 18.6 Criar um usuário

Em **Usuarios > POST /usuarios**, use um e-mail e uma matrícula que ainda não
existam:

```json
{
  "nome": "Solicitante Swagger",
  "email": "swagger001@ifrn.edu.br",
  "matricula": "SWG001",
  "tipo": "SOLICITANTE"
}
```

Para criar um técnico, o `setorId` é obrigatório:

```json
{
  "nome": "Tecnico Swagger",
  "email": "tecnico.swagger001@ifrn.edu.br",
  "matricula": "SWGTEC001",
  "tipo": "TECNICO",
  "setorId": 1
}
```

Cada e-mail deve ser único. Se repetir um e-mail já existente, a resposta
esperada é `409`.

### 18.7 Fluxo completo de um chamado

Use os dados iniciais abaixo ou confirme os IDs pela listagem:

```text
solicitanteId: 1
categoriaId: 1
tecnicoId: 3
```

#### Abrir chamado

Em **Chamados > POST /chamados**, use:

```json
{
  "titulo": "Computador sem video - Swagger",
  "descricao": "O monitor do laboratorio nao liga",
  "local": "Laboratorio 2",
  "prioridade": "ALTA",
  "solicitanteId": 1,
  "categoriaId": 1
}
```

A resposta esperada é `201 Created`. Copie o `id` retornado. Esse é o número que
deve ser informado no campo `id` dos próximos endpoints.

#### Assumir chamado

Em **PATCH /chamados/{id}/assumir**, informe o ID copiado e use:

```json
{
  "usuarioId": 3
}
```

A resposta deve retornar `200` e o status `EM_ATENDIMENTO`.

#### Registrar interação

Em **POST /chamados/{id}/interacoes**, use:

```json
{
  "texto": "Estou verificando o equipamento pelo Swagger.",
  "autorId": 3
}
```

A resposta esperada é `201 Created`.

O solicitante também pode interagir:

```json
{
  "texto": "O problema continua acontecendo.",
  "autorId": 1
}
```

#### Resolver chamado

Em **PATCH /chamados/{id}/resolver**, use:

```json
{
  "tecnicoId": 3,
  "solucao": "Cabo de video substituido e monitor testado."
}
```

A resposta deve retornar `200`, status `RESOLVIDO` e `dataFechamento` preenchida.

### 18.8 Testar cancelamento

Abra outro chamado, mas não execute o endpoint de assumir:

```json
{
  "titulo": "Teste de cancelamento Swagger",
  "descricao": "Chamado criado para validar cancelamento",
  "local": "Sala 1",
  "prioridade": "MEDIA",
  "solicitanteId": 2,
  "categoriaId": 2
}
```

Copie o ID retornado e execute **PATCH /chamados/{id}/cancelar**:

```json
{
  "usuarioId": 2
}
```

O status esperado é `CANCELADO`. Se o chamado já tiver sido assumido, a API
retorna `409`.

### 18.9 Testar respostas de erro

Na pasta de testes negativos, ou diretamente pelos endpoints, execute:

| Cenário | Requisição | Resultado esperado |
|---|---|---:|
| Campo obrigatório vazio | `POST /setores` com `nome` vazio | `400` |
| ID inexistente | `GET /chamados/99999` | `404` |
| Sigla duplicada | `POST /setores` com `sigla: COTIC` | `409` |
| Técnico sem setor | `POST /usuarios` com `tipo: TECNICO` sem `setorId` | `409` |
| Técnico de setor incorreto | `PATCH /chamados/{id}/assumir` com `usuarioId: 4` em categoria do setor 1 | `409` |
| Interação em chamado encerrado | `POST /chamados/{id}/interacoes` após resolver | `409` |

A resposta de erro possui o formato:

```json
{
  "timestamp": "2026-10-28T18:00:00Z",
  "status": 409,
  "mensagem": "Regra de negocio violada.",
  "caminho": "/chamados/1/resolver"
}
```

### 18.10 Problemas comuns

- **Erro de conexão:** confirme `docker compose ps` e se a API está na porta
  `8080`.
- **Resposta `404`:** consulte a listagem e use um ID existente.
- **Resposta `409`:** o recurso existe, mas a regra de negócio impede a operação;
  não é erro do Swagger.
- **Resposta `400` ao enviar JSON:** confira se o corpo está no modo
  `application/json` e se todos os campos obrigatórios estão preenchidos.
- **CORS no Swagger Editor online:** use Swagger UI local ou importe a
  especificação no Insomnia/Postman.
## 19. Swagger Editor: erro `Failed to fetch`

A API possui CORS configurado para permitir o Swagger Editor
(`https://editor.swagger.io`) e interfaces locais nas portas comuns de
 desenvolvimento.

Depois de alterar a configuração, reconstrua e reinicie a API:

```powershell
docker compose down
docker compose up --build -d
docker compose logs -f api
```

Teste no PowerShell se a resposta contém o cabeçalho:

```powershell
curl.exe -i `
  -H "Origin: https://editor.swagger.io" `
  "http://localhost:8080/setores?page=0&size=10"
```

A resposta deve conter:

```text
Access-Control-Allow-Origin: https://editor.swagger.io
```

Se o Swagger Editor online continuar exibindo `Failed to fetch`, use uma
instalação local do Swagger UI ou execute a especificação no Swagger Editor
com o navegador permitindo acesso a `http://localhost:8080`. Também confirme
que a API está em execução e que o endereço do servidor no OpenAPI é exatamente
`http://localhost:8080`.
## 20. Coleção Postman para Newman

A coleção automatizada está em
[postman-collection.json](./postman-collection.json). Ela é compatível com
Postman e Newman v6 ou superior.

A coleção possui cinco grupos:

- Setores;
- Categorias;
- Usuários;
- Chamados;
- Testes negativos.

Os scripts da coleção capturam automaticamente os IDs criados durante a
execução (`created_setor_id`, `created_categoria_id`, `created_usuario_id`,
`chamado_id` e `cancel_id`). O sufixo de teste é gerado automaticamente para
evitar duplicidade de siglas, e-mails e matrículas.

### Instalar o Newman

Com Node.js instalado:

```powershell
npm install -g newman
newman --version
```

No Linux, os mesmos comandos funcionam:

```bash
npm install -g newman
newman --version
```

### Executar a coleção

Primeiro inicie a API:

```text
docker compose up --build -d
```

Depois execute a coleção:

```powershell
newman run .\postman-collection.json --reporters cli
```

Linux:

```bash
newman run ./postman-collection.json --reporters cli
```

Para informar outra URL:

```powershell
newman run .\postman-collection.json `
  --env-var "base_url=http://localhost:8080" `
  --reporters cli
```

Linux:

```bash
newman run ./postman-collection.json \
  --env-var 'base_url=http://localhost:8080' \
  --reporters cli
```

### Executar com relatório HTML

Instale o reporter uma vez:

```text
npm install -g newman-reporter-htmlextra
```

Execute:

```powershell
newman run .\postman-collection.json `
  --reporters cli,htmlextra `
  --reporter-htmlextra-export .\newman-report.html
```

O arquivo `newman-report.html` será criado na pasta do projeto.

### Ordem dos testes

A coleção deve ser executada do início ao fim para que os scripts capturem os
IDs automaticamente. O roteiro principal é:

1. Consultar os cadastros iniciais;
2. Criar, atualizar e excluir setor de teste;
3. Criar, atualizar e excluir categoria de teste;
4. Criar e atualizar usuário de teste;
5. Abrir chamado;
6. Assumir o chamado;
7. Registrar interações;
8. Resolver o chamado;
9. Abrir e cancelar outro chamado;
10. Validar respostas `400`, `404` e `409`.

Para executar apenas uma pasta:

```powershell
newman run .\postman-collection.json --folder "04 - Chamados"
```

Os testes negativos retornam códigos de erro propositalmente. Isso não indica
falha da coleção: eles verificam se a API rejeita corretamente requisições
inválidas.
