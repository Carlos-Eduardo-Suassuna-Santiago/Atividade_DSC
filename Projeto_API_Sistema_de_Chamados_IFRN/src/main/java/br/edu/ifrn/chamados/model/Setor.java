package br.edu.ifrn.chamados.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "setores", uniqueConstraints = @UniqueConstraint(name = "uk_setor_sigla", columnNames = "sigla"))
public class Setor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120) private String nome;
    @Column(nullable = false, length = 20) private String sigla;
    @JsonIgnore @OneToMany(mappedBy = "setor") private List<Categoria> categorias = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "setor") private List<Usuario> usuarios = new ArrayList<>();
    protected Setor() {}
    public Setor(String nome, String sigla) { this.nome = nome; this.sigla = sigla; }
    public Long getId() { return id; } public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public String getSigla() { return sigla; } public void setSigla(String v) { sigla = v; }
}
