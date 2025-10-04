package pe.com.isil.inversioneslazaro.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;

@Controller
@RequestMapping("/registroUsu")
public class RegistroUsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registro")
    public String index(Model model)
    {
        model.addAttribute("usuario", new Usuario());
        return "usuario/form";
    }

    @PostMapping("/registro")
    public String crear(@Valid Usuario usuario, BindingResult bindingResult, RedirectAttributes ra, Model model)
    {

        //Validar si existe un email, tiene que ser unico
        String email = usuario.getEmail();
        String dni = usuario.getDni();
        boolean usuarioExiste = usuarioRepository.existsByEmail(email);
        boolean dniExists = usuarioRepository.existsByDni(dni);
        if (usuarioExiste)
        {
            bindingResult.rejectValue("email", "EmailAlredayExists.usuario.email");
        }
        //Validar si existe DNI, tiene que ser unico
        if (dniExists) {
            bindingResult.rejectValue("dni", "DniAlreadyExists.usuario.dni");
        }

        //Validar la coincidencia de contraseñas
        if(! usuario.getPassword1().equals(usuario.getPassword2()))
        {
            bindingResult.rejectValue("password1", "PasswordNotEquals");
        }

        if (usuario.getPassword1() == null || usuario.getPassword1().isBlank()) {
            bindingResult.rejectValue("password1", "PasswordEmpty", "La contraseña no puede estar vacía");
        }

        if (bindingResult.hasErrors())
        {
            model.addAttribute("usuario", usuario);
            return "/usuario/form";
        }
        //asignamos el password encryptado
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword1()));
        //Asignamos el role por defecto
        usuario.setRol(Usuario.Rol.CLIENTE);
        //grabamos el usuario en base de datos
        usuarioRepository.save(usuario);
        ra.addFlashAttribute("registroExitoso", "Registro de Usuario exitoso");
        return "redirect:/login";
    }
}
