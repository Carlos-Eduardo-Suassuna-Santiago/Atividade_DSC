package br.edu.ifrn.biblioteca.repository;

/**
 * Proyección utilizada por la Consulta 7 (agregación): nombre de la categoría
 * y la cantidad de libros que contiene.
 */
public interface CategoriaQtd {

    String getNome();

    Long getTotal();
}