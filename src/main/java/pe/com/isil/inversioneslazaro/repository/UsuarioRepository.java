package pe.com.isil.inversioneslazaro.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.Usuario;


import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByEmail(String email);
    Boolean existsByEmail(String email);
    boolean existsByDni(String dni);

    Page<Usuario> findByDniContainingIgnoreCase(String dni, Pageable pageable);




}
