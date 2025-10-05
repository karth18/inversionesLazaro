package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;

import java.security.Principal;
@Controller
@RequestMapping("/cliente")
public class UsuarioClienteController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    //********************** corresponde al update de la cuenta del cliente***********************
    @GetMapping("/account/editar")
    public String editarUsuario(Model model, Principal principal){
        String email = principal.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        return "usuario/userdashboard";
    }

    @PostMapping("/account/editar")
    public String actualizar(@ModelAttribute Usuario usuario, RedirectAttributes ra){
        Usuario existente = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizas solo los campos permitidos
        existente.setNombres(usuario.getNombres());
        existente.setApellidos(usuario.getApellidos());
        existente.setCelular(usuario.getCelular());
        existente.setDireccion(usuario.getDireccion());

        usuarioRepository.save(existente);
        ra.addFlashAttribute("mensaje", "Datos actualizados correctamente");
        return "redirect:/cliente/account/editar";
    }
}
