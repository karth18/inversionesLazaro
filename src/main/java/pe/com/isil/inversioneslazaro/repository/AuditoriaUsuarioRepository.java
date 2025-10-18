package pe.com.isil.inversioneslazaro.repository;

import jakarta.persistence.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.AuditoriaUsuario;
import pe.com.isil.inversioneslazaro.model.Usuario;


@Repository
public interface AuditoriaUsuarioRepository extends JpaRepository<AuditoriaUsuario, Integer> {

    @Query("""
        SELECT a FROM AuditoriaUsuario a
        WHERE (:filtro IS NULL OR :filtro = '' 
            OR LOWER(a.realizadoPor) LIKE LOWER(CONCAT('%', :filtro, '%'))
            OR LOWER(a.accion) LIKE LOWER(CONCAT('%', :filtro, '%'))
            OR LOWER(a.usuarioAfectado.dni) LIKE LOWER(CONCAT('%', :filtro, '%'))
            OR LOWER(a.usuarioAfectado.email) LIKE LOWER(CONCAT('%', :filtro, '%'))            
        )
        ORDER BY a.fechaAccion DESC
    """)
    Page<AuditoriaUsuario> buscarAuditoria(@Param("filtro") String filtro, Pageable pageable);

}
