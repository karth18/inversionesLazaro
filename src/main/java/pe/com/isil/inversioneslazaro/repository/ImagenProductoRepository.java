package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.ImagenProducto;

@Repository
public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Long> {
    // solo si se necesita aun esta en proceso
    // Optional<ImagenProducto> findByProducto_IdAndEsPrincipalTrue(Long productoId);
}