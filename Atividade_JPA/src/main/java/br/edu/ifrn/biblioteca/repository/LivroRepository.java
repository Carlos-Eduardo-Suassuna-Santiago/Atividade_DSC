package br.edu.ifrn.biblioteca.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.edu.ifrn.biblioteca.model.Livro;

@Repository
public interface LivroRepository extends JpaRepository<Livro, Long> {

    /**
     * Consulta 1 - Lista todos los libros disponibles
     * (quantidadeDisponivel > 0), ordenados por título.
     */
    List<Livro> findByQuantidadeDisponivelGreaterThanOrderByTituloAsc(int quantidade);

    /**
     * Consulta 2 - Busca libros por nombre de la categoría.
     * Retorna todos los libros pertenecientes a esa categoría.
     */
    List<Livro> findByCategoria_NomeIgnoreCaseOrderByTituloAsc(String nomeCategoria);

    /**
     * Consulta 5 - Encuentra los libros escritos por un autor específico
     * (busca parcial por nombre), ordenados por año de publicación.
     */
    @Query("select distinct l from Livro l join l.autores a "
            + "where lower(a.nome) like lower(concat('%', :nomeAutor, '%')) "
            + "order by l.anoPublicacao, l.titulo")
    List<Livro> findLivrosPorNomeAutor(@Param("nomeAutor") String nomeAutor);

    /**
     * Consulta 7 - Cuenta la cantidad de libros por categoría
     * (agregación), ordenada por cantidad decreciente.
     */
    @Query("select c.nome as nome, count(l) as total "
            + "from Categoria c left join c.livros l "
            + "group by c.nome "
            + "order by count(l) desc, c.nome")
    List<CategoriaQtd> contarQuantidadeLivrosPorCategoria();
}