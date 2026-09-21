package br.edu.ifrn.biblioteca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.ifrn.biblioteca.model.ItemEmprestimo;

@Repository
public interface ItemEmprestimoRepository extends JpaRepository<ItemEmprestimo, Long> {
}