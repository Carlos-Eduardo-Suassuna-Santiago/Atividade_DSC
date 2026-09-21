package br.edu.ifrn.biblioteca.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.edu.ifrn.biblioteca.model.Emprestimo;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    /**
     * Consulta 4 - Lista los préstamos ACTIVOS de un usuario específico,
     * incluyendo la información del usuario y de los libros prestados
     * (a través de los ítems).
     */
    @Query("select e from Emprestimo e "
            + "join fetch e.usuario "
            + "join fetch e.itens i "
            + "join fetch i.livro "
            + "where e.usuario.id = :usuarioId "
            + "and e.status = br.edu.ifrn.biblioteca.model.EmprestimoStatus.ATIVO")
    List<Emprestimo> findEmprestimosAtivosPorUsuario(@Param("usuarioId") Long usuarioId);

    /**
     * Consulta 6 - Lista los préstamos atrasados: status ACTIVO y con fecha de
     * devolución prevista anterior a la fecha actual. Incluye usuario y libros.
     */
    @Query("select e from Emprestimo e "
            + "join fetch e.usuario "
            + "join fetch e.itens i "
            + "join fetch i.livro "
            + "where e.status = br.edu.ifrn.biblioteca.model.EmprestimoStatus.ATIVO "
            + "and e.dataDevolucaoPrevista < :dataAtual")
    List<Emprestimo> findEmprestimosAtrasados(@Param("dataAtual") LocalDate dataAtual);
}