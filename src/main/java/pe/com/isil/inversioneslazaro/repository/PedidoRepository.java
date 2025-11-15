package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.Pedido;
import pe.com.isil.inversioneslazaro.model.Usuario;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Page<Pedido> findByUsuario(Usuario usuario, Pageable pageable);

    @Query("SELECT p FROM Pedido p WHERE " +
            "LOWER(p.codigoPedido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(p.usuario.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Pedido> searchByCodigoOrEmail(@Param("busqueda") String busqueda, Pageable pageable);
}