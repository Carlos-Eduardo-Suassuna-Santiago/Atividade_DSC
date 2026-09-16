package br.edu.ifrn.calculadora;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.edu.ifrn.calculadora.controllers.CalculadoraController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CalculadoraControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CalculadoraController()).build();
    }

    @Test
    @DisplayName("1. Deve somar dois números via @PathVariable")
    void testSomar() throws Exception {
        mockMvc.perform(get("/calculadora/somar/10/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("15"));
    }

    @Test
    @DisplayName("2. Deve subtrair dois números via @RequestParam")
    void testSubtrair() throws Exception {
        mockMvc.perform(get("/calculadora/subtrair")
                        .param("numero1", "20")
                        .param("numero2", "8"))
                .andExpect(status().isOk())
                .andExpect(content().string("12"));
    }

    @Test
    @DisplayName("3.1. Deve realizar multiplicação no endpoint único de cálculo")
    void testCalcularMultiplicar() throws Exception {
        String expected = "Operação: multiplicação\nNúmero 1: 10\nNúmero 2: 5\nResultado: 50";
        mockMvc.perform(get("/calculadora/calcular/multiplicar")
                        .param("numero1", "10")
                        .param("numero2", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));
    }

    @Test
    @DisplayName("3.2. Deve realizar soma no endpoint único de cálculo")
    void testCalcularSomar() throws Exception {
        String expected = "Operação: soma\nNúmero 1: 10\nNúmero 2: 5\nResultado: 15";
        mockMvc.perform(get("/calculadora/calcular/somar")
                        .param("numero1", "10")
                        .param("numero2", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));
    }

    @Test
    @DisplayName("3.3. Deve realizar divisão com casasDecimais customizadas")
    void testCalcularDividirComCasasDecimais() throws Exception {
        String expected = "Operação: divisão\nNúmero 1: 10\nNúmero 2: 3\nResultado: 3.33";
        mockMvc.perform(get("/calculadora/calcular/dividir")
                        .param("numero1", "10")
                        .param("numero2", "3")
                        .param("casasDecimais", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));
    }

    @Test
    @DisplayName("3.4. Deve tratar divisão por zero")
    void testCalcularDivisaoPorZero() throws Exception {
        mockMvc.perform(get("/calculadora/calcular/dividir")
                        .param("numero1", "10")
                        .param("numero2", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string("Erro: não é possível dividir por zero."));
    }

    @Test
    @DisplayName("3.5. Deve tratar operação inválida")
    void testCalcularOperacaoInvalida() throws Exception {
        mockMvc.perform(get("/calculadora/calcular/potencia")
                        .param("numero1", "10")
                        .param("numero2", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Erro: operação inválida. Operações permitidas: somar, subtrair, multiplicar, dividir."));
    }

    @Test
    @DisplayName("4. Deve informar se número é PAR ou ÍMPAR")
    void testParOuImpar() throws Exception {
        mockMvc.perform(get("/calculadora/par-ou-impar/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("PAR"));

        mockMvc.perform(get("/calculadora/par-ou-impar/7"))
                .andExpect(status().isOk())
                .andExpect(content().string("ÍMPAR"));
    }

    @Test
    @DisplayName("5. Deve retornar análise completa do número")
    void testAnalisarNumero() throws Exception {
        String expected = "Número: 10\n" +
                "Par ou ímpar: PAR\n" +
                "Positivo, negativo ou zero: POSITIVO\n" +
                "Dobro: 20\n" +
                "Metade: 5\n" +
                "Quadrado: 100";

        mockMvc.perform(get("/calculadora/analisar/10"))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));
    }

    @Test
    @DisplayName("6. Deve calcular média e situação (Aprovado, Recuperação, Reprovado)")
    void testCalcularMedia() throws Exception {
        // Aprovado
        mockMvc.perform(get("/calculadora/media")
                        .param("nota1", "7")
                        .param("nota2", "8")
                        .param("nota3", "6"))
                .andExpect(status().isOk())
                .andExpect(content().string("Média: 7.0\nSituação: APROVADO"));

        // Recuperação
        mockMvc.perform(get("/calculadora/media")
                        .param("nota1", "4")
                        .param("nota2", "5")
                        .param("nota3", "6"))
                .andExpect(status().isOk())
                .andExpect(content().string("Média: 5.0\nSituação: RECUPERAÇÃO"));

        // Reprovado
        mockMvc.perform(get("/calculadora/media")
                        .param("nota1", "2")
                        .param("nota2", "3")
                        .param("nota3", "4"))
                .andExpect(status().isOk())
                .andExpect(content().string("Média: 3.0\nSituação: REPROVADO"));
    }
}
