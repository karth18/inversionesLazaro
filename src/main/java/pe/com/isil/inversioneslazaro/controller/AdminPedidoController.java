package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Auditoria;
import pe.com.isil.inversioneslazaro.model.Pedido;
import pe.com.isil.inversioneslazaro.model.PedidoDetalle;
import pe.com.isil.inversioneslazaro.model.Producto;
import pe.com.isil.inversioneslazaro.repository.PedidoRepository;
import pe.com.isil.inversioneslazaro.repository.ProductoRepository;
import pe.com.isil.inversioneslazaro.service.AuditoriaService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/pedidos")
public class AdminPedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private AuditoriaService auditoriaService;
    @Autowired private ProductoRepository productoRepository;
    /**
     * Muestra la lista paginada de todos los pedidos
     * (¡MODIFICADO CON BÚSQUEDA!)
     */
    @GetMapping("")
    public String verPedidos(Model model,
                             @RequestParam(required = false) String busqueda, // <-- 1. AÑADIDO
                             @PageableDefault(size = 15, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Pedido> pedidos; // <-- 2. DECLARADO

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            // --- 3. LÓGICA DE BÚSQUEDA ---
            pedidos = pedidoRepository.searchByCodigoOrEmail(busqueda, pageable);
        } else {
            // --- 4. LÓGICA POR DEFECTO ---
            pedidos = pedidoRepository.findAll(pageable);
        }

        model.addAttribute("pedidosPage", pedidos);
        model.addAttribute("estados", Pedido.EstadoPedido.values());
        model.addAttribute("busqueda", busqueda); // <-- 5. DEVUELTO A LA VISTA
        return "admin/pedido/index";
    }

    /**
     * Muestra la página de "Detalle de Pedido" (la que tiene los productos)
     */
    @GetMapping("/detalle/{id}")
    public String verDetallePedido(@PathVariable Long id, Model model, RedirectAttributes ra) {
        // (Este método se queda igual)
        return pedidoRepository.findById(id)
                .map(pedido -> {
                    model.addAttribute("pedido", pedido);
                    return "admin/pedido/detalle";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("msgError", "Pedido no encontrado");
                    return "redirect:/admin/pedidos";
                });
    }

    /**
     * Endpoint para actualizar el estado del pedido desde la lista
     */
    @PostMapping("/actualizar-estado")
    public String actualizarEstado(@RequestParam("pedidoId") Long pedidoId,
                                   @RequestParam("estado") Pedido.EstadoPedido estado,
                                   @RequestParam(value = "motivoCancelacion",required = false) String motivo,
                                   RedirectAttributes ra) {
        // (Este método se queda igual)
        return pedidoRepository.findById(pedidoId)
                .map(pedido -> {

                    if (estado == Pedido.EstadoPedido.CANCELADO) {
                        // Asignamos el motivo al pedido (asegúrate de tener este campo en tu Entidad)
                        pedido.setMotivoCancelacion(motivo);
                        if (pedido.getEstado() != Pedido.EstadoPedido.CANCELADO) {
                            for (PedidoDetalle detalle : pedido.getDetalles()) {
                                Producto producto = detalle.getProducto();
                                producto.setStock(producto.getStock() + detalle.getCantidad());
                                productoRepository.save(producto);
                            }
                        }
                    }

                    pedido.setEstado(estado);
                    pedidoRepository.save(pedido);


                    String accionAuditoria = "Estado actualizado a " + estado.name();
                    if (motivo != null && !motivo.isEmpty()) {
                        accionAuditoria += " | Motivo: " + motivo;
                    }
                    registrarAuditoria("Pedido", pedido.getId(), accionAuditoria);
                    ra.addFlashAttribute("msgExito", "Estado del Pedido " + pedido.getCodigoPedido() + " actualizado.");
                    return "redirect:/admin/pedidos";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("msgError", "Error al actualizar, pedido no encontrado.");
                    return "redirect:/admin/pedidos";
                });
    }

    @PostMapping("/api/actualizar-estado") // Fíjate que la URL es distinta (/api/...)
    @ResponseBody // Esto le dice a Spring: "No busques un HTML, devuelve datos puros"
    public ResponseEntity<?> actualizarEstadoApi(@RequestParam("pedidoId") Long pedidoId,
                                                 @RequestParam("estado") Pedido.EstadoPedido estado,
                                                 @RequestParam(value = "motivoCancelacion", required = false) String motivo) {

        Map<String, Object> response = new HashMap<>();

        try {
            Pedido pedido = pedidoRepository.findById(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            // 1. Copiamos EXACTAMENTE tu misma lógica de negocio aquí
            if (estado == Pedido.EstadoPedido.CANCELADO) {
                pedido.setMotivoCancelacion(motivo);

                if (pedido.getEstado() != Pedido.EstadoPedido.CANCELADO) {
                    for (PedidoDetalle detalle : pedido.getDetalles()) {
                        Producto producto = detalle.getProducto();
                        producto.setStock(producto.getStock() + detalle.getCantidad());
                        productoRepository.save(producto);
                    }
                }
            }

            pedido.setEstado(estado);
            pedidoRepository.save(pedido);

            // 2. Auditoría
            String accionAuditoria = "Estado actualizado a " + estado.name();
            if (motivo != null && !motivo.isEmpty()) {
                accionAuditoria += " | Motivo: " + motivo;
            }
            registrarAuditoria("Pedido", pedido.getId(), accionAuditoria);

            // 3. EN LUGAR DE REDIRECTATTRIBUTES, Enviamos JSON
            response.put("success", true);
            response.put("message", "Estado del Pedido " + pedido.getCodigoPedido() + " actualizado.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/imprimir/{id}")
    public String imprimirPedido(@PathVariable Long id, Model model, RedirectAttributes ra) {

        // Carga el pedido con todos sus detalles (productos, usuario, etc.)
        return pedidoRepository.findById(id)
                .map(pedido -> {
                    model.addAttribute("pedido", pedido);
                    // Apunta a la nueva plantilla de impresión
                    return "admin/pedido/imprimir"; // -> /templates/admin/pedido/imprimir.html
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("msgError", "Pedido no encontrado");
                    return "redirect:/admin/pedidos";
                });
    }

    // --- Helper de Auditoría (Este método se queda igual) ---
    private void registrarAuditoria(String entidad, Object id, String accion) {
        auditoriaService.registrarAccion(
                SecurityContextHolder.getContext().getAuthentication().getName(),
                entidad + " (Estado)",
                id.toString(),
                Auditoria.AccionAuditoria.ACTUALIZAR
        );
    }
}
