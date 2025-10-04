package pe.com.isil.inversioneslazaro.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();

            usuario.setPassword("");
            model.addAttribute("usuario", usuario); // <-- clave "usuario"
            model.addAttribute("modoEdicion",true);
            return "usuario/editar";
        } else {
            return "redirect:/admin/usuarios"; // o alguna página de error
        }
    }

    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable String id,
                             @RequestParam String password,
                             @Valid @ModelAttribute("usuario") Usuario usuario, BindingResult result, Model model) {

        if (!password.isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(password)); // <-- aquí actualizas la contraseña real
        }else {
            Usuario existente = usuarioRepository.findById(id).orElse(null);
            if(existente != null){
                usuario.setPassword(existente.getPassword());
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("modoEdicion", true);
            return "usuario/editar"; // vuelve al formulario
        }
        usuario.setDni(id);
        usuarioRepository.save(usuario);
        return "redirect:/admin/usuarios";
    }
}
