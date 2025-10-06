package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.CategoriaProducto;

import java.util.Optional;

@Repository

public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, Long>{

    Optional<CategoriaProducto> findBySlug(String slug);
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsBySlug(String slug);

}
