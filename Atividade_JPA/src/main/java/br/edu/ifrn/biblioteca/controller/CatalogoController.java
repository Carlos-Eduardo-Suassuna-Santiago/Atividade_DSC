package br.edu.ifrn.biblioteca.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifrn.biblioteca.model.Categoria;
import br.edu.ifrn.biblioteca.model.Livro;
import br.edu.ifrn.biblioteca.repository.CategoriaRepository;
import br.edu.ifrn.biblioteca.repository.LivroRepository;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final LivroRepository livroRepository;
    private final CategoriaRepository categoriaRepository;

    public CatalogoController(LivroRepository livroRepository, CategoriaRepository categoriaRepository) {
        this.livroRepository = livroRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping("/livros")
    public List<LivroResumo> livros() {
        return livroRepository.findAll().stream()
                .sorted((primeiro, segundo) -> primeiro.getTitulo().compareToIgnoreCase(segundo.getTitulo()))
                .map(LivroResumo::from)
                .toList();
    }

    @GetMapping("/categorias")
    public List<CategoriaResumo> categorias() {
        return categoriaRepository.findAll().stream()
                .sorted((primeira, segunda) -> primeira.getNome().compareToIgnoreCase(segunda.getNome()))
                .map(categoria -> new CategoriaResumo(categoria.getId(), categoria.getNome()))
                .toList();
    }

    public record LivroResumo(
            Long id,
            String titulo,
            String editora,
            Integer anoPublicacao,
            Integer numeroPaginas,
            Integer quantidadeTotal,
            Integer quantidadeDisponivel,
            String categoria,
            List<String> autores) {

        static LivroResumo from(Livro livro) {
            return new LivroResumo(
                    livro.getId(),
                    livro.getTitulo(),
                    livro.getEditora(),
                    livro.getAnoPublicacao(),
                    livro.getNumeroPaginas(),
                    livro.getQuantidadeTotal(),
                    livro.getQuantidadeDisponivel(),
                    livro.getCategoria().getNome(),
                    livro.getAutores().stream().map(autor -> autor.getNome()).toList());
        }
    }

    public record CategoriaResumo(Long id, String nome) {
    }
}