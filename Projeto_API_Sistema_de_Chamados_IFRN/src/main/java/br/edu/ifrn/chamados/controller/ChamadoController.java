package br.edu.ifrn.chamados.controller;

import br.edu.ifrn.chamados.dto.Dtos.*;
import br.edu.ifrn.chamados.model.*;
import br.edu.ifrn.chamados.service.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController @RequestMapping("/chamados")
public class ChamadoController {
    private final ChamadoService service;
    public ChamadoController(ChamadoService s){service=s;}
    private ChamadoResponse dto(Chamado c){return new ChamadoResponse(c.getId(),c.getTitulo(),c.getDescricao(),c.getLocal(),c.getPrioridade(),c.getStatus(),c.getDataAbertura(),c.getDataFechamento(),c.getSolucao(),c.getSolicitante().getId(),c.getCategoria().getId(),c.getCategoria().getSetor().getId(),c.getTecnico()==null?null:c.getTecnico().getId(),c.getInteracoes().stream().map(i->new InteracaoResponse(i.getId(),i.getTexto(),i.getDataHora(),i.getAutor().getId(),i.getAutor().getNome())).collect(Collectors.toList()));}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ChamadoResponse abrir(@Valid @RequestBody ChamadoRequest r){return dto(service.abrir(r));}
    @GetMapping public Pagina<ChamadoResponse> listar(@RequestParam(required=false) StatusChamado status,@RequestParam(required=false) Prioridade prioridade,@RequestParam(required=false) Long setorId,@RequestParam(required=false) Long categoriaId,@RequestParam(required=false) Long solicitanteId,@RequestParam(required=false) Long tecnicoId,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,Pageable p){Page<Chamado> page=service.listar(status,prioridade,setorId,categoriaId,solicitanteId,tecnicoId,dataInicio,dataFim,p);return new Pagina<>(page.map(this::dto).getContent(),page.getNumber(),page.getSize(),page.getTotalElements(),page.getTotalPages());}
    @GetMapping("/{id}") public ChamadoResponse detalhe(@PathVariable Long id){return dto(service.detalhar(id));}
    @PatchMapping("/{id}/assumir") public ChamadoResponse assumir(@PathVariable Long id,@Valid @RequestBody AcaoRequest r){return dto(service.assumir(id,r.usuarioId()));}
    @PatchMapping("/{id}/resolver") public ChamadoResponse resolver(@PathVariable Long id,@Valid @RequestBody SolucaoRequest r){return dto(service.resolver(id,r));}
    @PatchMapping("/{id}/cancelar") public ChamadoResponse cancelar(@PathVariable Long id,@Valid @RequestBody AcaoRequest r){return dto(service.cancelar(id,r.usuarioId()));}
    @PostMapping("/{id}/interacoes") @ResponseStatus(HttpStatus.CREATED) public InteracaoResponse interacao(@PathVariable Long id,@Valid @RequestBody InteracaoRequest r){Interacao i=service.adicionarInteracao(id,r);return new InteracaoResponse(i.getId(),i.getTexto(),i.getDataHora(),i.getAutor().getId(),i.getAutor().getNome());}
}
