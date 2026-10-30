package br.edu.ifrn.chamados.repository;
import br.edu.ifrn.chamados.model.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface ChamadoRepository extends JpaRepository<Chamado,Long>, JpaSpecificationExecutor<Chamado> {
    boolean existsByCategoriaSetorId(Long setorId);
    boolean existsByCategoriaId(Long categoriaId);
    boolean existsBySolicitanteId(Long id);
}
