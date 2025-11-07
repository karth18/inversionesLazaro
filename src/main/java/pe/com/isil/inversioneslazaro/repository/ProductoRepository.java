package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.Categoria;
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


    //nuevo metodo para poder hacer dinamico el inicio
    /**
     * Trae productos con precio de oferta válido
     */
    @Query("SELECT p FROM Producto p WHERE p.estado = true AND p.precioOferta IS NOT NULL AND p.precioOferta < p.precio")
    List<Producto> findProductosEnOferta(Pageable pageable);

    /**
     * Trae productos marcados como "Destacados"
     */
    List<Producto> findByEsDestacadoTrueAndEstadoTrue(Pageable pageable);

    /**
     * Trae productos de una categoría específica (para las secciones)
     */
    List<Producto> findByIdCateAndEstadoTrue(Categoria categoria, Pageable pageable);
    @Query(value = "SELECT * FROM producto WHERE estado = true ORDER BY RAND()", nativeQuery = true)
    List<Producto> findRandomProductos(Pageable pageable);

}
