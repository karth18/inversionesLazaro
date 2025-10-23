package pe.com.isil.inversioneslazaro.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.com.isil.inversioneslazaro.dto.CarritoItem;
import pe.com.isil.inversioneslazaro.model.Producto;
import pe.com.isil.inversioneslazaro.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CarritoService {

    public static final String CARRITO_SESSION_KEY = "carrito";

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private HttpSession httpSession;

    // Obtiene el carrito de la sesión
    public Map<Long, CarritoItem> getCarrito() {
        @SuppressWarnings("unchecked")
        Map<Long, CarritoItem> carrito = (Map<Long, CarritoItem>) httpSession.getAttribute(CARRITO_SESSION_KEY);

        if (carrito == null) {
            carrito = new HashMap<>();
            httpSession.setAttribute(CARRITO_SESSION_KEY, carrito);
        }
        return carrito;
    }

    // Añade un producto (el que viene de detail.html)
    public void agregarAlCarrito(Long productoId, int cantidad) {
        Optional<Producto> productoOpt = productoRepository.findById(productoId);
        if (productoOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado");
        }

        Producto producto = productoOpt.get();
        Map<Long, CarritoItem> carrito = getCarrito();

        if (carrito.containsKey(productoId)) {
            // Si ya existe, incrementamos la cantidad
            CarritoItem item = carrito.get(productoId);
            item.setCantidad(item.getCantidad() + cantidad);
        } else {
            // Si es nuevo, lo creamos
            CarritoItem newItem = new CarritoItem(producto);
            newItem.setCantidad(cantidad);
            carrito.put(productoId, newItem);
        }

        httpSession.setAttribute(CARRITO_SESSION_KEY, carrito);
    }

    // Actualiza la cantidad o el checkbox (desde la vista del carrito)
    public void actualizarItem(Long productoId, int cantidad, boolean seleccionado) {
        Map<Long, CarritoItem> carrito = getCarrito();
        CarritoItem item = carrito.get(productoId);

        if (item != null) {
            if (cantidad > 0) {
                item.setCantidad(cantidad);
            } else {
                carrito.remove(productoId);
                return;
            }
            item.setSeleccionado(seleccionado);
            httpSession.setAttribute(CARRITO_SESSION_KEY, carrito);
        }
    }

    // Elimina un item
    public void eliminarItem(Long productoId) {
        Map<Long, CarritoItem> carrito = getCarrito();
        carrito.remove(productoId);
        httpSession.setAttribute(CARRITO_SESSION_KEY, carrito);
    }

    // Calcula los totales (SOLO de los seleccionados)
    // ... (tus otros métodos como agregarAlCarrito)

    /**
     * Calcula los totales (Subtotal, Descuento, Total)
     * SOLO de los items seleccionados.
     */
    public Map<String, BigDecimal> calcularTotales() {
        Map<Long, CarritoItem> carrito = getCarrito();

        // El subtotal ya se calcula con los precios de oferta (gracias al DTO)
        BigDecimal subtotal = BigDecimal.ZERO;

        // Nuevo: Calculamos cuánto se ahorró por los descuentos de producto
        BigDecimal ahorroPorProductos = BigDecimal.ZERO;

        // Este es para cupones (ej. "CYBER10")
        BigDecimal descuentoPorCupon = BigDecimal.ZERO;

        for (CarritoItem item : carrito.values()) {
            if (item.isSeleccionado()) { // Solo suma si está chequeado

                // 1. Suma el subtotal (ya viene con el descuento)
                subtotal = subtotal.add(item.getSubtotal());

                // 2. Calcula el ahorro
                if (item.isTieneDescuento()) {
                    // Ahorro por 1 unidad
                    BigDecimal ahorroUnitario = item.getPrecioSinDescuento().subtract(item.getPrecioUnitario());
                    // Ahorro total (ahorro x cantidad)
                    BigDecimal ahorroTotalItem = ahorroUnitario.multiply(new BigDecimal(item.getCantidad()));

                    ahorroPorProductos = ahorroPorProductos.add(ahorroTotalItem);
                }
            }
        }

        // Lógica futura de cupón:
        // if (hayCuponAplicado) {
        //    descuentoPorCupon = subtotal.multiply(new BigDecimal("0.10")); // 10%
        // }

        BigDecimal total = subtotal.subtract(descuentoPorCupon);

        Map<String, BigDecimal> totales = new HashMap<>();
        totales.put("subtotal", subtotal); // Precio ya descontado
        totales.put("ahorroPorProductos", ahorroPorProductos); // Cuánto ahorró
        totales.put("descuentoPorCupon", descuentoPorCupon); // Descuento de cupón
        totales.put("total", total); // Total a pagar

        return totales;
    }
}