package pe.com.isil.inversioneslazaro.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.Usuario;


import java.util.Optional;
@SuppressWarnings("unused")
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Page<Usuario> findByEstadoTrue(Pageable pageable);
    Optional<Usuario> findByEmail(String email);
    Page<Usuario> findByDniContainingIgnoreCaseOrEmailContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
            String dni, String email, String nombres, String apellidos, Pageable pageable);

    Usuario findByTokenVerificacion(String token);

    boolean existsByDni(String dni);
    boolean existsByEmail(String email);

}
