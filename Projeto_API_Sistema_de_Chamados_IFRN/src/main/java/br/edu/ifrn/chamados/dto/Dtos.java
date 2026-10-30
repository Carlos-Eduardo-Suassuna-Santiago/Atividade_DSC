package br.edu.ifrn.chamados.dto;

import br.edu.ifrn.chamados.model.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public final class Dtos {
    private Dtos() {}
    public record SetorRequest(@NotBlank String nome,@NotBlank String sigla) {}
    public record CategoriaRequest(@NotBlank String nome,String descricao,@NotNull Long setorId) {}
    public record UsuarioRequest(@NotBlank String nome,@NotBlank @Email String email,@NotBlank String matricula,@NotNull TipoUsuario tipo,Long setorId) {}
    public record ChamadoRequest(@NotBlank String titulo,@NotBlank String descricao,@NotBlank String local,Prioridade prioridade,@NotNull Long solicitanteId,@NotNull Long categoriaId) {}
    public record SolucaoRequest(@NotBlank String solucao,@NotNull Long tecnicoId) {}
    public record AcaoRequest(@NotNull Long usuarioId) {}
    public record InteracaoRequest(@NotBlank String texto,@NotNull Long autorId) {}
    public record Pagina<T>(List<T> itens,int pagina,int tamanho,long totalElementos,int totalPaginas) {}
    public record InteracaoResponse(Long id,String texto,LocalDateTime dataHora,Long autorId,String autorNome) {}
    public record ChamadoResponse(Long id,String titulo,String descricao,String local,Prioridade prioridade,StatusChamado status,LocalDateTime dataAbertura,LocalDateTime dataFechamento,String solucao,Long solicitanteId,Long categoriaId,Long setorId,Long tecnicoId,List<InteracaoResponse> interacoes) {}
}
