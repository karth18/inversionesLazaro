package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Pedido;
import pe.com.isil.inversioneslazaro.model.PedidoSeguimiento;
import pe.com.isil.inversioneslazaro.repository.PedidoRepository;
import pe.com.isil.inversioneslazaro.repository.PedidoSeguimientoRepository;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/finanzas")
public class FinanzasController {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private PedidoSeguimientoRepository seguimientoRepository;

    // 1. BANDEJA DE PAGOS POR VALIDAR
    @GetMapping("/validacion")
    public String bandejaValidacion(Model model) {
        // Solo mostramos lo que está PENDIENTE (El dinero no ha sido confirmado)
        List<Pedido> pedidosPendientes = pedidoRepository.findByEstado(Pedido.EstadoPedido.PENDIENTE);

        model.addAttribute("pedidos", pedidosPendientes);
        return "admin/finanzas/validacion";
    }

    // 2. ACCIÓN: APROBAR PAGO (Liberar pedido a Almacén)
    @PostMapping("/aprobar")
    public String aprobarPago(@RequestParam Long id, RedirectAttributes ra) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow();

        // CAMBIO DE ESTADO CRÍTICO: De PENDIENTE a ORDEN_RECIBIDA
        // Al hacer esto, automáticamente aparece en la pantalla del Almacenero
        pedido.setEstado(Pedido.EstadoPedido.ORDEN_RECIBIDA);
        pedidoRepository.save(pedido);

        // Trazabilidad
        String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
        registrarSeguimiento(pedido, Pedido.EstadoPedido.ORDEN_RECIBIDA, "Pago validado manualmente por Finanzas (" + usuario + "). Liberado para almacén.");

        ra.addFlashAttribute("msgExito", "Pago aprobado. El pedido #" + pedido.getCodigoPedido() + " pasó a Almacén.");
        return "redirect:/admin/finanzas/validacion";
    }

    // 3. ACCIÓN: RECHAZAR PAGO (Cancelar pedido)
    @PostMapping("/rechazar")
    public String rechazarPago(@RequestParam Long id, @RequestParam String motivo, RedirectAttributes ra) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow();

        pedido.setEstado(Pedido.EstadoPedido.CANCELADO);
        pedido.setMotivoCancelacion("Pago rechazado por Finanzas: " + motivo);
        pedidoRepository.save(pedido);

        registrarSeguimiento(pedido, Pedido.EstadoPedido.CANCELADO, "Pago rechazado. Motivo: " + motivo);

        ra.addFlashAttribute("msgError", "Pedido #" + pedido.getCodigoPedido() + " cancelado por falta de pago.");
        return "redirect:/admin/finanzas/validacion";
    }

    private void registrarSeguimiento(Pedido p, Pedido.EstadoPedido e, String comentario) {
        PedidoSeguimiento ps = new PedidoSeguimiento();
        ps.setPedido(p);
        ps.setEstado(e);
        ps.setFechaCambio(LocalDateTime.now());
        ps.setComentario(comentario);
        seguimientoRepository.save(ps);
    }
}