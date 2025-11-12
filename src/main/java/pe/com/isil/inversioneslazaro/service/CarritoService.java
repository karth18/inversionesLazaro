package pe.com.isil.inversioneslazaro.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.isil.inversioneslazaro.dto.CarritoItem;
import pe.com.isil.inversioneslazaro.model.CarritoItemPersistente;
import pe.com.isil.inversioneslazaro.model.Producto;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.CarritoItemPersistenteRepository;
import pe.com.isil.inversioneslazaro.repository.ProductoRepository;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarritoService {

    @Autowired private HttpSession httpSession;
    @Autowired private CarritoItemPersistenteRepository carritoRepo;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    public static final String CARRITO_SESSION_KEY = "carrito";

    private Optional<Usuario> getUsuarioLogueado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return usuarioRepository.findByEmail(auth.getName());
    }

    private Map<Long, CarritoItem> getSessionCart() {
        Map<Long, CarritoItem> carrito = (Map<Long, CarritoItem>) httpSession.getAttribute(CARRITO_SESSION_KEY);
        if (carrito == null) {
            carrito = new HashMap<>();
            httpSession.setAttribute(CARRITO_SESSION_KEY, carrito);
        }
        return carrito;
    }

    @Transactional(readOnly = true)
    public Collection<CarritoItem> getItems() {
        Optional<Usuario> usuarioOpt = getUsuarioLogueado();
        if (usuarioOpt.isPresent()) {
            List<CarritoItemPersistente> itemsDB = carritoRepo.findByUsuario(usuarioOpt.get());
            return itemsDB.stream()
                    .map(itemDB -> {
                        CarritoItem dto = new CarritoItem(itemDB.getProducto());
                        dto.setCantidad(itemDB.getCantidad());
                        dto.setSeleccionado(itemDB.isSeleccionado());
                        return dto;
                    })
                    .collect(Collectors.toList());
        } else {
            return getSessionCart().values();
        }
    }

    @Transactional
    public void agregarAlCarrito(Long productoId, int cantidad) {
        // (Tu lógica de agregar al carrito está bien, no cambia)
        Optional<Usuario> usuarioOpt = getUsuarioLogueado();
        Producto producto = productoRepository.findById(productoId).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            Optional<CarritoItemPersistente> itemOpt = carritoRepo.findByUsuarioAndProducto(usuario, producto);
            int cantidadActual = itemOpt.map(CarritoItemPersistente::getCantidad).orElse(0);
            if (cantidadActual + cantidad > producto.getStock()) {
                throw new RuntimeException("Stock insuficiente");
            }
            if (itemOpt.isPresent()) {
                CarritoItemPersistente item = itemOpt.get();
                item.setCantidad(cantidadActual + cantidad);
                item.setSeleccionado(true);
                carritoRepo.save(item);
            } else {
                CarritoItemPersistente newItem = new CarritoItemPersistente(usuario, producto, cantidad, true);
                carritoRepo.save(newItem);
            }
        } else {
            Map<Long, CarritoItem> carrito = getSessionCart();
            int cantidadActual = 0;
            if (carrito.containsKey(productoId)) {
                cantidadActual = carrito.get(productoId).getCantidad();
            }
            if (cantidadActual + cantidad > producto.getStock()) {
                throw new RuntimeException("Stock insuficiente");
            }
            if (carrito.containsKey(productoId)) {
                CarritoItem item = carrito.get(productoId);
                item.setCantidad(item.getCantidad() + cantidad);
            } else {
                CarritoItem newItem = new CarritoItem(producto);
                newItem.setCantidad(cantidad);
                carrito.put(productoId, newItem);
            }
            httpSession.setAttribute(CARRITO_SESSION_KEY, carrito);
        }
    }

    /**
     * MÉTODO HÍBRIDO: Actualiza un item Y DEVUELVE LOS NUEVOS TOTALES
     */
    @Transactional
    // --- CAMBIO 1: El método ahora devuelve un Map ---
    public Map<String, BigDecimal> actualizarItem(Long productoId, int cantidad, boolean seleccionado) {
        Optional<Usuario> usuarioOpt = getUsuarioLogueado();
        Producto producto = productoRepository.findById(productoId).orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (usuarioOpt.isPresent()) {
            // --- Lógica de BD (Logueado) ---
            Usuario usuario = usuarioOpt.get();
            Optional<CarritoItemPersistente> itemOpt = carritoRepo.findByUsuarioAndProducto(usuario, producto);

            if (itemOpt.isPresent()) {
                CarritoItemPersistente item = itemOpt.get();
                if (cantidad <= 0) {
                    carritoRepo.delete(item);
                } else {
                    if (cantidad > producto.getStock()) {
                        item.setCantidad(producto.getStock());
                    } else {
                        item.setCantidad(cantidad);
                    }
                    item.setSeleccionado(seleccionado);
                    carritoRepo.save(item);
                }
            }
        } else {
            // --- Lógica de Sesión (Invitado) ---
            Map<Long, CarritoItem> carrito = getSessionCart();
            CarritoItem item = carrito.get(productoId);
            if (item != null) {
                if (cantidad > 0) {
                    item.setCantidad(Math.min(cantidad, item.getStock()));
                } else {
                    carrito.remove(productoId);
                    httpSession.setAttribute(CARRITO_SESSION_KEY, carrito);
                }
                item.setSeleccionado(seleccionado);
                httpSession.setAttribute(CARRITO_SESSION_KEY, carrito);
            }
        }

        // --- CAMBIO 2: Llama a calcularTotales DESDE DENTRO de la transacción ---
        return calcularTotales();
    }

    /**
     * MÉTODO HÍBRIDO: Elimina un item Y DEVUELVE LOS NUEVOS TOTALES
     */
    @Transactional
    // --- CAMBIO 3: El método ahora devuelve un Map ---
    public Map<String, BigDecimal> eliminarItem(Long productoId) {
        Optional<Usuario> usuarioOpt = getUsuarioLogueado();

        if (usuarioOpt.isPresent()) {
            // --- Lógica de BD (Logueado) ---
            Usuario usuario = usuarioOpt.get();
            Producto producto = productoRepository.findById(productoId).orElseThrow();
            carritoRepo.deleteByUsuarioAndProducto(usuario, producto);
        } else {
            // --- Lógica de Sesión (Invitado) ---
            Map<Long, CarritoItem> carrito = getSessionCart();
            carrito.remove(productoId);
            httpSession.setAttribute(CARRITO_SESSION_KEY, carrito);
        }

        // --- CAMBIO 4: Llama a calcularTotales DESDE DENTRO de la transacción ---
        return calcularTotales();
    }

    /**
     * CALCULAR TOTALES
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> calcularTotales() {
        Collection<CarritoItem> items = getItems(); // Esto llamará al 'getItems' transaccional

        BigDecimal subtotalOriginal = BigDecimal.ZERO;
        BigDecimal ahorroPorProductos = BigDecimal.ZERO;
        BigDecimal descuentoPorCupon = BigDecimal.ZERO;

        for (CarritoItem item : items) {
            if (item.isSeleccionado()) {
                subtotalOriginal = subtotalOriginal.add(item.getSubtotalOriginal());
                if (item.isTieneDescuento()) {
                    BigDecimal ahorroUnitario = item.getPrecioSinDescuento().subtract(item.getPrecioUnitario());
                    BigDecimal ahorroTotalItem = ahorroUnitario.multiply(new BigDecimal(item.getCantidad()));
                    ahorroPorProductos = ahorroPorProductos.add(ahorroTotalItem);
                }
            }
        }
        BigDecimal total = subtotalOriginal.subtract(ahorroPorProductos).subtract(descuentoPorCupon);
        Map<String, BigDecimal> totales = new HashMap<>();
        totales.put("subtotal", subtotalOriginal);
        totales.put("ahorroPorProductos", ahorroPorProductos);
        totales.put("descuentoPorCupon", descuentoPorCupon);
        totales.put("total", total);
        return totales;
    }

    // (migrarCarritoSesionADb y agregarAlCarritoDb se quedan igual)
    @Transactional
    public void migrarCarritoSesionADb() {
        Usuario usuario = getUsuarioLogueado().orElse(null);
        if (usuario == null) return;
        Map<Long, CarritoItem> sessionCart = getSessionCart();
        if (sessionCart.isEmpty()) return;
        for (CarritoItem sessionItem : sessionCart.values()) {
            try {
                agregarAlCarritoDb(usuario, sessionItem.getProductoId(), sessionItem.getCantidad());
            } catch (Exception e) {
                System.err.println("Error migrando item: " + e.getMessage());
            }
        }
        httpSession.removeAttribute(CARRITO_SESSION_KEY);
    }

    @Transactional
    private void agregarAlCarritoDb(Usuario usuario, Long productoId, int cantidad) {
        Producto producto = productoRepository.findById(productoId).orElseThrow();
        Optional<CarritoItemPersistente> itemOpt = carritoRepo.findByUsuarioAndProducto(usuario, producto);
        if (itemOpt.isPresent()) {
            CarritoItemPersistente item = itemOpt.get();
            int nuevaCantidad = item.getCantidad() + cantidad;
            if (nuevaCantidad > producto.getStock()) {
                nuevaCantidad = producto.getStock();
            }
            item.setCantidad(nuevaCantidad);
            item.setSeleccionado(true);
            carritoRepo.save(item);
        } else {
            if (cantidad > producto.getStock()) {
                cantidad = producto.getStock();
            }
            CarritoItemPersistente newItem = new CarritoItemPersistente(usuario, producto, cantidad, true);
            carritoRepo.save(newItem);
        }
    }
}