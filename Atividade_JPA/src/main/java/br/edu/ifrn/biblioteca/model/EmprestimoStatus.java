package br.edu.ifrn.biblioteca.model;

/**
 * Estado de un préstamo de la biblioteca.
 */
public enum EmprestimoStatus {

    /** El libro está prestado y dentro del plazo. */
    ATIVO,

    /** El libro fue devuelto a la biblioteca. */
    DEVOLVIDO,

    /** La fecha prevista de devolución ya venció. */
    ATRASADO
}