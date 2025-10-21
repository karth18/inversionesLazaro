package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.Categoria;
import pe.com.isil.inversioneslazaro.model.TipoProducto;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoProductoRepository extends JpaRepository<TipoProducto, Long> {
    Optional<TipoProducto> findByNombre(String nombre);

    List<TipoProducto> findByEstadoTrue();
    List<TipoProducto> findByEstadoFalse();
}
