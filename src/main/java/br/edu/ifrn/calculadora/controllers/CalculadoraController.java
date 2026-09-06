package br.edu.ifrn.calculadora.controllers;

import java.util.Locale;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calculadora")
public class CalculadoraController {

    // 1. Endpoint de soma com @PathVariable
    // GET /calculadora/somar/{numero1}/{numero2}
    @GetMapping("/somar/{numero1}/{numero2}")
    public String somar(@PathVariable double numero1, @PathVariable double numero2) {
        double resultado = numero1 + numero2;
        return formatarNumero(resultado);
    }

    // 2. Endpoint de subtração com @RequestParam
    // GET /calculadora/subtrair?numero1=20&numero2=8
    @GetMapping("/subtrair")
    public String subtrair(@RequestParam double numero1, @RequestParam double numero2) {
        double resultado = numero1 - numero2;
        return formatarNumero(resultado);
    }

    // 3. Endpoint único de cálculo
    // GET /calculadora/calcular/{operacao}?numero1=10&numero2=5
    // Suporta somar, subtrair, multiplicar e dividir com casasDecimais opcional (default 2)
    @GetMapping("/calcular/{operacao}")
    public String calcular(
            @PathVariable String operacao,
            @RequestParam double numero1,
            @RequestParam double numero2,
            @RequestParam(defaultValue = "2") int casasDecimais) {

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
    public String parOuImpar(@PathVariable long numero) {
        if (numero % 2 == 0) {
            return "PAR";
        } else {
            return "ÍMPAR";
        }
    }

    // 5. Análise de número
    // GET /calculadora/analisar/{numero}
    @GetMapping("/analisar/{numero}")
    public String analisar(@PathVariable double numero) {
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
    public String calcularMedia(
            @RequestParam double nota1,
            @RequestParam double nota2,
            @RequestParam double nota3) {

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
