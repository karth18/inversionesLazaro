package pe.com.isil.inversioneslazaro.repository;

import com.stripe.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.HomeComponente;
import java.util.List;
import java.util.Optional;

@Repository
public interface HomeComponenteRepository extends JpaRepository<HomeComponente, Long> {

    // Busca todos los componentes de una sección específica que estén activos
    // y los ordena por el campo 'orden'
    List<HomeComponente> findBySeccionAndEstaActivoTrueOrderByOrdenAsc(HomeComponente.Seccion seccion);

    // Busca todos por sección (para el panel de admin)
    List<HomeComponente> findBySeccionOrderByOrdenAsc(HomeComponente.Seccion seccion);

    Optional<HomeComponente> findBySeccionAndOrden(HomeComponente.Seccion seccion, Integer orden);

    @Query("SELECT MAX(c.orden) FROM HomeComponente c WHERE c.seccion = :seccion")
    Integer findMaxOrdenBySeccion(@Param("seccion") HomeComponente.Seccion seccion);
}