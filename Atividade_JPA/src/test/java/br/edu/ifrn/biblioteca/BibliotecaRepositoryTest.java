package br.edu.ifrn.biblioteca;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.edu.ifrn.biblioteca.model.Emprestimo;
import br.edu.ifrn.biblioteca.model.EmprestimoStatus;
import br.edu.ifrn.biblioteca.model.Livro;
import br.edu.ifrn.biblioteca.model.Usuario;
import br.edu.ifrn.biblioteca.repository.CategoriaQtd;
import br.edu.ifrn.biblioteca.repository.EmprestimoRepository;
import br.edu.ifrn.biblioteca.repository.LivroRepository;
import br.edu.ifrn.biblioteca.repository.UsuarioRepository;

/**
 * Testes de integração que validam as 7 consultas solicitadas na atividade,
 * contra a base H2 em memória populada pelo {@link br.edu.ifrn.biblioteca.config.DataSeeder}.
 */
@SpringBootTest
class BibliotecaRepositoryTest {

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Test
    @DisplayName("Consulta 1 - Listar todos os livros disponíveis (qtd > 0) ordenados por título")
    void consulta1LivrosDisponiveis() {
        List<Livro> livros = livroRepository.findByQuantidadeDisponivelGreaterThanOrderByTituloAsc(0);

        assertThat(livros).hasSize(4);
        assertThat(livros)
                .extracting(Livro::getTitulo)
                .containsExactly("Crônica de Gelo e Fogo", "Dom Casmurro",
                        "Memórias Póstumas de Brás Cubas", "O Senhor dos Anéis");
        assertThat(livros)
                .allSatisfy(l -> assertThat(l.getQuantidadeDisponivel()).isGreaterThan(0));
    }

    @Test
    @DisplayName("Consulta 2 - Buscar livros por categoria")
    void consulta2LivrosPorCategoria() {
        List<Livro> livros = livroRepository.findByCategoria_NomeIgnoreCaseOrderByTituloAsc("ficção");

        assertThat(livros).hasSize(4);
        assertThat(livros)
                .extracting(Livro::getTitulo)
                .contains("Dom Casmurro", "O Senhor dos Anéis", "Crônica de Gelo e Fogo");
        assertThat(livros)
                .allSatisfy(l -> assertThat(l.getCategoria().getNome()).isEqualToIgnoringCase("Ficção"));
    }

    @Test
    @DisplayName("Consulta 3 - Buscar usuários por nome (busca parcial)")
    void consulta3UsuariosPorNomeParcial() {
        List<Usuario> usuarios = usuarioRepository.findByNomeContainingIgnoreCase("silva");

        assertThat(usuarios).hasSize(2);
        assertThat(usuarios)
                .extracting(Usuario::getNome)
                .containsExactlyInAnyOrder("João Silva", "Maria Silva Santos");
    }

    @Test
    @DisplayName("Consulta 4 - Listar empréstimos ativos de um usuário (com livros)")
    void consulta4EmprestimosAtivosDeUmUsuario() {
        Usuario joao = usuarioJoao();
        List<Emprestimo> ativos = emprestimoRepository.findEmprestimosAtivosPorUsuario(joao.getId());

        assertThat(ativos).hasSize(2);
        assertThat(ativos).allSatisfy(e -> {
            assertThat(e.getStatus()).isEqualTo(EmprestimoStatus.ATIVO);
            assertThat(e.getUsuario().getNome()).isEqualTo("João Silva");
            assertThat(e.getItens()).isNotEmpty();
            assertThat(e.getItens())
                    .allSatisfy(i -> assertThat(i.getLivro()).isNotNull());
        });
    }

    @Test
    @DisplayName("Consulta 5 - Encontrar livros de um autor específico ordenados por ano")
    void consulta5LivrosDeUmAutor() {
        List<Livro> livros = livroRepository.findLivrosPorNomeAutor("Assis");

        assertThat(livros).hasSize(2);
        assertThat(livros)
                .extracting(Livro::getTitulo)
                .containsExactly("Memórias Póstumas de Brás Cubas", "Dom Casmurro");
        assertThat(livros.get(0).getAnoPublicacao()).isLessThan(livros.get(1).getAnoPublicacao());
    }

    @Test
    @DisplayName("Consulta 6 - Listar empréstimos atrasados (ATIVO e prevista < hoje)")
    void consulta6EmprestimosAtrasados() {
        List<Emprestimo> atrasados = emprestimoRepository.findEmprestimosAtrasados(LocalDate.now());

        assertThat(atrasados).hasSize(1);
        Emprestimo unico = atrasados.get(0);
        assertThat(unico.getStatus()).isEqualTo(EmprestimoStatus.ATIVO);
        assertThat(unico.getDataDevolucaoPrevista()).isBefore(LocalDate.now());
        assertThat(unico.getUsuario().getNome()).isEqualTo("João Silva");
        assertThat(unico.getItens())
                .extracting(i -> i.getLivro().getTitulo())
                .contains("Memórias Póstumas de Brás Cubas");
    }

    @Test
    @DisplayName("Consulta 7 - Contar quantidade de livros por categoria (decrescente)")
    void consulta7ContarLivrosPorCategoria() {
        List<CategoriaQtd> resultado = livroRepository.contarQuantidadeLivrosPorCategoria();

        assertThat(resultado).hasSize(3);
        assertThat(resultado).extracting(CategoriaQtd::getNome)
                .containsExactly("Ficção", "Romance", "Ciência");
        assertThat(resultado.get(0).getTotal()).isEqualTo(4L);
        assertThat(resultado.get(1).getTotal()).isEqualTo(1L);
        assertThat(resultado.get(2).getTotal()).isEqualTo(0L);
    }

    private Usuario usuarioJoao() {
        Optional<Usuario> joao = usuarioRepository.findByCpf("111.111.111-11");
        assertThat(joao).isPresent();
        return joao.get();
    }
}