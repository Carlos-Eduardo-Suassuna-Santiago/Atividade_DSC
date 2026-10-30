package br.edu.ifrn.chamados.controller;

import br.edu.ifrn.chamados.dto.Dtos.*;
import br.edu.ifrn.chamados.model.*;
import br.edu.ifrn.chamados.service.CadastroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class CadastroController {
    private final CadastroService service;
    public CadastroController(CadastroService s){service=s;}
    private <T> Pagina<T> pagina(Page<T> p){return new Pagina<>(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages());}
    @PostMapping("/setores") public Setor setor(@Valid @RequestBody SetorRequest r){return service.criar(r);}
    @GetMapping("/setores") public Pagina<Setor> setores(@RequestParam(required=false) String nome,Pageable p){return pagina(service.setores(nome,p));}
    @GetMapping("/setores/{id}") public Setor setor(@PathVariable Long id){return service.setor(id);}
    @PutMapping("/setores/{id}") public Setor setor(@PathVariable Long id,@Valid @RequestBody SetorRequest r){return service.atualizar(id,r);}
    @DeleteMapping("/setores/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void delSetor(@PathVariable Long id){service.excluirSetor(id);}
    @PostMapping("/categorias") public Categoria categoria(@Valid @RequestBody CategoriaRequest r){return service.criar(r);}
    @GetMapping("/categorias") public Pagina<Categoria> categorias(@RequestParam(required=false) String nome,@RequestParam(required=false) Long setorId,Pageable p){return pagina(service.categorias(nome,setorId,p));}
    @GetMapping("/setores/{id}/categorias") public Pagina<Categoria> categoriasDoSetor(@PathVariable Long id,Pageable p){return pagina(service.categorias(null,id,p));}
    @GetMapping("/categorias/{id}") public Categoria categoria(@PathVariable Long id){return service.categoria(id);}
    @PutMapping("/categorias/{id}") public Categoria categoria(@PathVariable Long id,@Valid @RequestBody CategoriaRequest r){return service.atualizar(id,r);}
    @DeleteMapping("/categorias/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void delCategoria(@PathVariable Long id){service.excluirCategoria(id);}
    @PostMapping("/usuarios") public Usuario usuario(@Valid @RequestBody UsuarioRequest r){return service.criar(r);}
    @GetMapping("/usuarios") public Pagina<Usuario> usuarios(@RequestParam(required=false) String nome,@RequestParam(required=false) TipoUsuario tipo,@RequestParam(required=false) Long setorId,Pageable p){return pagina(service.usuarios(nome,tipo,setorId,p));}
    @GetMapping("/usuarios/{id}") public Usuario usuario(@PathVariable Long id){return service.usuario(id);}
    @PutMapping("/usuarios/{id}") public Usuario usuario(@PathVariable Long id,@Valid @RequestBody UsuarioRequest r){return service.atualizar(id,r);}
}
