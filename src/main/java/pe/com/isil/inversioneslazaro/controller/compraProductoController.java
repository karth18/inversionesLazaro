package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pe.com.isil.inversioneslazaro.dto.CarritoItem;
import pe.com.isil.inversioneslazaro.dto.CarritoTotalesDTO;
import pe.com.isil.inversioneslazaro.dto.DireccionDTO;
import pe.com.isil.inversioneslazaro.model.Departamento;
import pe.com.isil.inversioneslazaro.model.Direccion;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.DepartamentoRepository;
import pe.com.isil.inversioneslazaro.repository.DireccionRepository;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;
import pe.com.isil.inversioneslazaro.service.CarritoService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/compra")
public class compraProductoController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private DireccionRepository direccionRepository;
    @Autowired private DepartamentoRepository departamentoRepository;

    @Autowired
    private CarritoService carritoService;
    // Deberías inyectar tu repositorio o servicio de Carrito
   // @Autowired private ItemCarritoRepositor itemCarritoRepository;

    @GetMapping("/checkout")
    @Transactional(readOnly = true)
    String checkout(Model model, @AuthenticationPrincipal UserDetails userDetails) {

        // 1. Obtener el usuario
        Usuario usuarioActual = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Cargar las ENTIDADES de dirección
        List<Direccion> direccionesUsuario = direccionRepository.findByUsuarioIdOrderByEsPrincipalDesc(usuarioActual.getId());

        // 3. Determinar la ENTIDAD seleccionada
        Optional<Direccion> direccionSeleccionadaOpt = direccionesUsuario.stream()
                .filter(Direccion::isEsPrincipal)
                .findFirst()
                .or(() -> direccionesUsuario.stream().findFirst());

        // *** 4. CONVERTIR A DTOs ***
        // (Esto funciona gracias a @Transactional)
        List<DireccionDTO> direccionesDTO = direccionesUsuario.stream()
                .map(DireccionDTO::new)
                .collect(Collectors.toList());

        DireccionDTO direccionSeleccionadaDTO = direccionSeleccionadaOpt
                .map(DireccionDTO::new)
                .orElse(null);

        // *** 5. AÑADIR DTOs AL MODELO ***
        model.addAttribute("direccionesUsuario", direccionesDTO);
        model.addAttribute("direccionSeleccionada", direccionSeleccionadaDTO);

        // 6. Cargar departamentos (para el modal)
        List<Departamento> departamentos = departamentoRepository.findAllByOrderByNombreAsc();
        model.addAttribute("departamentos", departamentos);

        // 7. Cargar datos del carrito (como ya lo corregimos)
        Map<Long, CarritoItem> carritoMap = carritoService.getCarrito();
        List<CarritoItem> itemsSeleccionados = carritoMap.values().stream()
                .filter(CarritoItem::isSeleccionado)
                .collect(Collectors.toList());
        Map<String, BigDecimal> totalesMap = carritoService.calcularTotales();
        CarritoTotalesDTO totales = new CarritoTotalesDTO(totalesMap);

        model.addAttribute("itemsCarrito", itemsSeleccionados);
        model.addAttribute("totales", totales);

        return "compra/checkout";
    }
}
