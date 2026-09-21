# Sistema de Biblioteca — Spring Boot (JPA)

Projeto desenvolvido para a disciplina de **Desenvolvimento de Sistemas Corporativos (2026.2)** do
**IFRN** - Curso Superior de Tecnologia em Análise e Desenvolvimento de Sistemas.
**Professor:** Jeferson Queiroga

---

## 🎯 Objetivo da Atividade

Implementar um projeto **Spring Boot** configurando corretamente as camadas **Model** e **Repository**
com base no domínio do **Sistema de Biblioteca** e nas **consultas solicitadas** no enunciado.

---

## 🚀 Tecnologias Utilizadas

- **Java 21 (LTS)**
- **Spring Boot 4.1.1**
- **Spring Data JPA / Hibernate**
- **Banco H2** em memória (com console web em `/h2-console`)
- **Maven Wrapper** (`mvnw`, `mvnw.cmd`)
- **JUnit 5 + AssertJ** (testes de integração das consultas)

---

## 🗂️ Estrutura de Pacotes

```
br.edu.ifrn.biblioteca
├── BibliotecaApplication.java            # Classe principal
├── model/                                 # Camada Model (entidades JPA)
│   ├── Categoria.java
│   ├── Autor.java
│   ├── Livro.java
│   ├── Usuario.java
│   ├── Emprestimo.java
│   ├── ItemEmprestimo.java
│   └── EmprestimoStatus.java              # enum (ATIVO, DEVOLVIDO, ATRASADO)
├── repository/                            # Camada Repository (Spring Data)
│   ├── CategoriaRepository.java
│   ├── AutorRepository.java
│   ├── LivroRepository.java
│   ├── UsuarioRepository.java
│   ├── EmprestimoRepository.java
│   ├── ItemEmprestimoRepository.java
│   └── CategoriaQtd.java                  # projeção da Consulta 7
└── config/
    └── DataSeeder.java                    # popula dados de exemplo
```

---

## 📐 Modelo de Dados (mapeamento JPA)

| Entidade        | Relacionamentos |
|-----------------|-----------------|
| **LIVRO**       | N:1 com `CATEGORIA`; N:N com `AUTOR` via tabela associativa `livro_autor`; 1:N com `ITEM_EMPRESTIMO` |
| **AUTOR**       | N:N com `LIVRO` (mappedBy) |
| **CATEGORIA**   | 1:N com `LIVRO` |
| **USUARIO**     | 1:N com `EMPRESTIMO` |
| **EMPRESTIMO**  | N:1 com `USUARIO`; 1:N com `ITEM_EMPRESTIMO`; `status` como `ENUM` (STRING) |
| **ITEM_EMPRESTIMO** | N:1 com `EMPRESTIMO`; N:1 com `LIVRO` |

A relação **LIVRO ⟷ AUTOR** (N:N, "tabela associativa" `livro_autor` com PK composta `livro_id` + `autor_id`)
é mapeada com `@ManyToMany` + `@JoinTable`. A relação **EMPRESTIMO ⟷ LIVRO** (N:N) é mapeada através da
entidade associativa `ITEM_EMPRESTIMO` (um préstamo pode conter vários livros).

---

## 🔍 Consultas Implementadas

| # | Consulta | Método do Repository |
|---|----------|----------------------|
| 1 | Listar todos os livros disponíveis (`quantidadeDisponivel > 0`), ordenados por título | `LivroRepository.findByQuantidadeDisponivelGreaterThanOrderByTituloAsc` |
| 2 | Buscar livros por categoria (nome da categoria) | `LivroRepository.findByCategoria_NomeIgnoreCaseOrderByTituloAsc` |
| 3 | Buscar usuários por nome (busca parcial, ex.: "Silva") | `UsuarioRepository.findByNomeContainingIgnoreCase` |
| 4 | Listar empréstimos ativos de um usuário (com livros) | `EmprestimoRepository.findEmprestimosAtivosPorUsuario` (JPQL com `join fetch`) |
| 5 | Encontrar livros de um autor específico (ordenados por ano) | `LivroRepository.findLivrosPorNomeAutor` (JPQL) |
| 6 | Listar empréstimos atrasados (`dataDevolucaoPrevista < hoje` e status ATIVO) | `EmprestimoRepository.findEmprestimosAtrasados` (JPQL com `join fetch`) |
| 7 | Contar quantidade de livros por categoria (ordem decrescente) | `LivroRepository.contarQuantidadeLivrosPorCategoria` (JPQL de agregação) |

### Exemplos de uso
```java
livroRepository.findByQuantidadeDisponivelGreaterThanOrderByTituloAsc(0);

usuarioRepository.findByNomeContainingIgnoreCase("Silva");

emprestimoRepository.findEmprestimosAtivosPorUsuario(usuarioId);

emprestimoRepository.findEmprestimosAtrasados(LocalDate.now());

livroRepository.contarQuantidadeLivrosPorCategoria();
```

### Consultas SQL para o H2 Console

Com a aplicação em execução, acesse `http://localhost:8080/h2-console` e conecte usando:

- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Usuário:** `sa`
- **Senha:** *(vazia)*

As consultas abaixo usam os nomes das tabelas e colunas criados pelo mapeamento JPA.

#### 1. Listar todos os livros disponíveis

Retorna os livros com quantidade disponível maior que zero, ordenados pelo título.

```sql
SELECT *
FROM livro
WHERE quantidade_disponivel > 0
ORDER BY titulo;
```

#### 2. Buscar livros por categoria

Altere o valor de `c.nome` para consultar outra categoria.

```sql
SELECT l.id,
             l.isbn,
             l.titulo,
             l.editora,
             l.ano_publicacao,
             l.numero_paginas,
             l.quantidade_total,
             l.quantidade_disponivel,
             c.nome AS categoria
FROM livro l
JOIN categoria c ON c.id = l.categoria_id
WHERE UPPER(c.nome) = UPPER('Ficção')
ORDER BY l.titulo;
```

#### 3. Buscar usuários por nome

A busca parcial abaixo encontra, por exemplo, os usuários que possuem `Silva` no nome.

```sql
SELECT *
FROM usuario
WHERE UPPER(nome) LIKE UPPER('%Silva%')
ORDER BY nome;
```

#### 4. Listar empréstimos ativos de um usuário

O exemplo localiza o usuário pelo CPF e lista os livros dos empréstimos com status `ATIVO`.

```sql
SELECT e.id AS emprestimo_id,
             u.nome AS usuario,
             e.data_emprestimo,
             e.data_devolucao_prevista,
             e.status,
             l.titulo AS livro
FROM emprestimo e
JOIN usuario u ON u.id = e.usuario_id
JOIN item_emprestimo ie ON ie.emprestimo_id = e.id
JOIN livro l ON l.id = ie.livro_id
WHERE e.status = 'ATIVO'
    AND u.cpf = '111.111.111-11'
ORDER BY e.data_devolucao_prevista, l.titulo;
```

#### 5. Encontrar livros de um autor

Altere o texto de `a.nome` para consultar outro autor. O resultado é ordenado pelo ano de publicação.

```sql
SELECT DISTINCT l.id,
                                l.titulo,
                                l.editora,
                                l.ano_publicacao,
                                a.nome AS autor
FROM livro l
JOIN livro_autor la ON la.livro_id = l.id
JOIN autor a ON a.id = la.autor_id
WHERE UPPER(a.nome) LIKE UPPER('%Assis%')
ORDER BY l.ano_publicacao, l.titulo;
```

#### 6. Listar empréstimos atrasados

Considera atrasado o empréstimo ainda `ATIVO` cuja data prevista já passou. A consulta também exibe o usuário e os livros.

```sql
SELECT e.id AS emprestimo_id,
             u.nome AS usuario,
             u.cpf,
             e.data_devolucao_prevista,
             e.status,
             l.titulo AS livro
FROM emprestimo e
JOIN usuario u ON u.id = e.usuario_id
JOIN item_emprestimo ie ON ie.emprestimo_id = e.id
JOIN livro l ON l.id = ie.livro_id
WHERE e.status = 'ATIVO'
    AND e.data_devolucao_prevista < CURRENT_DATE
ORDER BY e.data_devolucao_prevista, u.nome, l.titulo;
```

#### 7. Contar livros por categoria

O `LEFT JOIN` mantém no resultado categorias que ainda não possuem livros.

```sql
SELECT c.nome AS categoria,
             COUNT(l.id) AS quantidade_livros
FROM categoria c
LEFT JOIN livro l ON l.categoria_id = c.id
GROUP BY c.id, c.nome
ORDER BY quantidade_livros DESC, c.nome;
```

---

## ⚙️ Dados de Exemplo (seed automático)

Ao iniciar, a aplicação popula via `DataSeeder`:

- 3 categorias (Ficção, Romance, Ciência)
- 4 autores (Machado de Assis, Clarice Lispector, J.R.R. Tolkien, George R.R. Martin)
- 5 livros (um deles com `quantidadeDisponivel` igual a 0)
- 3 usuários (incluindo "João Silva" e "Maria Silva Santos")
- 5 empréstimos (ativos no prazo, ativos atrasados, devolvido e um com status `ATRASADO`)

---

## 🛠️ Como Executar

### Compilar e rodar os testes:
```powershell
.\mvnw.cmd clean test
```

### Executar a aplicação:
```powershell
.\mvnw.cmd spring-boot:run
```
ou
```powershell
.\mvnw.cmd clean package
java -jar target/biblioteca-api-0.0.1-SNAPSHOT.jar
```

A aplicação sobe em `http://localhost:8080`.
**H2 Console:** `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb` · Usuário: `sa` · Senha: *(vazia)*

---

## ✅ Validação

Da suíte de testes (`BibliotecaApplicationTests` + `BibliotecaRepositoryTest`), são executados testes de
integração para **as 7 consultas**, validando os resultados esperados contra a base em memória populada
pelo seed.

```
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```