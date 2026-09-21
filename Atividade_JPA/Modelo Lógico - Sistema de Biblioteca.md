### **Atividade \- D. Sistema Corporativo**

## O objetivo deste exercício é desenvolver um projeto Spring Boot, configurando corretamente as camadas Model e Repository com base no domínio proposto e nas consultas solicitadas.

## **Modelo Lógico \- Sistema de Biblioteca**

### **Entidades e Atributos:**

**1\. LIVRO**

* id (PK)  
* isbn  
* titulo  
* editora  
* anoPublicacao  
* numeroPaginas  
* quantidadeTotal  
* quantidadeDisponivel  
* categoria\_id (FK)

**2\. AUTOR**

* id (PK)  
* nome  
* dataNascimento  
* nacionalidade  
* biografia

**3\. CATEGORIA**

* id (PK)  
* nome  
* descricao

**4\. USUARIO**

* id (PK)  
* nome  
* cpf  
* email  
* telefone  
* dataCadastro  
* endereco  
* ativo

**5\. EMPRESTIMO**

* id (PK)  
* usuario\_id (FK)  
* dataEmprestimo  
* dataDevolucaoPrevista  
* dataDevolucaoEfetiva  
* status (ATIVO, DEVOLVIDO, ATRASADO)  
* valorMulta

**6\. ITEM\_EMPRESTIMO**

* id (PK)  
* emprestimo\_id (FK)  
* livro\_id (FK)

**7\. LIVRO\_AUTOR** (Tabela associativa)

* livro\_id (FK)  
* autor\_id (FK)  
* (PK composta)

### **Relacionamentos:**

1. **LIVRO ↔ AUTOR** (N:N)  
   * Um livro pode ter vários autores  
   * Um autor pode escrever vários livros  
   * Tabela associativa: LIVRO\_AUTOR  
2. **LIVRO → CATEGORIA** (N:1)  
   * Um livro pertence a uma categoria  
   * Uma categoria pode ter vários livros  
3. **EMPRESTIMO → USUARIO** (N:1)  
   * Um empréstimo pertence a um usuário  
   * Um usuário pode ter vários empréstimos  
4. **EMPRESTIMO ↔ LIVRO** (N:N através de ITEM\_EMPRESTIMO)  
   * Um empréstimo pode conter vários livros  
   * Um livro pode estar em vários empréstimos (em momentos diferentes)  
   * Tabela associativa: ITEM\_EMPRESTIMO

## **Exercícios de Consultas \- Sistema de Biblioteca**

**1\. Listar todos os livros disponíveis**

* Criar uma consulta que retorne todos os livros onde `quantidadeDisponivel > 0`  
* Ordenar por título

**2\. Buscar livros por categoria**

* Criar uma consulta que receba o nome da categoria como parâmetro  
* Retornar todos os livros dessa categoria

**3\. Buscar usuários por nome (busca parcial)**

* Implementar busca que aceite parte do nome  
* Exemplo: "Silva" deve retornar "João Silva", "Maria Silva Santos", etc.

**4\. Listar empréstimos ativos de um usuário**

* Buscar todos os empréstimos com status ATIVO de um usuário específico  
* Incluir informações dos livros emprestados

**5\. Encontrar livros de um autor específico**

* Criar consulta que receba o nome do autor  
* Retornar todos os livros escritos por esse autor  
* Ordenar por ano de publicação

**6\. Listar empréstimos atrasados**

* Buscar empréstimos onde `dataDevolucaoPrevista < data atual`  
* E status ainda é ATIVO  
* Incluir informações do usuário e livros

**7\. Contar quantidade de livros por categoria**

* Criar consulta de agregação  
* Retornar nome da categoria e quantidade de livros  
* Ordenar por quantidade decrescente

