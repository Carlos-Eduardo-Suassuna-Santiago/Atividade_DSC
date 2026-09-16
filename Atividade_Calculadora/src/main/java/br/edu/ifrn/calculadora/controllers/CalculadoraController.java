package br.edu.ifrn.calculadora.controllers;

import java.util.Locale;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/calculadora")
@Tag(name = "Calculadora", description = "Endpoints de operações matemáticas e análises numéricas")
public class CalculadoraController {

    // 1. Endpoint de soma com @PathVariable
    // GET /calculadora/somar/{numero1}/{numero2}
    @GetMapping("/somar/{numero1}/{numero2}")
    @Operation(summary = "Soma dois números", description = "Recebe dois números por PathVariable e retorna a soma.")
    public String somar(
            @Parameter(description = "Primeiro número", example = "10") @PathVariable double numero1,
            @Parameter(description = "Segundo número", example = "5") @PathVariable double numero2) {
        double resultado = numero1 + numero2;
        return formatarNumero(resultado);
    }

    // 2. Endpoint de subtração com @RequestParam
    // GET /calculadora/subtrair?numero1=20&numero2=8
    @GetMapping("/subtrair")
    @Operation(summary = "Subtrai dois números", description = "Recebe dois números por RequestParam e retorna a subtração (numero1 - numero2).")
    public String subtrair(
            @Parameter(description = "Primeiro número", example = "20") @RequestParam double numero1,
            @Parameter(description = "Segundo número", example = "8") @RequestParam double numero2) {
        double resultado = numero1 - numero2;
        return formatarNumero(resultado);
    }

    // 3. Endpoint único de cálculo
    // GET /calculadora/calcular/{operacao}?numero1=10&numero2=5
    // Suporta somar, subtrair, multiplicar e dividir com casasDecimais opcional (default 2)
    @GetMapping("/calcular/{operacao}")
    @Operation(summary = "Endpoint único de cálculo", description = "Executa a operação informada no path (somar, subtrair, multiplicar, dividir) com parâmetros na query string.")
    public String calcular(
            @Parameter(description = "Operação (somar, subtrair, multiplicar, dividir)", example = "multiplicar") @PathVariable String operacao,
            @Parameter(description = "Primeiro número", example = "10") @RequestParam double numero1,
            @Parameter(description = "Segundo número", example = "5") @RequestParam double numero2,
            @Parameter(description = "Casas decimais para formatação (padrão 2)", example = "2") @RequestParam(defaultValue = "2") int casasDecimais) {

        String opNormalizada = operacao.trim().toLowerCase(Locale.ROOT);
        String nomeOperacao;
        double resultado;

        switch (opNormalizada) {
            case "somar":
            case "soma":
                nomeOperacao = "soma";
                resultado = numero1 + numero2;
                break;

            case "subtrair":
            case "subtracao":
            case "subtração":
                nomeOperacao = "subtração";
                resultado = numero1 - numero2;
                break;

            case "multiplicar":
            case "multiplicacao":
            case "multiplicação":
                nomeOperacao = "multiplicação";
                resultado = numero1 * numero2;
                break;

            case "dividir":
            case "divisao":
            case "divisão":
                if (numero2 == 0) {
                    return "Erro: não é possível dividir por zero.";
                }
                nomeOperacao = "divisão";
                resultado = numero1 / numero2;
                break;

            default:
                return "Erro: operação inválida. Operações permitidas: somar, subtrair, multiplicar, dividir.";
        }

        String resultadoFormatado;
        if (opNormalizada.startsWith("divid") || opNormalizada.startsWith("divis")) {
            resultadoFormatado = String.format(Locale.US, "%." + casasDecimais + "f", resultado);
        } else if (resultado % 1 == 0) {
            resultadoFormatado = String.valueOf((long) resultado);
        } else {
            resultadoFormatado = String.format(Locale.US, "%." + casasDecimais + "f", resultado);
        }

        return String.format(Locale.US,
                "Operação: %s\nNúmero 1: %s\nNúmero 2: %s\nResultado: %s",
                nomeOperacao,
                formatarNumero(numero1),
                formatarNumero(numero2),
                resultadoFormatado);
    }

    // 4. Par ou ímpar
    // GET /calculadora/par-ou-impar/{numero}
    @GetMapping("/par-ou-impar/{numero}")
    @Operation(summary = "Verifica par ou ímpar", description = "Informa se o número recebido via PathVariable é PAR ou ÍMPAR.")
    public String parOuImpar(
            @Parameter(description = "Número inteiro para verificação", example = "10") @PathVariable long numero) {
        if (numero % 2 == 0) {
            return "PAR";
        } else {
            return "ÍMPAR";
        }
    }

    // 5. Análise de número
    // GET /calculadora/analisar/{numero}
    @GetMapping("/analisar/{numero}")
    @Operation(summary = "Análise completa do número", description = "Retorna informações detalhadas do número: paridade, sinal, dobro, metade e quadrado.")
    public String analisar(
            @Parameter(description = "Número para análise", example = "10") @PathVariable double numero) {
        String paridade;
        if (numero % 1 == 0) {
            paridade = ((long) numero % 2 == 0) ? "PAR" : "ÍMPAR";
        } else {
            paridade = "NÃO SE APLICA";
        }

        String sinal;
        if (numero > 0) {
            sinal = "POSITIVO";
        } else if (numero < 0) {
            sinal = "NEGATIVO";
        } else {
            sinal = "ZERO";
        }

        double dobro = numero * 2;
        double metade = numero / 2.0;
        double quadrado = numero * numero;

        return String.format(Locale.US,
                "Número: %s\nPar ou ímpar: %s\nPositivo, negativo ou zero: %s\nDobro: %s\nMetade: %s\nQuadrado: %s",
                formatarNumero(numero),
                paridade,
                sinal,
                formatarNumero(dobro),
                formatarNumero(metade),
                formatarNumero(quadrado));
    }

    // Desafio adicional — cálculo de média
    // GET /calculadora/media?nota1=7&nota2=8&nota3=6
    @GetMapping("/media")
    @Operation(summary = "Cálculo de média escolar", description = "Recebe três notas por RequestParam, calcula a média e define a situação: APROVADO, RECUPERAÇÃO ou REPROVADO.")
    public String calcularMedia(
            @Parameter(description = "Primeira nota", example = "7") @RequestParam double nota1,
            @Parameter(description = "Segunda nota", example = "8") @RequestParam double nota2,
            @Parameter(description = "Terceira nota", example = "6") @RequestParam double nota3) {

        double media = (nota1 + nota2 + nota3) / 3.0;

        String situacao;
        if (media >= 7.0) {
            situacao = "APROVADO";
        } else if (media >= 4.0) {
            situacao = "RECUPERAÇÃO";
        } else {
            situacao = "REPROVADO";
        }

        return String.format(Locale.US, "Média: %.1f\nSituação: %s", media, situacao);
    }

    // Método auxiliar para formatar números inteiros sem ".0"
    private String formatarNumero(double n) {
        if (n % 1 == 0) {
            return String.valueOf((long) n);
        }
        return String.format(Locale.US, "%s", n);
    }
}
