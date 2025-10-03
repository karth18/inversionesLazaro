package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;

import java.util.Optional;


@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("")
    String index(Model model, @PageableDefault(size=5) Pageable pageable, @RequestParam(required = false) String dni)
    {
        //1. listado tipo Page de todos usuarios registrados en la base de datos
        Page<Usuario> usu;
        if (dni != null && !dni.trim().isEmpty())
        {
            usu = usuarioRepository.findByDniContainingIgnoreCase(dni, pageable);
        }
        else
        {
            usu = usuarioRepository.findAll(pageable);
        }

        //2. creamos un atributo en el model a enviar a la vista
        model.addAttribute("usu", usu);

        //3. retornamos la vista o HTML a mostrar
        return "usuario/index"; //al archivo: index.html
    }

    @GetMapping("/editar/{id}")
    public String editar(Model model, @PathVariable String id){
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get()); // <-- clave "usuario"
            return "usuario/editar";
        } else {
            return "redirect:/admin/usuarios"; // o alguna página de error
        }
    }
    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable String id,
                             @RequestParam String password1,
                             @RequestParam String password2,
                             @ModelAttribute("usuario") Usuario usuario) {

        if (!password1.isEmpty()) {
            if (!password1.equals(password2)) {
                // Manejar error: las contraseñas no coinciden
                return "usuario/editar";
            }
            usuario.setPassword(passwordEncoder.encode(password1)); // <-- aquí actualizas la contraseña real
        }

        usuario.setDni(id);
        usuarioRepository.save(usuario);
        return "redirect:/admin/usuarios";
    }
}
