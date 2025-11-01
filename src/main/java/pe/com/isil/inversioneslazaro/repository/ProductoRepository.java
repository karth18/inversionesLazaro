package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.Producto;

import java.util.List;
import java.util.Optional;
@SuppressWarnings("unused")
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodPro(String codPro);
    boolean existsByCodPro(String codPro);
    Page<Producto> findByEstadoIsTrue(Pageable pageable);
    Page<Producto> findByNomProContainingAndEstadoIsTrue(String nombre, Pageable pageable);
    Page<Producto> findByNomProContaining(String nombre, Pageable pageable);
    Page<Producto> findByEstado(boolean estado, Pageable pageable);
    Page<Producto> findByNomProContainingAndEstado(String nombre, boolean estado, Pageable pageable);
    List<Producto> findByEstadoIsTrue();


}
