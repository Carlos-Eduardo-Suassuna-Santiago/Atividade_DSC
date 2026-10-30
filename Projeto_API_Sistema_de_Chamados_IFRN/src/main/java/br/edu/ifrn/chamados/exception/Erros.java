package br.edu.ifrn.chamados.exception;
public class Erros {
    public static class NaoEncontrado extends RuntimeException { public NaoEncontrado(String m){super(m);} }
    public static class RegraNegocio extends RuntimeException { public RegraNegocio(String m){super(m);} }
    public static class Conflito extends RuntimeException { public Conflito(String m){super(m);} }
    private Erros(){}
}
