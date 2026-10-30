package br.edu.ifrn.chamados.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity @Table(name = "categorias")
public class Categoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String nome;
    @Column(length = 500) private String descricao;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "setor_id", nullable = false) private Setor setor;
    protected Categoria() {}
    public Categoria(String nome, String descricao, Setor setor) { this.nome = nome; this.descricao = descricao; this.setor = setor; }
    public Long getId() { return id; } public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public String getDescricao() { return descricao; } public void setDescricao(String v) { descricao = v; }
    public Setor getSetor() { return setor; } public void setSetor(Setor v) { setor = v; }
}
