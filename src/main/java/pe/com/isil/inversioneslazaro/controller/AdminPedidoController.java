package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Auditoria;
import pe.com.isil.inversioneslazaro.model.Pedido;
import pe.com.isil.inversioneslazaro.repository.PedidoRepository;
import pe.com.isil.inversioneslazaro.service.AuditoriaService;

@Controller
@RequestMapping("/admin/pedidos")
public class AdminPedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private AuditoriaService auditoriaService;

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
                                   RedirectAttributes ra) {
        // (Este método se queda igual)
        return pedidoRepository.findById(pedidoId)
                .map(pedido -> {
                    pedido.setEstado(estado);
                    pedidoRepository.save(pedido);
                    registrarAuditoria("Pedido", pedido.getId(), "Estado actualizado a " + estado.name());
                    ra.addFlashAttribute("msgExito", "Estado del Pedido " + pedido.getCodigoPedido() + " actualizado.");
                    return "redirect:/admin/pedidos";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("msgError", "Error al actualizar, pedido no encontrado.");
                    return "redirect:/admin/pedidos";
                });
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
