package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.Auditoria;

@SuppressWarnings("unused")
@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    @Query("""
        SELECT a FROM Auditoria a
        WHERE (:filtro IS NULL OR :filtro = ''
            OR LOWER(a.realizadoPor) LIKE LOWER(CONCAT('%', :filtro, '%'))
            OR LOWER(a.accion) LIKE LOWER(CONCAT('%', :filtro, '%'))
            OR LOWER(a.entidadAfectada) LIKE LOWER(CONCAT('%', :filtro, '%'))
            OR LOWER(a.entidadId) LIKE LOWER(CONCAT('%', :filtro, '%'))
        )
        ORDER BY a.fechaAccion DESC
    """)
    Page<Auditoria> buscarAuditoria(@Param("filtro") String filtro, Pageable pageable);

}
