package pe.com.isil.inversioneslazaro.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.AuditoriaUsuario;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.AuditoriaUsuarioRepository;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;

import java.util.Optional;

@SuppressWarnings("unused")
@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditoriaUsuarioRepository auditoriaUsuarioRepository;




    @GetMapping("")
    public String index(Model model,
                        @PageableDefault(size = 8) Pageable pageable,
                        @RequestParam(required = false) String busqueda) {

        Page<Usuario> usu;

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            // Buscar por todos los campos
            usu = usuarioRepository
                    .findByDniContainingIgnoreCaseOrEmailContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
                            busqueda, busqueda, busqueda, busqueda, pageable);
        } else {
            usu = usuarioRepository.findByEstadoTrue(pageable);
        }

        model.addAttribute("usu", usu);
        model.addAttribute("busqueda", busqueda); // para mantener el valor en el input
        model.addAttribute("totalRegistros", usu.getTotalElements());

        return "usuario/index";
    }


    @GetMapping("/activar")
    public String activar(Model model,
                        @PageableDefault(size = 8) Pageable pageable,
                        @RequestParam(required = false) String busqueda) {
        Page<Usuario> usu;
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            // Buscar por todos los campos
            usu = usuarioRepository
                    .findByDniContainingIgnoreCaseOrEmailContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
                            busqueda, busqueda, busqueda, busqueda, pageable);
        } else {
            usu = usuarioRepository.findAll(pageable);
        }

        model.addAttribute("usu", usu);
        model.addAttribute("busqueda", busqueda); // para mantener el valor en el input
        model.addAttribute("totalRegistros", usu.getTotalElements());

        return "usuario/activarusu";
    }


    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, RedirectAttributes ra) {
        // Opcional: verificar si existe antes de eliminar
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            Usuario usuarioe = usuario.get();
            usuarioe.setEstado(false);
            usuarioRepository.save(usuarioe);

            // Obtener el email del usuario logueado
            String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();

            // Registrar en auditoría
            AuditoriaUsuario audit = new AuditoriaUsuario();
            audit.setUsuarioAfectado(usuarioe);
            audit.setAccion("ELIMINAR");
            audit.setRealizadoPor(emailLogueado);
            auditoriaUsuarioRepository.save(audit);
            ra.addFlashAttribute("mensajeExito", "Usuario eliminado correctamente");
        } else {
            ra.addFlashAttribute("mensajeError", "Usuario no encontrado");
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/habilitar/{id}")
    public String habilitar(@PathVariable Integer id, RedirectAttributes ra) {
        // Opcional: verificar si existe antes de eliminar
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            Usuario usuarioe = usuario.get();
            usuarioe.setEstado(true);
            usuarioRepository.save(usuarioe);

            // Obtener el email del usuario logueado
            String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();

            // Registrar en auditoría
            AuditoriaUsuario audit = new AuditoriaUsuario();
            audit.setUsuarioAfectado(usuarioe);
            audit.setAccion("HABILITAR");
            audit.setRealizadoPor(emailLogueado);
            auditoriaUsuarioRepository.save(audit);
            ra.addFlashAttribute("mensajeExito", "Usuario Habilitado correctamente");
        } else {
            ra.addFlashAttribute("mensajeError", "Usuario no encontrado");
        }

        return "redirect:/admin/usuarios/activar";
    }

    @GetMapping("/editar/{id}")
    public String editar(Model model, @PathVariable Integer id) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();

            usuario.setPassword("");
            model.addAttribute("usuario", usuario); // <-- clave "usuario"
            model.addAttribute("modoEdicion", true);
            return "usuario/editar";
        } else {
            return "redirect:/admin/usuarios"; // o alguna página de error
        }
    }

    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Integer id,
                             @RequestParam String password,
                             @Valid @ModelAttribute("usuario") Usuario usuario, BindingResult result, Model model, HttpSession session) {
        Usuario existente = usuarioRepository.findById(id).orElse(null);
        if(existente == null){
            return "redirect: /admin/usuarios";
        }

        if (result.hasErrors()) {

            usuario.setId(id);
            model.addAttribute("usuario", usuario);
            model.addAttribute("modoEdicion", true);
            return "usuario/editar"; // vuelve al formulario
        }

        existente.setDni(usuario.getDni());
        existente.setNombres(usuario.getNombres());
        existente.setApellidos(usuario.getApellidos());
        existente.setCelular(usuario.getCelular());
        existente.setFechaCreacion(usuario.getFechaCreacion());
        existente.setEmail(usuario.getEmail());
        existente.setRol(usuario.getRol());
        existente.setPolitica(usuario.getPolitica());

        if (password != null && !password.isBlank()) {
            existente.setPassword(passwordEncoder.encode(password));
        }
        usuarioRepository.save(existente);

        String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();
        AuditoriaUsuario audit = new AuditoriaUsuario();
        audit.setUsuarioAfectado(existente);
        audit.setAccion("EDICIÓN");
        audit.setRealizadoPor(emailLogueado);
        auditoriaUsuarioRepository.save(audit);

        return "redirect:/admin/usuarios";
    }


    @GetMapping("/auditoria")
    public String verAuditoria(@RequestParam(value = "busqueda", required = false) String busqueda,
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by("fechaAccion").descending());
        Page<AuditoriaUsuario> audi;

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            audi = auditoriaUsuarioRepository.buscarAuditoria(busqueda.trim(), pageable);
        } else {
            audi = auditoriaUsuarioRepository.findAll(pageable);
        }

        model.addAttribute("auditorias", audi.getContent());
        model.addAttribute("audi", audi);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("totalRegistros", audi.getTotalElements());

        return "auditoria/index";
    }

}
