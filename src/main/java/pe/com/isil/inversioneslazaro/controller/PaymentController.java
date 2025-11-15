package pe.com.isil.inversioneslazaro.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.com.isil.inversioneslazaro.model.*;
import pe.com.isil.inversioneslazaro.repository.*;
import pe.com.isil.inversioneslazaro.service.CarritoService;
import pe.com.isil.inversioneslazaro.dto.CarritoItem;
import pe.com.isil.inversioneslazaro.service.StripeService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pago")
public class PaymentController {

    @Autowired private CarritoService carritoService;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private DireccionRepository direccionRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private CarritoItemPersistenteRepository carritoItemRepo;

    @Autowired private StripeService stripeService;

    public static record PaymentRequest(String token, Long direccionId, String shippingMethod) {}

    // (DTO para el simulador)
    public static record SimulationRequest(Long direccionId, String shippingMethod) {}


    /**
     * MÉTODO DE PAGO REAL (CON STRIPE)
     */
    @PostMapping("/procesar")
    @Transactional
    public ResponseEntity<Map<String, Object>> procesarPago(
            @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Direccion direccion = direccionRepository.findById(request.direccionId()).orElseThrow(() -> new RuntimeException("Dirección no encontrada"));

            Map<String, BigDecimal> totalesMap = carritoService.calcularTotales();
            BigDecimal totalProductos = totalesMap.get("total");
            BigDecimal costoEnvio = request.shippingMethod().equals("delivery") ? new BigDecimal("10.00") : BigDecimal.ZERO;
            BigDecimal totalFinal = totalProductos.add(costoEnvio);

            Pedido pedido = new Pedido();
            pedido.setUsuario(usuario);
            pedido.setDireccion(direccion);
            pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
            pedido.setTotal(totalFinal);

            List<PedidoDetalle> detalles = new ArrayList<>();
            Collection<CarritoItem> itemsDelCarrito = carritoService.getItems();

            for (CarritoItem item : itemsDelCarrito) {
                if (item.isSeleccionado()) {
                    Producto p = productoRepository.findById(item.getProductoId()).orElseThrow();
                    // Validación de Stock
                    if (p.getStock() < item.getCantidad()) {
                        throw new RuntimeException("Stock insuficiente para: " + p.getNomPro());
                    }
                    detalles.add(new PedidoDetalle(pedido, p, item.getCantidad(), item.getPrecioUnitario()));
                }
            }
            if (detalles.isEmpty()) {
                throw new RuntimeException("No hay items seleccionados en el carrito.");
            }
            pedido.setDetalles(detalles);

            Pedido pedidoGuardado = pedidoRepository.save(pedido);

            // --- INICIO: LÓGICA DE STRIPE ---
            int montoEnCentavos = totalFinal.multiply(new BigDecimal(100)).intValue();
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", montoEnCentavos);
            chargeParams.put("currency", "pen");
            chargeParams.put("description", "Pago por Pedido #" + pedidoGuardado.getCodigoPedido());
            chargeParams.put("source", request.token());

            Map<String, String> metadata = new HashMap<>();
            metadata.put("pedido_id_interno", pedidoGuardado.getId().toString());
            metadata.put("codigo_pedido", pedidoGuardado.getCodigoPedido());
            metadata.put("usuario_email", usuario.getEmail());
            chargeParams.put("metadata", metadata);
            Charge charge = stripeService.createCharge(
                    montoEnCentavos,
                    "pen",
                    "Pago por Pedido #" + pedidoGuardado.getCodigoPedido(),
                    request.token(),
                    metadata
            );
            // --- FIN: LÓGICA DE STRIPE ---

            if ("succeeded".equals(charge.getStatus())) {
                pedidoGuardado.setEstado(Pedido.EstadoPedido.ORDEN_RECIBIDA);
                pedidoGuardado.setStripeChargeId(charge.getId());

                // Descontar Stock
                for (PedidoDetalle detalle : pedidoGuardado.getDetalles()) {
                    Producto p = detalle.getProducto();
                    int nuevoStock = p.getStock() - detalle.getCantidad();
                    p.setStock(Math.max(nuevoStock, 0));
                    productoRepository.save(p);
                }

                pedidoRepository.save(pedidoGuardado);
                carritoItemRepo.deleteByUsuarioAndSeleccionadoTrue(usuario);

                response.put("success", true);
                response.put("pedidoId", pedidoGuardado.getId());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "El pago falló: " + charge.getFailureMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (StripeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    /**
     * ENDPOINT DE SIMULACIÓN (¡AHORA CORREGIDO!)
     * (Copia exacta del método real, pero sin Stripe)
     */
    @PostMapping("/simular")
    @Transactional
    public ResponseEntity<Map<String, Object>> simularPago(
            @RequestBody SimulationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 1. Obtener Usuario y Dirección
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Direccion direccion = direccionRepository.findById(request.direccionId()).orElseThrow(() -> new RuntimeException("Dirección no encontrada"));

            // 2. Calcular el total REAL
            Map<String, BigDecimal> totalesMap = carritoService.calcularTotales();
            BigDecimal totalProductos = totalesMap.get("total");
            BigDecimal costoEnvio = request.shippingMethod().equals("delivery") ? new BigDecimal("10.00") : BigDecimal.ZERO;
            BigDecimal totalFinal = totalProductos.add(costoEnvio);

            // 3. Crear el Pedido PENDIENTE
            Pedido pedido = new Pedido();
            pedido.setUsuario(usuario);
            pedido.setDireccion(direccion);
            pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
            pedido.setTotal(totalFinal);

            // 4. Copiar items Y VALIDAR STOCK
            List<PedidoDetalle> detalles = new ArrayList<>();
            Collection<CarritoItem> itemsDelCarrito = carritoService.getItems();

            for (CarritoItem item : itemsDelCarrito) {
                if (item.isSeleccionado()) {
                    Producto p = productoRepository.findById(item.getProductoId()).orElseThrow();

                    // --- ¡VALIDACIÓN DE STOCK AÑADIDA! ---
                    if (p.getStock() < item.getCantidad()) {
                        throw new RuntimeException("Stock insuficiente para: " + p.getNomPro());
                    }
                    detalles.add(new PedidoDetalle(pedido, p, item.getCantidad(), item.getPrecioUnitario()));
                }
            }
            if (detalles.isEmpty()) {
                throw new RuntimeException("No hay items seleccionados en el carrito.");
            }
            pedido.setDetalles(detalles);

            Pedido pedidoGuardado = pedidoRepository.save(pedido);

            // 5. --- SIMULACIÓN DE PAGO EXITOSO ---
            // (No hay llamada a Stripe)

            pedidoGuardado.setEstado(Pedido.EstadoPedido.ORDEN_RECIBIDA);
            pedidoGuardado.setStripeChargeId("sim_test_" + pedidoGuardado.getCodigoPedido()); // Un ID simulado

            // --- ¡DESCUENTO DE STOCK AÑADIDO! ---
            for (PedidoDetalle detalle : pedidoGuardado.getDetalles()) {
                Producto p = detalle.getProducto();
                int nuevoStock = p.getStock() - detalle.getCantidad();
                p.setStock(Math.max(nuevoStock, 0));
                productoRepository.save(p);
            }

            pedidoRepository.save(pedidoGuardado);

            // 6. Limpiar el carrito
            carritoItemRepo.deleteByUsuarioAndSeleccionadoTrue(usuario);

            response.put("success", true);
            response.put("pedidoId", pedidoGuardado.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Atrapa errores (ej. "Stock insuficiente")
            response.put("success", false);
            response.put("message", "Error en la simulación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}


//package pe.com.isil.inversioneslazaro.controller;
//
//import com.stripe.Stripe;
//import com.stripe.exception.StripeException;
//import com.stripe.model.Charge;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.*;
//import pe.com.isil.inversioneslazaro.model.*;
//import pe.com.isil.inversioneslazaro.repository.*;
//import pe.com.isil.inversioneslazaro.service.CarritoService;
//import pe.com.isil.inversioneslazaro.dto.CarritoItem;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/pago")
//public class PaymentController {
//
//    @Autowired
//    private CarritoService carritoService;
//    @Autowired
//    private PedidoRepository pedidoRepository;
//    @Autowired
//    private UsuarioRepository usuarioRepository;
//    @Autowired
//    private DireccionRepository direccionRepository;
//    @Autowired
//    private ProductoRepository productoRepository;
//    @Autowired
//    private CarritoItemPersistenteRepository carritoItemRepo;
//
//    @Value("${stripe.secret.key}")
//    private String secretKey;
//
//    @PostConstruct
//    public void init() {
//        Stripe.apiKey = secretKey;
//    }
//
//    public static record PaymentRequest(String token, Long direccionId, String shippingMethod) {}
//
//    @PostMapping("/procesar")
//    @Transactional
//    public ResponseEntity<Map<String, Object>> procesarPago(
//            @RequestBody PaymentRequest request,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        Map<String, Object> response = new HashMap<>();
//        try {
//            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
//            Direccion direccion = direccionRepository.findById(request.direccionId()).orElseThrow();
//
//            Map<String, BigDecimal> totalesMap = carritoService.calcularTotales();
//            BigDecimal totalProductos = totalesMap.get("total");
//            BigDecimal costoEnvio = request.shippingMethod().equals("delivery") ? new BigDecimal("10.00") : BigDecimal.ZERO;
//            BigDecimal totalFinal = totalProductos.add(costoEnvio);
//
//            Pedido pedido = new Pedido();
//            pedido.setUsuario(usuario);
//            pedido.setDireccion(direccion);
//            pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
//            pedido.setTotal(totalFinal);
//
//            List<PedidoDetalle> detalles = new ArrayList<>();
//            for (CarritoItem item : carritoService.getItems()) {
//                if (item.isSeleccionado()) {
//                    Producto p = productoRepository.findById(item.getProductoId()).orElseThrow();
//                    detalles.add(new PedidoDetalle(pedido, p, item.getCantidad(), item.getPrecioUnitario()));
//                }
//            }
//
//            if (detalles.isEmpty()) {
//                throw new RuntimeException("No hay items seleccionados en el carrito.");
//            }
//            pedido.setDetalles(detalles);
//
//            Pedido pedidoGuardado = pedidoRepository.save(pedido);
//
//            int montoEnCentavos = totalFinal.multiply(new BigDecimal(100)).intValue();
//            Map<String, Object> chargeParams = new HashMap<>();
//            chargeParams.put("amount", montoEnCentavos);
//            chargeParams.put("currency", "pen");
//            chargeParams.put("description", "Pago por Pedido #" + pedidoGuardado.getCodigoPedido());
//            chargeParams.put("source", request.token());
//
//            Map<String, String> metadata = new HashMap<>();
//            metadata.put("pedido_id_interno", pedidoGuardado.getId().toString());
//            metadata.put("codigo_pedido", pedidoGuardado.getCodigoPedido());
//            metadata.put("usuario_email", usuario.getEmail());
//            chargeParams.put("metadata", metadata);
//
//            Charge charge = Charge.create(chargeParams);
//
//            if ("succeeded".equals(charge.getStatus())) {
//
//                pedidoGuardado.setEstado(Pedido.EstadoPedido.ORDEN_RECIBIDA);
//                pedidoGuardado.setStripeChargeId(charge.getId());
//                pedidoRepository.save(pedidoGuardado);
//
//                carritoItemRepo.deleteByUsuarioAndSeleccionadoTrue(usuario);
//
//                response.put("success", true);
//                response.put("pedidoId", pedidoGuardado.getId());
//                return ResponseEntity.ok(response);
//            } else {
//                // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! ---
//                // Si el pago no fue exitoso (ej. "failed"), devolvemos
//                // una respuesta de error al frontend.
//                response.put("success", false);
//                response.put("message", "El pago falló: " + charge.getFailureMessage());
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            }
//
//        } catch (StripeException e) {
//            // Esto atrapa errores de Stripe (ej. tarjeta inválida, CVC incorrecto)
//            response.put("success", false);
//            response.put("message", e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        } catch (Exception e) {
//            // Esto atrapa otros errores (ej. "No hay items seleccionados")
//            response.put("success", false);
//            response.put("message", "Error interno: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
//
//    /**
//     *
//     * Simulador de pago
//     *
//     */
//
//    // DTO simple para recibir la petición de simulación (no necesita token)
//    public static record SimulationRequest(Long direccionId, String shippingMethod) {}
//
//    /**
//     * ENDPOINT DE SIMULACIÓN
//     * Este método NO llama a Stripe. Simula un pago exitoso y crea el pedido.
//     */
//    @PostMapping("/simular") // <-- ¡NUEVA RUTA!
//    @Transactional
//    public ResponseEntity<Map<String, Object>> simularPago(
//            @RequestBody SimulationRequest request, // <-- ¡NUEVO DTO DE REQUEST!
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        Map<String, Object> response = new HashMap<>();
//        try {
//            // 1. Obtener Usuario y Dirección (Igual que el real)
//            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
//            Direccion direccion = direccionRepository.findById(request.direccionId()).orElseThrow(() -> new RuntimeException("Dirección no encontrada"));
//
//            // 2. Calcular el total REAL (Igual que el real)
//            Map<String, BigDecimal> totalesMap = carritoService.calcularTotales();
//            BigDecimal totalProductos = totalesMap.get("total");
//            BigDecimal costoEnvio = request.shippingMethod().equals("delivery") ? new BigDecimal("10.00") : BigDecimal.ZERO;
//            BigDecimal totalFinal = totalProductos.add(costoEnvio);
//
//            // 3. Crear el Pedido (Igual que el real)
//            Pedido pedido = new Pedido();
//            pedido.setUsuario(usuario);
//            pedido.setDireccion(direccion);
//            pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
//            pedido.setTotal(totalFinal);
//
//            // 4. Copiar items (Igual que el real)
//            List<PedidoDetalle> detalles = new ArrayList<>();
//            for (CarritoItem item : carritoService.getItems()) {
//                if (item.isSeleccionado()) {
//                    Producto p = productoRepository.findById(item.getProductoId()).orElseThrow();
//                    detalles.add(new PedidoDetalle(pedido, p, item.getCantidad(), item.getPrecioUnitario()));
//                }
//            }
//            if (detalles.isEmpty()) {
//                throw new RuntimeException("No hay items seleccionados en el carrito.");
//            }
//            pedido.setDetalles(detalles);
//
//            Pedido pedidoGuardado = pedidoRepository.save(pedido);
//
//            // --- 5. ¡AQUÍ ESTÁ LA SIMULACIÓN! ---
//            // No llamamos a Charge.create().
//            // Simplemente fingimos que el pago fue exitoso.
//
//            pedidoGuardado.setEstado(Pedido.EstadoPedido.ORDEN_RECIBIDA);
//            pedidoGuardado.setStripeChargeId("sim_test_" + pedidoGuardado.getCodigoPedido()); // Un ID simulado
//            pedidoRepository.save(pedidoGuardado);
//
//            // 6. Limpiar el carrito (Igual que el real)
//            carritoItemRepo.deleteByUsuarioAndSeleccionadoTrue(usuario);
//
//            response.put("success", true);
//            response.put("pedidoId", pedidoGuardado.getId());
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            // Atrapa cualquier error (ej. "No hay items")
//            response.put("success", false);
//            response.put("message", "Error interno en la simulación: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
//}