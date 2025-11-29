package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.*;
import pe.com.isil.inversioneslazaro.repository.PedidoRepository;
import pe.com.isil.inversioneslazaro.repository.PedidoSeguimientoRepository;
import pe.com.isil.inversioneslazaro.repository.ProductoRepository;
import pe.com.isil.inversioneslazaro.service.AuditoriaService;
import pe.com.isil.inversioneslazaro.service.EmailService; // <--- IMPORTANTE

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/pedidos")
public class AdminPedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private AuditoriaService auditoriaService;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private PedidoSeguimientoRepository pedidoSeguimientoRepository;

    // 1. INYECTAR EL SERVICIO DE CORREO (ESTO FALTABA)
    @Autowired
    private EmailService emailService;

    @GetMapping("")
    public String verPedidos(Model model,
                             @RequestParam(required = false) String busqueda,
                             @PageableDefault(size = 15, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Pedido> pedidos;
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            pedidos = pedidoRepository.searchByCodigoOrEmail(busqueda, pageable);
        } else {
            pedidos = pedidoRepository.findAll(pageable);
        }

        model.addAttribute("pedidosPage", pedidos);
        model.addAttribute("estados", Pedido.EstadoPedido.values());
        model.addAttribute("busqueda", busqueda);
        return "admin/pedido/index";
    }

    @GetMapping("/detalle/{id}")
    public String verDetallePedido(@PathVariable Long id, Model model, RedirectAttributes ra) {
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

    @PostMapping("/actualizar-estado")
    @Transactional
    public String actualizarEstado(@RequestParam("pedidoId") Long pedidoId,
                                   @RequestParam("estado") Pedido.EstadoPedido estado,
                                   @RequestParam(value = "motivoCancelacion",required = false) String motivo,
                                   RedirectAttributes ra) {
        return pedidoRepository.findById(pedidoId)
                .map(pedido -> {

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

                    try {
                        String usuarioResponsable = SecurityContextHolder.getContext().getAuthentication().getName();
                        String comentarioHistorial = (motivo != null && !motivo.isEmpty())
                                ? motivo
                                : "El estado cambió a " + estado.name().replace("_", " ");

                        PedidoSeguimiento seguimiento = new PedidoSeguimiento();
                        seguimiento.setPedido(pedido);
                        seguimiento.setEstado(estado);
                        seguimiento.setFechaCambio(LocalDateTime.now());
                        seguimiento.setUsuarioResponsable(usuarioResponsable);
                        seguimiento.setComentario(comentarioHistorial);

                        pedidoSeguimientoRepository.save(seguimiento);

                        // TAMBIÉN AQUÍ PODRÍAS AGREGAR EL ENVÍO SI USAS ESTE MÉTODO
                        emailService.enviarCorreoEstadoPedido(pedido);

                    } catch (Exception e) {
                        System.err.println("Error procesando pedido: " + e.getMessage());
                    }

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

    @PostMapping("/api/actualizar-estado")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> actualizarEstadoApi(@RequestParam("pedidoId") Long pedidoId,
                                                 @RequestParam("estado") Pedido.EstadoPedido estado,
                                                 @RequestParam(value = "motivoCancelacion", required = false) String motivo,
                                                 @RequestParam(value = "nuevaFecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nuevaFecha,
                                                 @RequestParam(value = "motivoReagendacion", required = false) String motivoReagendacion) {

        Map<String, Object> response = new HashMap<>();

        try {
            Pedido pedido = pedidoRepository.findById(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            String comentarioHistorial;

            // 1. Lógica de Cancelación y Reagendado
            if (estado == Pedido.EstadoPedido.CANCELADO) {
                pedido.setMotivoCancelacion(motivo);
                comentarioHistorial = motivo;
                if (pedido.getEstado() != Pedido.EstadoPedido.CANCELADO) {
                    for (PedidoDetalle detalle : pedido.getDetalles()) {
                        Producto producto = detalle.getProducto();
                        producto.setStock(producto.getStock() + detalle.getCantidad());
                        productoRepository.save(producto);
                    }
                }
            }
            else if (estado == Pedido.EstadoPedido.REAGENDADO && nuevaFecha != null) {
                pedido.setFechaEntregaEstimada(nuevaFecha.atTime(20, 0));
                String motivoTexto = (motivoReagendacion != null && !motivoReagendacion.isEmpty()) ? motivoReagendacion : "Motivos logísticos";
                // Añadimos el prefijo para que el EmailService lo pueda limpiar
                comentarioHistorial = "REAGENDADO: " + motivoTexto + ". Nueva fecha: " + nuevaFecha;
            }
            else {
                comentarioHistorial = "El estado cambió a " + estado.name().replace("_", " ");
            }

            pedido.setEstado(estado);
            pedidoRepository.save(pedido);

            // 2. Guardar Historial
            try {
                String usuarioResponsable = SecurityContextHolder.getContext().getAuthentication().getName();

                PedidoSeguimiento seguimiento = new PedidoSeguimiento();
                seguimiento.setPedido(pedido);
                seguimiento.setEstado(estado);
                seguimiento.setFechaCambio(LocalDateTime.now());
                seguimiento.setUsuarioResponsable(usuarioResponsable);
                seguimiento.setComentario(comentarioHistorial);

                pedidoSeguimientoRepository.save(seguimiento);

            } catch (Exception e) {
                System.err.println("Error al guardar historial (API): " + e.getMessage());
            }

            // 3. Auditoría
            String accionAuditoria = "Estado actualizado a " + estado.name();
            if (motivo != null && !motivo.isEmpty()) {
                accionAuditoria += " | Motivo: " + motivo;
            }
            registrarAuditoria("Pedido", pedido.getId(), accionAuditoria);

            // =================================================================
            // 4. ¡AQUÍ ESTÁ LA MAGIA! ENVÍO DE CORREO (ESTO TE FALTABA)
            // =================================================================
            try {
                // Solo enviamos si es un estado relevante
                if (estado == Pedido.EstadoPedido.EN_CAMINO ||
                        estado == Pedido.EstadoPedido.REAGENDADO ||
                        estado == Pedido.EstadoPedido.ENTREGADO ||
                        estado == Pedido.EstadoPedido.CANCELADO) {

                    emailService.enviarCorreoEstadoPedido(pedido);
                }
            } catch (Exception e) {
                System.err.println("Error enviando notificación: " + e.getMessage());
            }
            // =================================================================

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
        return pedidoRepository.findById(id)
                .map(pedido -> {
                    model.addAttribute("pedido", pedido);
                    return "admin/pedido/imprimir";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("msgError", "Pedido no encontrado");
                    return "redirect:/admin/pedidos";
                });
    }

    private void registrarAuditoria(String entidad, Object id, String accion) {
        auditoriaService.registrarAccion(
                SecurityContextHolder.getContext().getAuthentication().getName(),
                entidad + " (Estado)",
                id.toString(),
                Auditoria.AccionAuditoria.ACTUALIZAR
        );
    }
}