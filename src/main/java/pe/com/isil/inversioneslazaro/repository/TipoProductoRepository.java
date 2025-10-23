package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.TipoProducto;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoProductoRepository extends JpaRepository<TipoProducto, Long> {
    Optional<TipoProducto> findByNombre(String nombre);
//    List<TipoProducto> findByIdCate(long id);

    @Query("SELECT t FROM TipoProducto t WHERE t.idCate.id = :idCategoria")
    List<TipoProducto> findTiposByCategoriaId(@Param("idCategoria") Long idCategoria);

    List<TipoProducto> findByEstadoTrue();
    List<TipoProducto> findByEstadoFalse();
}
