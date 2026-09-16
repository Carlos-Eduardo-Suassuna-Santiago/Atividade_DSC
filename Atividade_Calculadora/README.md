# API de Calculadora com Spring Boot

Projeto desenvolvido para a disciplina de **Desenvolvimento de Sistemas Corporativos (2026.2)** do **IFRN (Instituto Federal de Educação, Ciência e Tecnologia do Rio Grande do Norte)** - Curso Superior de Tecnologia em Análise e Desenvolvimento de Sistemas.
**Professor:** Jeferson Queiroga

---

## 🎯 Objetivo
Criação de um `CalculadoraController` que realiza operações matemáticas através de endpoints HTTP utilizando Spring Boot. Toda a lógica de negócio foi centralizada diretamente no controller, sem uso de banco de dados, entidades ou serviços.

---

## 🚀 Tecnologias Utilizadas
- **Java 21 (LTS)**
- **Spring Boot 4 / Spring Web**
- **Springdoc OpenAPI (Swagger UI)**
- **Interface Web Moderna (HTML5, CSS3, JavaScript Vanilla)**
- **Maven Wrapper (`mvnw`, `mvnw.cmd`)**
- **JUnit 5 / MockMvc**

---

## 🖥️ Como Acessar a Interface Gráfica e Swagger

Com a aplicação rodando, basta abrir no navegador:
- **Interface Web Interativa (Calculadora Visual):** [http://localhost:8080](http://localhost:8080)
- **Documentação Interativa Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 📋 Endpoints Implementados

Base URL: `http://localhost:8080/calculadora`

### 1. Soma com `@PathVariable`
- **Rota:** `GET /calculadora/somar/{numero1}/{numero2}`
- **Exemplo:** `GET /calculadora/somar/10/5`
- **Resposta:**
  ```
  15
  ```

---

### 2. Subtração com `@RequestParam`
- **Rota:** `GET /calculadora/subtrair?numero1={n1}&numero2={n2}`
- **Exemplo:** `GET /calculadora/subtrair?numero1=20&numero2=8`
- **Resposta:**
  ```
  12
  ```

---

### 3. Endpoint Único de Cálculo
- **Rota:** `GET /calculadora/calcular/{operacao}?numero1={n1}&numero2={n2}&casasDecimais={cd}`
- Suporta as operações: `somar`, `subtrair`, `multiplicar` e `dividir`.
- Possui o parâmetro opcional `casasDecimais` (padrão: `2`).

#### Exemplos:
- **Multiplicação:**
  `GET /calculadora/calcular/multiplicar?numero1=10&numero2=5`
  ```
  Operação: multiplicação
  Número 1: 10
  Número 2: 5
  Resultado: 50
  ```

- **Divisão com Casas Decimais Customizadas:**
  `GET /calculadora/calcular/dividir?numero1=10&numero2=3&casasDecimais=2`
  ```
  Operação: divisão
  Número 1: 10
  Número 2: 3
  Resultado: 3.33
  ```

- **Tratamento de Divisão por Zero:**
  `GET /calculadora/calcular/dividir?numero1=10&numero2=0`
  ```
  Erro: não é possível dividir por zero.
  ```

- **Tratamento de Operação Inválida:**
  `GET /calculadora/calcular/invalida?numero1=10&numero2=5`
  ```
  Erro: operação inválida. Operações permitidas: somar, subtrair, multiplicar, dividir.
  ```

---

### 4. Par ou Ímpar
- **Rota:** `GET /calculadora/par-ou-impar/{numero}`
- **Exemplo 1:** `GET /calculadora/par-ou-impar/10` $\rightarrow$ `PAR`
- **Exemplo 2:** `GET /calculadora/par-ou-impar/7` $\rightarrow$ `ÍMPAR`

---

### 5. Análise de Número
- **Rota:** `GET /calculadora/analisar/{numero}`
- **Exemplo:** `GET /calculadora/analisar/10`
- **Resposta:**
  ```
  Número: 10
  Par ou ímpar: PAR
  Positivo, negativo ou zero: POSITIVO
  Dobro: 20
  Metade: 5
  Quadrado: 100
  ```

---

### 6. Desafio Adicional — Cálculo de Média
- **Rota:** `GET /calculadora/media?nota1={n1}&nota2={n2}&nota3={n3}`
- **Critérios:**
  - $\text{Média} \ge 7.0 \rightarrow$ `APROVADO`
  - $\text{Média} \ge 4.0 \rightarrow$ `RECUPERAÇÃO`
  - $\text{Média} < 4.0 \rightarrow$ `REPROVADO`
- **Exemplo:** `GET /calculadora/media?nota1=7&nota2=8&nota3=6`
- **Resposta:**
  ```
  Média: 7.0
  Situação: APROVADO
  ```

---

## 🛠️ Como Executar

### Compilar e Rodar os Testes:
No Windows:
```powershell
.\mvnw.cmd clean test
```
No Linux/Mac:
```bash
./mvnw clean test
```

### Gerar o arquivo `.jar`:
```powershell
.\mvnw.cmd clean package
```

### Executar a Aplicação:
```powershell
java -jar target/calculadora-api-0.0.1-SNAPSHOT.jar
```
Ou diretamente com o plugin do Spring Boot:
```powershell
.\mvnw.cmd spring-boot:run
```
A API estará disponível em `http://localhost:8080`.
