package br.edu.ifrn.chamados.controller;
import br.edu.ifrn.chamados.dto.Dtos.*;
import br.edu.ifrn.chamados.model.*;
import br.edu.ifrn.chamados.service.ChamadoService;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;
@RestController @RequestMapping("/usuarios")
public class UsuarioChamadoController {
    private final ChamadoService service;
    public UsuarioChamadoController(ChamadoService s){service=s;}
    @GetMapping("/{id}/chamados") public Pagina<ChamadoResponse> listar(@PathVariable Long id,@RequestParam(required=false) StatusChamado status,Pageable p){Page<Chamado> page=service.doUsuario(id,status,p);return new Pagina<>(page.getContent().stream().map(c->new ChamadoResponse(c.getId(),c.getTitulo(),c.getDescricao(),c.getLocal(),c.getPrioridade(),c.getStatus(),c.getDataAbertura(),c.getDataFechamento(),c.getSolucao(),c.getSolicitante().getId(),c.getCategoria().getId(),c.getCategoria().getSetor().getId(),c.getTecnico()==null?null:c.getTecnico().getId(),c.getInteracoes().stream().map(i->new InteracaoResponse(i.getId(),i.getTexto(),i.getDataHora(),i.getAutor().getId(),i.getAutor().getNome())).collect(Collectors.toList()))).toList(),page.getNumber(),page.getSize(),page.getTotalElements(),page.getTotalPages());}
}
