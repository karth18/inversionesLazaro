package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.Distrito;

import java.util.List;

@Repository
public interface DistritoRepository  extends JpaRepository<Distrito, Long> {

    List<Distrito> findByProvincia_IdOrderByNombreAsc(Long provinciaId);
}
