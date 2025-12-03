package pe.com.isil.inversioneslazaro.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Pedido;
import pe.com.isil.inversioneslazaro.model.PedidoSeguimiento;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.PedidoRepository;
import pe.com.isil.inversioneslazaro.repository.PedidoSeguimientoRepository;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;
import pe.com.isil.inversioneslazaro.service.StorageService; // Tu servicio de subir fotos

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/logistica")
public class LogisticaController {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PedidoSeguimientoRepository seguimientoRepository;
    @Autowired private StorageService storageService; // Asegúrate de tener este servicio

    // =====================================================
    // 1. VISTA ALMACÉN: Bandeja de pedidos por empaquetar
    // =====================================================
    @GetMapping("/almacen")
    public String bandejaAlmacen(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario almaceneroActual = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        // LISTA 1: Pedidos nuevos que NADIE ha tomado (Estado: ORDEN_RECIBIDA)
        List<Pedido> porTomar = pedidoRepository.findByEstado(Pedido.EstadoPedido.ORDEN_RECIBIDA);

        // LISTA 2: Pedidos que YO tomé y estoy preparando (Estado: EN_PREPARACION y soy yo el almacenero)
        // Necesitas crear este método en el Repo: findByAlmaceneroAndEstado
        List<Pedido> misPendientes = pedidoRepository.findByAlmaceneroAndEstado(almaceneroActual, Pedido.EstadoPedido.EN_PREPARACION);

        model.addAttribute("pedidosPorTomar", porTomar);
        model.addAttribute("misPedidos", misPendientes);

        return "logistica/almacen";
    }

    @PostMapping("/almacen/tomar")
    public String tomarPedido(@RequestParam Long id, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes ra) {
        Usuario almacenero = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Pedido pedido = pedidoRepository.findById(id).orElseThrow();

        // VALIDACIÓN DE CONCURRENCIA: ¿Alguien más lo tomó hace un milisegundo?
        if (pedido.getAlmacenero() != null) {
            ra.addFlashAttribute("error", "¡Alerta! El pedido ya fue tomado por " + pedido.getAlmacenero().getNombres());
            return "redirect:/logistica/almacen";
        }

        pedido.setAlmacenero(almacenero);
        pedido.setEstado(Pedido.EstadoPedido.EN_PREPARACION);
        pedidoRepository.save(pedido);

        registrarSeguimiento(pedido, Pedido.EstadoPedido.EN_PREPARACION, "Iniciando empaquetado por: " + almacenero.getNombres());

        ra.addFlashAttribute("msg", "Pedido #" + pedido.getCodigoPedido() + " asignado a ti.");
        return "redirect:/logistica/almacen";
    }

    // --- PARTE ALMACÉN ---
    @PostMapping("/almacen/listo")
    public String marcarListo(@RequestParam Long id, RedirectAttributes ra) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow();

        // CAMBIO: Ahora lo pasamos al estado intermedio
        pedido.setEstado(Pedido.EstadoPedido.EMPAQUETADO);
        pedidoRepository.save(pedido);

        registrarSeguimiento(pedido, Pedido.EstadoPedido.EMPAQUETADO, "Empaquetado listo. Esperando asignación de ruta.");

        ra.addFlashAttribute("msg", "Pedido enviado a zona de Despacho.");
        return "redirect:/logistica/almacen";
    }

    // --- PARTE DESPACHO ---
    @GetMapping("/despacho")
    public String bandejaDespacho(Model model) {
        // CAMBIO: El jefe de despacho solo ve lo que ya está listo (EMPAQUETADO)
        // No le interesa lo que todavía están soldando o pintando.
        List<Pedido> pedidos = pedidoRepository.findByEstado(Pedido.EstadoPedido.EMPAQUETADO);

        List<Usuario> choferes = usuarioRepository.findByRolesContaining(Usuario.Rol.CHOFER);

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("choferes", choferes);
        return "logistica/despacho";
    }

    @PostMapping("/despacho/asignar")
    public String asignarChofer(@RequestParam Long pedidoId, @RequestParam Integer choferId, RedirectAttributes ra) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        Usuario chofer = usuarioRepository.findById(choferId).orElseThrow();

        pedido.setChofer(chofer);
        pedido.setEstado(Pedido.EstadoPedido.EN_CAMINO);
        pedidoRepository.save(pedido);

        registrarSeguimiento(pedido, Pedido.EstadoPedido.EN_CAMINO, "En ruta con el chofer: " + chofer.getNombres());

        ra.addFlashAttribute("msg", "Ruta asignada correctamente.");
        return "redirect:/logistica/despacho";
    }

    // =====================================================
    // 3. VISTA CHOFER: Mis Rutas (Móvil)
    // =====================================================
    @GetMapping("/chofer")
    public String misRutas(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario chofer = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        // Buscar pedidos donde yo soy el chofer Y que estén EN CAMINO
        List<Pedido> misEntregas = pedidoRepository.findByChoferAndEstado(chofer, Pedido.EstadoPedido.EN_CAMINO);

        model.addAttribute("pedidos", misEntregas);
        return "logistica/chofer";
    }

    @PostMapping("/chofer/confirmar")
    public String confirmarEntrega(@RequestParam Long pedidoId,
                                   @RequestParam("foto") MultipartFile foto,
                                   RedirectAttributes ra) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();

        if (!foto.isEmpty()) {
            String nombreFoto = storageService.store(foto); // Tu servicio guarda la foto
            pedido.setFotoEntrega(nombreFoto);
        }

        pedido.setEstado(Pedido.EstadoPedido.ENTREGADO);
        pedidoRepository.save(pedido);

        registrarSeguimiento(pedido, Pedido.EstadoPedido.ENTREGADO, "Entregado correctamente. Evidencia cargada.");

        ra.addFlashAttribute("msg", "Entrega registrada. ¡Buen trabajo!");
        return "redirect:/logistica/chofer";
    }

    // --- NUEVO: Cancelar desde Almacén ---
    @PostMapping("/almacen/cancelar")
    public String cancelarDesdeAlmacen(@RequestParam Long pedidoId,
                                       @RequestParam String motivo,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       RedirectAttributes ra) {

        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        Usuario almacenero = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        // 1. Cambiar estado
        pedido.setEstado(Pedido.EstadoPedido.CANCELADO);

        // 2. Guardar motivo específico
        String motivoCompleto = "Cancelado en Almacén por " + almacenero.getNombres() + ": " + motivo;
        pedido.setMotivoCancelacion(motivoCompleto);

        // 3. Devolver Stock (¡Muy importante en almacén!)
        // Aquí deberías recorrer los detalles y sumar el stock nuevamente a los productos
        // for (PedidoDetalle d : pedido.getDetalles()) { ... devolver stock ... }

        pedidoRepository.save(pedido);

        // 4. Registrar en Historial
        PedidoSeguimiento log = new PedidoSeguimiento();
        log.setPedido(pedido);
        log.setEstado(Pedido.EstadoPedido.CANCELADO);
        log.setFechaCambio(LocalDateTime.now());
        log.setUsuarioResponsable(almacenero.getEmail());
        log.setComentario(motivoCompleto);
        seguimientoRepository.save(log);

        ra.addFlashAttribute("error", "El pedido " + pedido.getCodigoPedido() + " ha sido cancelado.");
        return "redirect:/logistica/almacen";
    }
    // --- Helper ---
    private void registrarSeguimiento(Pedido p, Pedido.EstadoPedido e, String comentario) {
        PedidoSeguimiento ps = new PedidoSeguimiento();
        ps.setPedido(p);
        ps.setEstado(e);
        ps.setFechaCambio(LocalDateTime.now());
        ps.setComentario(comentario);
        String usuarioActual = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        ps.setUsuarioResponsable(usuarioActual);
        seguimientoRepository.save(ps);
    }
}