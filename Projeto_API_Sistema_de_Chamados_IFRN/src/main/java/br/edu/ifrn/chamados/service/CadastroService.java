package br.edu.ifrn.chamados.service;

import br.edu.ifrn.chamados.dto.Dtos.*;
import br.edu.ifrn.chamados.exception.Erros.*;
import br.edu.ifrn.chamados.model.*;
import br.edu.ifrn.chamados.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class CadastroService {
    private final SetorRepository setores; private final CategoriaRepository categorias; private final UsuarioRepository usuarios; private final ChamadoRepository chamados;
    public CadastroService(SetorRepository s,CategoriaRepository c,UsuarioRepository u,ChamadoRepository ch){setores=s;categorias=c;usuarios=u;chamados=ch;}
    private Pageable page(Pageable p){return PageRequest.of(p.getPageNumber(),Math.min(p.getPageSize(),50),p.getSort().isSorted()?p.getSort():Sort.by("id").ascending());}
    public Page<Setor> setores(String nome,Pageable p){Specification<Setor> x=(r,q,c)->nome==null?null:c.like(c.lower(r.get("nome")),"%"+nome.toLowerCase()+"%");return setores.findAll(x,page(p));}
    public Setor setor(Long id){return setores.findById(id).orElseThrow(()->new NaoEncontrado("Setor não encontrado."));}
    public Setor criar(SetorRequest r){if(setores.existsBySiglaIgnoreCase(r.sigla()))throw new Conflito("Sigla já cadastrada.");return setores.save(new Setor(r.nome(),r.sigla()));}
    public Setor atualizar(Long id,SetorRequest r){Setor s=setor(id);s.setNome(r.nome());s.setSigla(r.sigla());return setores.save(s);}
    public void excluirSetor(Long id){if(!setores.existsById(id))throw new NaoEncontrado("Setor não encontrado.");if(chamados.existsByCategoriaSetorId(id))throw new RegraNegocio("Setor possui chamados vinculados.");setores.deleteById(id);}
    public Page<Categoria> categorias(String nome,Long setorId,Pageable p){Specification<Categoria> x=(r,q,c)->nome==null?null:c.like(c.lower(r.get("nome")),"%"+nome.toLowerCase()+"%");if(setorId!=null)x=x.and((r,q,c)->c.equal(r.get("setor").get("id"),setorId));return categorias.findAll(x,page(p));}
    public Categoria categoria(Long id){return categorias.findById(id).orElseThrow(()->new NaoEncontrado("Categoria não encontrada."));}
    public Categoria criar(CategoriaRequest r){return categorias.save(new Categoria(r.nome(),r.descricao(),setor(r.setorId())));}
    public Categoria atualizar(Long id,CategoriaRequest r){Categoria c=categoria(id);c.setNome(r.nome());c.setDescricao(r.descricao());c.setSetor(setor(r.setorId()));return categorias.save(c);}
    public void excluirCategoria(Long id){if(!categorias.existsById(id))throw new NaoEncontrado("Categoria não encontrada.");if(chamados.existsByCategoriaId(id))throw new RegraNegocio("Categoria possui chamados vinculados.");categorias.deleteById(id);}
    public Page<Usuario> usuarios(String nome,TipoUsuario tipo,Long setorId,Pageable p){Specification<Usuario> x=(r,q,c)->nome==null?null:c.like(c.lower(r.get("nome")),"%"+nome.toLowerCase()+"%");if(tipo!=null)x=x.and((r,q,c)->c.equal(r.get("tipo"),tipo));if(setorId!=null)x=x.and((r,q,c)->c.equal(r.get("setor").get("id"),setorId));return usuarios.findAll(x,page(p));}
    public Usuario usuario(Long id){return usuarios.findById(id).orElseThrow(()->new NaoEncontrado("Usuário não encontrado."));}
    public Usuario criar(UsuarioRequest r){if(usuarios.existsByEmailIgnoreCase(r.email()))throw new Conflito("Email já cadastrado.");if(r.tipo()==TipoUsuario.TECNICO&&r.setorId()==null)throw new RegraNegocio("Técnico deve pertencer a um setor.");return usuarios.save(new Usuario(r.nome(),r.email(),r.matricula(),r.tipo(),r.setorId()==null?null:setor(r.setorId())));}
    public Usuario atualizar(Long id,UsuarioRequest r){Usuario u=usuario(id);u.setNome(r.nome());u.setEmail(r.email());u.setMatricula(r.matricula());u.setTipo(r.tipo());u.setSetor(r.setorId()==null?null:setor(r.setorId()));return usuarios.save(u);}
}
