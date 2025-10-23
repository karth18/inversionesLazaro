package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.isil.inversioneslazaro.model.Venta;

import java.time.LocalDateTime;
@SuppressWarnings("unused")
public interface VentaRepository extends JpaRepository<Venta, Long> {
//    Page<Venta> findByAll(Pageable pageable);
//    Page<Venta> findByFechaBetween(LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
//    Page<Venta> findByClienteId(Long clienteId, Pageable pageable);
//    Page<Venta> findByClienteIdAndFechaBetween(Long clienteId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
}
