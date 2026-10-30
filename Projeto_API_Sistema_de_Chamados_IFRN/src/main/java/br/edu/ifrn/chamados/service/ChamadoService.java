package br.edu.ifrn.chamados.service;

import br.edu.ifrn.chamados.dto.Dtos.*;
import br.edu.ifrn.chamados.exception.Erros.*;
import br.edu.ifrn.chamados.model.*;
import br.edu.ifrn.chamados.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ChamadoService {
    private final ChamadoRepository chamados; private final UsuarioRepository usuarios; private final CategoriaRepository categorias; private final InteracaoRepository interacoes;
    public ChamadoService(ChamadoRepository c,UsuarioRepository u,CategoriaRepository ca,InteracaoRepository i){chamados=c;usuarios=u;categorias=ca;interacoes=i;}
    private Usuario usuario(Long id){return usuarios.findById(id).orElseThrow(()->new NaoEncontrado("Usuário não encontrado."));}
    private Chamado chamado(Long id){return chamados.findById(id).orElseThrow(()->new NaoEncontrado("Chamado não encontrado."));}
    private Pageable page(Pageable p){return PageRequest.of(p.getPageNumber(),Math.min(p.getPageSize(),50),p.getSort().isSorted()?p.getSort():Sort.by("dataAbertura").descending());}
    public Chamado abrir(ChamadoRequest r){Usuario u=usuario(r.solicitanteId());if(u.getTipo()!=TipoUsuario.SOLICITANTE)throw new RegraNegocio("Apenas solicitantes podem abrir chamados.");Categoria c=categorias.findById(r.categoriaId()).orElseThrow(()->new NaoEncontrado("Categoria não encontrada."));return chamados.save(new Chamado(r.titulo(),r.descricao(),r.local(),r.prioridade(),u,c));}
    public Page<Chamado> listar(StatusChamado status,Prioridade prioridade,Long setorId,Long categoriaId,Long solicitanteId,Long tecnicoId,LocalDateTime inicio,LocalDateTime fim,Pageable p){Specification<Chamado> x=(r,q,c)->null;if(status!=null)x=x.and((r,q,c)->c.equal(r.get("status"),status));if(prioridade!=null)x=x.and((r,q,c)->c.equal(r.get("prioridade"),prioridade));if(setorId!=null)x=x.and((r,q,c)->c.equal(r.get("categoria").get("setor").get("id"),setorId));if(categoriaId!=null)x=x.and((r,q,c)->c.equal(r.get("categoria").get("id"),categoriaId));if(solicitanteId!=null)x=x.and((r,q,c)->c.equal(r.get("solicitante").get("id"),solicitanteId));if(tecnicoId!=null)x=x.and((r,q,c)->c.equal(r.get("tecnico").get("id"),tecnicoId));if(inicio!=null)x=x.and((r,q,c)->c.greaterThanOrEqualTo(r.get("dataAbertura"),inicio));if(fim!=null)x=x.and((r,q,c)->c.lessThanOrEqualTo(r.get("dataAbertura"),fim));return chamados.findAll(x,page(p));}
    public Chamado detalhar(Long id){return chamado(id);}
    public Chamado assumir(Long id,Long tecnicoId){Chamado c=chamado(id);Usuario t=usuario(tecnicoId);if(t.getTipo()!=TipoUsuario.TECNICO||t.getSetor()==null||!t.getSetor().getId().equals(c.getCategoria().getSetor().getId()))throw new RegraNegocio("Técnico não pertence ao setor da categoria.");if(c.getStatus()!=StatusChamado.ABERTO)throw new RegraNegocio("Somente chamados abertos podem ser assumidos.");c.assumir(t);return chamados.save(c);}
    public Chamado resolver(Long id,SolucaoRequest r){Chamado c=chamado(id);Usuario t=usuario(r.tecnicoId());if(c.getStatus()!=StatusChamado.EM_ATENDIMENTO||c.getTecnico()==null||!c.getTecnico().getId().equals(t.getId()))throw new RegraNegocio("Somente o técnico atribuído pode resolver.");c.resolver(r.solucao());return chamados.save(c);}
    public Chamado cancelar(Long id,Long usuarioId){Chamado c=chamado(id);if(c.getStatus()!=StatusChamado.ABERTO||!c.getSolicitante().getId().equals(usuarioId))throw new RegraNegocio("Somente o solicitante pode cancelar um chamado aberto.");c.cancelar();return chamados.save(c);}
    public Interacao adicionarInteracao(Long id,InteracaoRequest r){Chamado c=chamado(id);Usuario a=usuario(r.autorId());if(c.getStatus()==StatusChamado.RESOLVIDO||c.getStatus()==StatusChamado.CANCELADO)throw new RegraNegocio("Chamado encerrado não aceita interações.");if(!a.getId().equals(c.getSolicitante().getId())&&(c.getTecnico()==null||!a.getId().equals(c.getTecnico().getId())))throw new RegraNegocio("Usuário não participa deste chamado.");return interacoes.save(new Interacao(r.texto(),c,a));}
    public Page<Chamado> doUsuario(Long id,StatusChamado status,Pageable p){if(!usuarios.existsById(id))throw new NaoEncontrado("Usuário não encontrado.");Specification<Chamado>x=(r,q,c)->c.or(c.equal(r.get("solicitante").get("id"),id),c.equal(r.get("tecnico").get("id"),id));if(status!=null)x=x.and((r,q,c)->c.equal(r.get("status"),status));return chamados.findAll(x,page(p));}
}
