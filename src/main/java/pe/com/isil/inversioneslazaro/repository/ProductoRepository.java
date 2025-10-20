package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.Producto;

import java.util.Optional;
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodPro(String codPro);
    boolean existsByCodPro(String codPro);

    Page<Producto> findByNomProContaining(String nombre, Pageable pageable);
}
