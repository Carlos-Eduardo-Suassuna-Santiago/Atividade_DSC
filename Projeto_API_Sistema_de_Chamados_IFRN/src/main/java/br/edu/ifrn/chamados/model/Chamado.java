package br.edu.ifrn.chamados.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "chamados")
public class Chamado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=180) private String titulo;
    @Column(nullable=false,length=2000) private String descricao;
    @Column(nullable=false,length=180) private String local;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=10) private Prioridade prioridade;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private StatusChamado status;
    @Column(name="data_abertura",nullable=false) private LocalDateTime dataAbertura;
    @Column(name="data_fechamento") private LocalDateTime dataFechamento;
    @Column(length=2000) private String solucao;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="solicitante_id",nullable=false) private Usuario solicitante;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="categoria_id",nullable=false) private Categoria categoria;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="tecnico_id") private Usuario tecnico;
    @OneToMany(mappedBy="chamado",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("dataHora ASC") private List<Interacao> interacoes=new ArrayList<>();
    protected Chamado(){}
    public Chamado(String titulo,String descricao,String local,Prioridade prioridade,Usuario solicitante,Categoria categoria){this.titulo=titulo;this.descricao=descricao;this.local=local;this.prioridade=prioridade==null?Prioridade.MEDIA:prioridade;this.status=StatusChamado.ABERTO;this.dataAbertura=LocalDateTime.now();this.solicitante=solicitante;this.categoria=categoria;}
    public Long getId(){return id;} public String getTitulo(){return titulo;} public String getDescricao(){return descricao;} public String getLocal(){return local;} public Prioridade getPrioridade(){return prioridade;} public StatusChamado getStatus(){return status;} public LocalDateTime getDataAbertura(){return dataAbertura;} public LocalDateTime getDataFechamento(){return dataFechamento;} public String getSolucao(){return solucao;} public Usuario getSolicitante(){return solicitante;} public Categoria getCategoria(){return categoria;} public Usuario getTecnico(){return tecnico;} public List<Interacao> getInteracoes(){return interacoes;}
    public void assumir(Usuario t){tecnico=t;status=StatusChamado.EM_ATENDIMENTO;} public void resolver(String s){solucao=s;status=StatusChamado.RESOLVIDO;dataFechamento=LocalDateTime.now();} public void cancelar(){status=StatusChamado.CANCELADO;dataFechamento=LocalDateTime.now();}
}
