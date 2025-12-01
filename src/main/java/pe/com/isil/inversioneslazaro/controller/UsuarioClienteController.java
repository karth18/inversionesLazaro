package pe.com.isil.inversioneslazaro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Auditoria;
import pe.com.isil.inversioneslazaro.model.Direccion;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.AuditoriaRepository;
import pe.com.isil.inversioneslazaro.repository.DepartamentoRepository;
import pe.com.isil.inversioneslazaro.repository.DireccionRepository;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;
import pe.com.isil.inversioneslazaro.service.AuditoriaService;

import java.security.Principal;
import java.util.List;

@SuppressWarnings("unused")
@Controller
@RequestMapping("/cliente") // Prefijo para todo: /cliente
public class UsuarioClienteController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AuditoriaService auditoriaService;
    @Autowired private DireccionRepository direccionRepository;
    @Autowired private AuditoriaRepository auditoriaRepository;
    @Autowired private DepartamentoRepository departamentoRepository;

    // ========================================================================
    // 1. VISTA PRINCIPAL (DASHBOARD) - CARGA TODO (DATOS Y DIRECCIONES)
    // ========================================================================
    // URL: /cliente/account
    @GetMapping("/account")
    public String verMiCuenta(Model model, @AuthenticationPrincipal UserDetails userDetails){
        // 1. Buscamos al usuario
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Buscamos sus direcciones
        List<Direccion> direcciones = direccionRepository.findByUsuario(usuario);

        // 3. Enviamos todo a la vista
        model.addAttribute("usuario", usuario);
        model.addAttribute("direcciones", direcciones);
        model.addAttribute("nuevaDireccion", new Direccion()); // Objeto vacío para el Modal de agregar
        model.addAttribute("listaDepartamentos", departamentoRepository.findAll());
        // Asegúrate de que tu HTML se llame 'userdashboard.html' y esté en la carpeta 'usuario'
        // O cambia esta ruta si moviste el archivo.
        return "usuario/userdashboard";
    }

    // ========================================================================
    // 2. ACTUALIZAR DATOS PERSONALES
    // ========================================================================
    @PostMapping("/account/update") // Cambié un poco la URL para ser específico
    public String actualizarDatos(@ModelAttribute Usuario usuario, RedirectAttributes ra){

        Usuario existente = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizas solo los campos permitidos
        existente.setNombres(usuario.getNombres());
        existente.setApellidos(usuario.getApellidos());
        existente.setCelular(usuario.getCelular());
        existente.setUsuarioModificacion(existente.getEmail());

        usuarioRepository.save(existente);

        // 🔍 Registrar auditoría (CORREGIDO EL ERROR QUE TENÍAS)
        String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();
        auditoriaService.registrarAccion(
                emailLogueado,
                "Usuario",
                existente.getId().toString(), // <-- AGREGADO .toString() para arreglar el rojo
                Auditoria.AccionAuditoria.ACTUALIZAR);

        ra.addFlashAttribute("mensaje", "Datos personales actualizados.");
        return "redirect:/cliente/account"; // Volver a la vista principal
    }

    // ========================================================================
    // 3. GESTIÓN DE DIRECCIONES
    // ========================================================================

    // A. GUARDAR DIRECCIÓN
    @PostMapping("/direccion/guardar")
    public String guardarDireccion(Direccion direccion, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes ra) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        direccion.setUsuario(usuario);

        // Si es la primera dirección, la hacemos principal por defecto
        if (direccionRepository.findByUsuario(usuario).isEmpty()) {
            direccion.setEsPrincipal(true);
        }

        direccionRepository.save(direccion);

        ra.addFlashAttribute("mensaje", "Nueva dirección agregada.");
        // El #v-direcciones sirve para que al recargar se abra la pestaña de direcciones (con el JS que te pasé)
        return "redirect:/cliente/account#v-direcciones";
    }

    // B. ELIMINAR DIRECCIÓN
    @GetMapping("/direccion/eliminar/{id}")
    public String eliminarDireccion(@PathVariable Long id, RedirectAttributes ra) {
        direccionRepository.deleteById(id);
        ra.addFlashAttribute("mensaje", "Dirección eliminada.");
        return "redirect:/cliente/account#v-direcciones";
    }

    // C. MARCAR COMO PRINCIPAL (Para los Radio Button)
    @GetMapping("/direccion/set-principal/{id}")
    @Transactional
    public String establecerPrincipal(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        // 1. Desmarcar todas las de este usuario
        // (Asegúrate de tener este método en tu Repository, o usa un bucle for aquí si prefieres)
        direccionRepository.desmarcarTodasPrincipales(usuario.getId());

        // 2. Marcar la elegida
        Direccion dir = direccionRepository.findById(id).orElseThrow();
        dir.setEsPrincipal(true);
        direccionRepository.save(dir);

        return "redirect:/cliente/account#v-direcciones";
    }
}

//package pe.com.isil.inversioneslazaro.controller;
//
//import jakarta.servlet.http.HttpSession;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//import pe.com.isil.inversioneslazaro.model.Auditoria;
//import pe.com.isil.inversioneslazaro.model.Direccion;
//import pe.com.isil.inversioneslazaro.model.Usuario;
//import pe.com.isil.inversioneslazaro.repository.AuditoriaRepository;
//import pe.com.isil.inversioneslazaro.repository.DireccionRepository;
//import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;
//import pe.com.isil.inversioneslazaro.service.AuditoriaService;
//
//import java.security.Principal;
//import java.util.List;
//
//@SuppressWarnings("unused")
//@Controller
//@RequestMapping("/cliente")
//public class UsuarioClienteController {
//
//    @Autowired
//    private UsuarioRepository usuarioRepository;
//    @Autowired
//    private AuditoriaService auditoriaService;
//
//    @Autowired private DireccionRepository direccionRepository;
//
//    @Autowired
//    private AuditoriaRepository auditoriaRepository;
//
//    //********************** corresponde al update de la cuenta del cliente***********************
//    @GetMapping("/account/editar")
//    public String editarUsuario(Model model, Principal principal){
//        String email = principal.getName();
//
//        Usuario usuario = usuarioRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
//        model.addAttribute("usuario", usuario);
//
//        return "usuario/userdashboard";
//    }
//
//    @PostMapping("/account/editar")
//    public String actualizar(@ModelAttribute Usuario usuario, RedirectAttributes ra, HttpSession session){
//
//        Usuario existente = usuarioRepository.findById(usuario.getId())
//                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
//
//        // Actualizas solo los campos permitidos
//        existente.setNombres(usuario.getNombres());
//        existente.setApellidos(usuario.getApellidos());
//        existente.setCelular(usuario.getCelular());
//        existente.setUsuarioModificacion(existente.getEmail());
//
//        usuarioRepository.save(existente);
//
//        // 🔍 Registrar auditoría
//        String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();
//        auditoriaService.registrarAccion(
//                emailLogueado,
//                "Usuario",
//                existente.getId(), // <-- EN ROJO (no existe)
//                Auditoria.AccionAuditoria.ACTUALIZAR); // <-- INCORRECTO
//
//        ra.addFlashAttribute("mensaje", "Datos actualizados correctamente");
//        return "redirect:/cliente/account/editar";
//    }
//
//
//    // 1. VER DASHBOARD (Cargamos Usuario + Direcciones)
//    @GetMapping("")
//    public String miCuenta(Model model, @AuthenticationPrincipal UserDetails userDetails) {
//        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
//
//        // Cargamos sus direcciones
//        List<Direccion> direcciones = direccionRepository.findByUsuario(usuario);
//
//        model.addAttribute("usuario", usuario);
//        model.addAttribute("direcciones", direcciones); // <--- IMPORTANTE
//        model.addAttribute("nuevaDireccion", new Direccion()); // Para el modal
//
//        return "cliente/dashboard";
//    }
//
//    // 2. GUARDAR DIRECCIÓN (Redirige al mismo dashboard con la pestaña activada)
//    @PostMapping("/direccion/guardar")
//    public String guardarDireccion(Direccion direccion, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes ra) {
//        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
//        direccion.setUsuario(usuario);
//        direccionRepository.save(direccion);
//
//        ra.addFlashAttribute("mensaje", "Dirección guardada.");
//        return "redirect:/cliente/account#direcciones"; // <--- El #direcciones es el truco
//    }
//
//    // 3. ELIMINAR DIRECCIÓN
//    @GetMapping("/direccion/eliminar/{id}")
//    public String eliminarDireccion(@PathVariable Long id, RedirectAttributes ra) {
//        direccionRepository.deleteById(id);
//        ra.addFlashAttribute("mensaje", "Dirección eliminada.");
//        return "redirect:/cliente/account#direcciones";
//    }
//}
