package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Pedido;
import pe.com.isil.inversioneslazaro.model.PedidoDetalle;
import pe.com.isil.inversioneslazaro.model.Producto;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.PedidoRepository;
import pe.com.isil.inversioneslazaro.repository.ProductoRepository;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;
import pe.com.isil.inversioneslazaro.service.StripeService;

import java.util.Map;

@Controller
@RequestMapping("/misPedidos") // La URL para el cliente
public class ClientePedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired private StripeService stripeService;
    @Autowired private ProductoRepository productoRepository;

    /**
     * Muestra la lista de "Mis Pedidos" del cliente logueado
     */
    @GetMapping("")
    public String verMisPedidos(Model model,
                                @AuthenticationPrincipal UserDetails userDetails,
                                @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable,
                                @RequestParam(value = "palabraClave", required = false) String palabraClave){

        // 1. Obtener el usuario logueado
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));


        // 2. Buscar pedidos SOLO de ese usuario
        Page<Pedido> pedidos;
        if (palabraClave != null && !palabraClave.isEmpty()) {
            // Si escribieron algo en el buscador:
            pedidos = pedidoRepository.findByUsuarioAndCodigoPedidoContainingIgnoreCase(usuario, palabraClave, pageable);
        } else {
            // Si no, trae todo el historial:
            pedidos = pedidoRepository.findByUsuario(usuario, pageable);
        }



        model.addAttribute("pedidosPage", pedidos);
        model.addAttribute("palabraClave", palabraClave);

        return "cliente/misPedidos"; // -> /templates/cliente/mis-pedidos.html
    }

    /**
     * Muestra el detalle de UN pedido, pero con seguridad
     */
    @GetMapping("/detalle/{id}")
    public String verDetallePedido(@PathVariable Long id,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(required = false) String palabraClave,
                                   Model model,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes ra) {

        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        return pedidoRepository.findById(id)
                .map(pedido -> {

                    // --- ¡VERIFICACIÓN DE SEGURIDAD! ---
                    // Comprueba si el pedido le pertenece al usuario logueado
                    if (!pedido.getUsuario().getId().equals(usuario.getId())) {
                        ra.addFlashAttribute("msgError", "Acceso denegado");
                        return "redirect:/misPedidos";
                    }

                    model.addAttribute("pedido", pedido);
                    model.addAttribute("page", page);
                    model.addAttribute("palabraClave", palabraClave);

                    return "cliente/detalle";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("msgError", "Pedido no encontrado");
                    return "redirect:/misPedidos";
                });
    }

    @PostMapping("/cancelar/{id}")
    @ResponseBody // Devuelve JSON
    @Transactional // ¡MUY IMPORTANTE!
    public ResponseEntity<?> cancelarPedido(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
            Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            // 1. Chequeo de Seguridad: ¿Es mi pedido?
            if (!pedido.getUsuario().getId().equals(usuario.getId())) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Acceso denegado."));
            }

            // 2. Chequeo de Lógica: ¿Se puede cancelar?
            if (pedido.getEstado() == Pedido.EstadoPedido.EN_CAMINO ||
                    pedido.getEstado() == Pedido.EstadoPedido.FINALIZADO ||
                    pedido.getEstado() == Pedido.EstadoPedido.CANCELADO) {
                return ResponseEntity.status(400).body(Map.of("success", false, "message", "Este pedido ya está en camino o finalizado, no se puede cancelar."));
            }

            // 3. Devolver el Dinero (Reembolso en Stripe)
            // (Si es un pago simulado "sim_...", no se hace reembolso)
            if (pedido.getStripeChargeId() != null && !pedido.getStripeChargeId().startsWith("sim_")) {
                stripeService.createRefund(pedido.getStripeChargeId());
            }

            // 4. Devolver el Stock
            for (PedidoDetalle detalle : pedido.getDetalles()) {
                Producto p = detalle.getProducto();
                int cantidadDevuelta = detalle.getCantidad();
                p.setStock(p.getStock() + cantidadDevuelta); // Suma el stock de vuelta
                productoRepository.save(p);
            }

            // 5. Actualizar Estado del Pedido
            pedido.setEstado(Pedido.EstadoPedido.CANCELADO);
            pedidoRepository.save(pedido);

            // 6. Enviar respuesta exitosa
            return ResponseEntity.ok(Map.of("success", true, "message", "Pedido " + pedido.getCodigoPedido() + " cancelado y reembolsado."));

        } catch (Exception e) {
            // Captura errores de Stripe o BD
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error al cancelar: " + e.getMessage()));
        }
    }
}