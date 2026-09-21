package br.edu.ifrn.biblioteca.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.ifrn.biblioteca.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Consulta 3 - Busca usuarios por nombre (busca parcial / substring).
     * Ejemplo: "Silva" retorna "João Silva", "Maria Silva Santos", etc.
     */
    List<Usuario> findByNomeContainingIgnoreCase(String nome);

    java.util.Optional<Usuario> findByCpf(String cpf);
}