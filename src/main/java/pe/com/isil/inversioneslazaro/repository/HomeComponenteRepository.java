package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.HomeComponente;
import java.util.List;

@Repository
public interface HomeComponenteRepository extends JpaRepository<HomeComponente, Long> {

    // Busca todos los componentes de una sección específica que estén activos
    // y los ordena por el campo 'orden'
    List<HomeComponente> findBySeccionAndEstaActivoTrueOrderByOrdenAsc(HomeComponente.Seccion seccion);

    // Busca todos por sección (para el panel de admin)
    List<HomeComponente> findBySeccionOrderByOrdenAsc(HomeComponente.Seccion seccion);
}