package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.BannerHome;

import java.util.List;
import java.util.Optional;

@Repository
public interface BannerHomeRepository extends JpaRepository<BannerHome, Long> {

    // Busca el primer banner que esté marcado como "activo"
    Optional<BannerHome> findFirstByEstaActivoTrue();
    List<BannerHome> findByEstaActivoTrueOrderByOrdenAsc(Pageable pageable);
    Optional<BannerHome>findByOrden(Integer orden);
    @Query("SELECT MAX(b.orden) FROM BannerHome b")
    Integer findMaxOrden();
}