package br.edu.ifrn.chamados.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity @Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(name = "uk_usuario_email", columnNames = "email"))
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 150) private String nome;
    @Column(nullable = false, length = 180) private String email;
    @Column(nullable = false, length = 30) private String matricula;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TipoUsuario tipo;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "setor_id") private Setor setor;
    protected Usuario() {}
    public Usuario(String nome, String email, String matricula, TipoUsuario tipo, Setor setor) { this.nome=nome; this.email=email; this.matricula=matricula; this.tipo=tipo; this.setor=setor; }
    public Long getId(){return id;} public String getNome(){return nome;} public void setNome(String v){nome=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getMatricula(){return matricula;} public void setMatricula(String v){matricula=v;}
    public TipoUsuario getTipo(){return tipo;} public void setTipo(TipoUsuario v){tipo=v;} public Setor getSetor(){return setor;} public void setSetor(Setor v){setor=v;}
}
