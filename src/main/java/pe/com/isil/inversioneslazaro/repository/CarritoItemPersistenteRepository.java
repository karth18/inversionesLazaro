package pe.com.isil.inversioneslazaro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pe.com.isil.inversioneslazaro.model.CarritoItemPersistente;
import pe.com.isil.inversioneslazaro.model.Producto;
import pe.com.isil.inversioneslazaro.model.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarritoItemPersistenteRepository extends JpaRepository<CarritoItemPersistente, Long> {

    // Busca todos los items del carrito para un usuario (para mostrar el carrito)
    List<CarritoItemPersistente> findByUsuario(Usuario usuario);

    // Busca un item específico (para agregar/actualizar cantidad)
    Optional<CarritoItemPersistente> findByUsuarioAndProducto(Usuario usuario, Producto producto);

    // Elimina un item del carrito (usa borrado físico como pediste)
    @Transactional
    void deleteByUsuarioAndProducto(Usuario usuario, Producto producto);

    // (Opcional pero recomendado) Un método para limpiar el carrito después de un pedido
    @Transactional
    void deleteByUsuarioAndSeleccionadoTrue(Usuario usuario);
}