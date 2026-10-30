package br.edu.ifrn.chamados.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity @Table(name="interacoes")
public class Interacao {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=2000) private String texto;
    @Column(name="data_hora",nullable=false) private LocalDateTime dataHora;
    @JsonIgnore @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="chamado_id",nullable=false) private Chamado chamado;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="autor_id",nullable=false) private Usuario autor;
    protected Interacao(){}
    public Interacao(String texto,Chamado chamado,Usuario autor){this.texto=texto;this.chamado=chamado;this.autor=autor;this.dataHora=LocalDateTime.now();}
    public Long getId(){return id;} public String getTexto(){return texto;} public LocalDateTime getDataHora(){return dataHora;} public Usuario getAutor(){return autor;} public Chamado getChamado(){return chamado;}
}
