package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.Direccion;
import pe.com.isil.inversioneslazaro.model.Usuario;

import java.util.List;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Long> {
    List<Direccion> findByUsuarioId(Integer usuarioId);
    List<Direccion> findByUsuarioIdOrderByEsPrincipalDesc(Integer usuarioId);
    List<Direccion> findByUsuario(Usuario usuario);
    @Modifying
    @Query("UPDATE Direccion d SET d.esPrincipal = false WHERE d.usuario.id = :usuarioId")
    void desmarcarTodasPrincipales(@Param("usuarioId") Integer usuarioId);
}
