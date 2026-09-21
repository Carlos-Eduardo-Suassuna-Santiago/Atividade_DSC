package br.edu.ifrn.biblioteca.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifrn.biblioteca.model.Autor;
import br.edu.ifrn.biblioteca.model.Categoria;
import br.edu.ifrn.biblioteca.model.Emprestimo;
import br.edu.ifrn.biblioteca.model.EmprestimoStatus;
import br.edu.ifrn.biblioteca.model.ItemEmprestimo;
import br.edu.ifrn.biblioteca.model.Livro;
import br.edu.ifrn.biblioteca.model.Usuario;
import br.edu.ifrn.biblioteca.repository.AutorRepository;
import br.edu.ifrn.biblioteca.repository.CategoriaRepository;
import br.edu.ifrn.biblioteca.repository.EmprestimoRepository;
import br.edu.ifrn.biblioteca.repository.LivroRepository;
import br.edu.ifrn.biblioteca.repository.UsuarioRepository;

/**
 * Población los datos de ejemplo del sistema de biblioteca al iniciar la
 * aplicación (base H2 en memoria). Permite ejecutar y validar las consultas
 * solicitadas en la actividad.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DataSeeder.class);

    private final CategoriaRepository categoriaRepository;
    private final AutorRepository autorRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;

    public DataSeeder(CategoriaRepository categoriaRepository,
            AutorRepository autorRepository,
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository,
            EmprestimoRepository emprestimoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.autorRepository = autorRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // ---------- Categorías ----------
        Categoria ficcao = categoriaRepository.save(new Categoria("Ficção", "Livros de ficção em geral"));
        Categoria romance = categoriaRepository.save(new Categoria("Romance", "Romances e literatura"));
        categoriaRepository.save(new Categoria("Ciência", "Livros científicos e técnicos"));

        // ---------- Autores ----------
        Autor machadoAssis = autorRepository.save(new Autor(
                "Machado de Assis", LocalDate.of(1839, 6, 21), "Brasileira", "Escritor brasileiro, maior nome do realismo."));
        Autor clariceLispector = autorRepository.save(new Autor(
                "Clarice Lispector", LocalDate.of(1920, 12, 10), "Ucraniana/Brasileira", "Romancista e contista brasileira."));
        Autor jrrTolkien = autorRepository.save(new Autor(
                "J.R.R. Tolkien", LocalDate.of(1892, 1, 3), "Inglesa", "Autor de O Senhor dos Anéis."));
        Autor georgeMartin = autorRepository.save(new Autor(
                "George R.R. Martin", LocalDate.of(1948, 9, 20), "Americana", "Autor de As Crônicas de Gelo e Fogo."));

        // ---------- Livros ----------
        Livro domCasmurro = criarLivro("978-85-01-00001-1", "Dom Casmurro", "Garnier",
                1899, 256, 10, 3, ficcao, machadoAssis);
        Livro memorias = criarLivro("978-85-01-00002-2", "Memórias Póstumas de Brás Cubas", "Garnier",
                1881, 208, 5, 1, ficcao, machadoAssis);
        Livro horaDaEstrela = criarLivro("978-85-01-00003-3", "A Hora da Estrela", "Rocco",
                1977, 96, 8, 0, romance, clariceLispector);
        Livro sda = criarLivro("978-85-01-00004-4", "O Senhor dos Anéis", "HarperCollins",
                1954, 1216, 15, 7, ficcao, jrrTolkien);
        Livro geloEFogo = criarLivro("978-85-01-00005-5", "Crônica de Gelo e Fogo", "Leya",
                1996, 694, 20, 12, ficcao, georgeMartin);

        List<Livro> livros = livroRepository.saveAll(List.of(
                domCasmurro, memorias, horaDaEstrela, sda, geloEFogo));

        // ---------- Usuarios ----------
        Usuario joao = usuarioRepository.save(new Usuario(
                "João Silva", "111.111.111-11", "joao.silva@email.com", "(84) 99999-1111",
                LocalDate.now().minusYears(1), "Rua A, 100 - Natal/RN", true));
        Usuario maria = usuarioRepository.save(new Usuario(
                "Maria Silva Santos", "222.222.222-22", "maria@email.com", "(84) 99999-2222",
                LocalDate.now().minusMonths(6), "Rua B, 200 - Natal/RN", true));
        Usuario ana = usuarioRepository.save(new Usuario(
                "Ana Costa", "333.333.333-33", "ana@email.com", "(84) 99999-3333",
                LocalDate.now().minusDays(30), "Rua C, 300 - Natal/RN", false));

        // ---------- Empréstimos ----------
        LocalDate hoje = LocalDate.now();

        // João (id 1): empréstimo ativo dentro do prazo
        Emprestimo e1 = new Emprestimo();
        e1.setUsuario(joao);
        e1.setDataEmprestimo(hoje.minusDays(10));
        e1.setDataDevolucaoPrevista(hoje.plusDays(10));
        e1.setStatus(EmprestimoStatus.ATIVO);
        e1.addItem(new ItemEmprestimo(domCasmurro));
        e1.addItem(new ItemEmprestimo(sda));
        emprestimoRepository.save(e1);

        // João: empréstimo ativo mas com data prevista no passado (atrasado s/ atualizar status)
        Emprestimo e2 = new Emprestimo();
        e2.setUsuario(joao);
        e2.setDataEmprestimo(hoje.minusDays(30));
        e2.setDataDevolucaoPrevista(hoje.minusDays(3));
        e2.setStatus(EmprestimoStatus.ATIVO);
        e2.addItem(new ItemEmprestimo(memorias));
        emprestimoRepository.save(e2);

        // Maria (id 2): empréstimo ativo dentro do prazo
        Emprestimo e3 = new Emprestimo();
        e3.setUsuario(maria);
        e3.setDataEmprestimo(hoje.minusDays(20));
        e3.setDataDevolucaoPrevista(hoje.plusDays(5));
        e3.setStatus(EmprestimoStatus.ATIVO);
        e3.addItem(new ItemEmprestimo(geloEFogo));
        emprestimoRepository.save(e3);

        // Maria: empréstimo devolvido
        Emprestimo e4 = new Emprestimo();
        e4.setUsuario(maria);
        e4.setDataEmprestimo(hoje.minusDays(40));
        e4.setDataDevolucaoPrevista(hoje.minusDays(10));
        e4.setDataDevolucaoEfetiva(hoje.minusDays(12));
        e4.setStatus(EmprestimoStatus.DEVOLVIDO);
        e4.setValorMulta(BigDecimal.ZERO);
        e4.addItem(new ItemEmprestimo(horaDaEstrela));
        emprestimoRepository.save(e4);

        // Ana: empréstimo com status ATRASADO
        Emprestimo e5 = new Emprestimo();
        e5.setUsuario(ana);
        e5.setDataEmprestimo(hoje.minusDays(60));
        e5.setDataDevolucaoPrevista(hoje.minusDays(30));
        e5.setStatus(EmprestimoStatus.ATRASADO);
        e5.setValorMulta(new BigDecimal("5.00"));
        e5.addItem(new ItemEmprestimo(sda));
        emprestimoRepository.save(e5);

        LOG.info("Seed de datos concluido: {} categorías, {} autores, {} libros, {} usuarios, {} préstamos.",
                categoriaRepository.count(), autorRepository.count(), livros.size(),
                usuarioRepository.count(), emprestimoRepository.count());
    }

    private Livro criarLivro(String isbn, String titulo, String editora, int ano,
            int paginas, int qtdTotal, int qtdDisponivel,
            Categoria categoria, Autor... autores) {
        Livro livro = new Livro();
        livro.setIsbn(isbn);
        livro.setTitulo(titulo);
        livro.setEditora(editora);
        livro.setAnoPublicacao(ano);
        livro.setNumeroPaginas(paginas);
        livro.setQuantidadeTotal(qtdTotal);
        livro.setQuantidadeDisponivel(qtdDisponivel);
        livro.setCategoria(categoria);
        for (Autor autor : autores) {
            livro.getAutores().add(autor);
            autor.getLivros().add(livro);
        }
        return livro;
    }
}