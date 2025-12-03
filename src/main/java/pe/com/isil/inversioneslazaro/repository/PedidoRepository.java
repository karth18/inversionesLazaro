package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.isil.inversioneslazaro.model.Pedido;
import pe.com.isil.inversioneslazaro.model.Usuario;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Page<Pedido> findByUsuario(Usuario usuario, Pageable pageable);

    @Query("SELECT p FROM Pedido p WHERE " +
            "LOWER(p.codigoPedido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(p.usuario.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Pedido> searchByCodigoOrEmail(@Param("busqueda") String busqueda, Pageable pageable);

    // Busca por código (ignorando mayúsculas/minúsculas) y filtra por usuario
    Page<Pedido> findByUsuarioAndCodigoPedidoContainingIgnoreCase(Usuario usuario, String codigo, Pageable pageable);

    // NUEVO: Para que Almacén vea solo los "ORDEN_RECIBIDA"
    List<Pedido> findByEstado(Pedido.EstadoPedido estado);

    // NUEVO: Para que el Chofer vea solo SUS pedidos que están "EN_CAMINO"
    List<Pedido> findByChoferAndEstado(Usuario chofer, Pedido.EstadoPedido estado);
    List<Pedido> findByAlmaceneroAndEstado(Usuario a, Pedido.EstadoPedido e);
}