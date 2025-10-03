package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cuenta")
public class DashboardController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/editar")
    public String editarUsuario(Model model, Principal principal){
        String email = principal.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        return "usuario/userdashboard";
    }

    @PostMapping("/editar")
    public String actualizar(@ModelAttribute Usuario usuario, RedirectAttributes ra){
        Usuario existente = usuarioRepository.findById(usuario.getDni())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizas solo los campos permitidos
        existente.setNombres(usuario.getNombres());
        existente.setApellidos(usuario.getApellidos());
        existente.setCelular(usuario.getCelular());
        existente.setDireccion(usuario.getDireccion());

        usuarioRepository.save(existente);

        ra.addFlashAttribute("mensaje", "Datos actualizados correctamente");
        return "redirect:/cuenta/editar";
    }

}
