package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.PedidoSeguimiento;
@Repository
public interface PedidoSeguimientoRepository extends JpaRepository<PedidoSeguimiento, Long> {
}
