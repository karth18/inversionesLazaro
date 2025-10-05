package pe.com.isil.inversioneslazaro.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;

import java.util.Optional;


@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("")
    public String index(Model model,
                        @PageableDefault(size = 10) Pageable pageable,
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

        return "usuario/index";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, RedirectAttributes ra) {
        // Opcional: verificar si existe antes de eliminar
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            usuarioRepository.deleteById(id);
            ra.addFlashAttribute("mensajeExito", "Usuario eliminado correctamente");
        } else {
            ra.addFlashAttribute("mensajeError", "Usuario no encontrado");
        }
        return "redirect:/admin/usuarios";
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
                             @Valid @ModelAttribute("usuario") Usuario usuario, BindingResult result, Model model) {

        if (!password.isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(password)); // <-- aquí actualizas la contraseña real
        } else {
            Usuario existente = usuarioRepository.findById(id).orElse(null);
            if (existente != null) {
                usuario.setPassword(existente.getPassword());
            }
        }
            if (result.hasErrors()) {
                model.addAttribute("usuario", usuario);
                model.addAttribute("modoEdicion", true);
                return "usuario/editar"; // vuelve al formulario
            }
            usuario.setId(id);
            usuarioRepository.save(usuario);
        return "redirect:/admin/usuarios";
    }
}
