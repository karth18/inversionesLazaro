package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pe.com.isil.inversioneslazaro.model.ConfiguracionEmpresa;

@Repository
public interface ConfiguracionEmpresaRepository extends JpaRepository<ConfiguracionEmpresa, Long> {

}
