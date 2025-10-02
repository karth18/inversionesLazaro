package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;

import java.security.Principal;
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
    public String actualizar(Usuario usuario){
        usuarioRepository.save(usuario);
        return "redirect:/usuario/userdashboard";
    }

}
