package pe.com.isil.inversioneslazaro.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;
import pe.com.isil.inversioneslazaro.service.UsuarioService;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/registrar")
public class RegistroUsuarioController {



    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioService usuarioService;

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
        boolean usuarioExiste = usuarioRepository.findByEmail(usuario.getEmail()).isPresent();
        if (usuarioExiste)
        {
            bindingResult.rejectValue("email", "EmailAlredayExists.usuario.email");
        }
        //Validar la coincidencia de contraseñas
        if(!usuario.getPassword1().equals(usuario.getPassword2()))
        {
            bindingResult.rejectValue("password1", "PasswordNotEquals");
        }
        if (usuario.getPassword1() == null || usuario.getPassword1().isBlank()) {
            bindingResult.rejectValue("password1", "PasswordEmpty", "La contraseña no puede estar vacía");
        }
        if(usuario.getPolitica() == null || !usuario.getPolitica()){
            bindingResult.rejectValue("politica", "politicasNoAceptadas.usuario.politica");
        }
        if (bindingResult.hasErrors())
        {
            model.addAttribute("usuario", usuario);
            return "/usuario/form";
        }

        //asignamos el password encryptado
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword1()));
        usuario.setRol(Usuario.Rol.CLIENTE);


        //cambios para poder validar con correo
        usuarioService.registrarNuevoUsuario(usuario);


        //Asignamos el role por defecto
//        usuario.setEstado(true);
//        usuario.setRol(Usuario.Rol.CLIENTE);
//        //grabamos el usuario en base de datos
//        usuarioRepository.save(usuario);
        ra.addFlashAttribute("emailVerificacion", usuario.getEmail());
        return "redirect:/registrar/instrucciones-verificacion";
    }



    // verificacion de la cuenta de correo

    @GetMapping("/verificar")
    public String verificarCuenta(@RequestParam("token") String token, Model model) {

        Usuario usuario = usuarioRepository.findByTokenVerificacion(token);

        if (usuario == null) {
            model.addAttribute("mensaje", "Error: El token de verificación es inválido.");
            return "verificacion_resultado"; // Vista para mostrar el resultado
        }

        if (usuario.getFechaExpiracionToken().isBefore(LocalDateTime.now())) {
            model.addAttribute("mensaje", "Error: El token ha expirado. Por favor, regístrate de nuevo o solicita un nuevo correo.");
            // Opcional: Implementar lógica para reenviar token
            return "verificacion_resultado";
        }

        //modificacion actual
        usuarioService.activarUsuario(usuario);

        //codigo anterior hasta que funcione el nuevo codigo

//        // El token es válido: Activar la cuenta
//        usuario.setEstado(true);
//        usuario.setTokenVerificacion(null); // Limpiar el token usado
//        usuario.setFechaExpiracionToken(null);
//        usuarioRepository.save(usuario);

        model.addAttribute("mensaje", "¡Felicidades! Tu cuenta ha sido verificada exitosamente. Ya puedes iniciar sesión.");
        return "verificacion_resultado"; // Muestra el mensaje de éxito
    }



    @GetMapping("/instrucciones-verificacion")
    public String mostrarInstruccionesVerificacion(@ModelAttribute("emailVerificacion") final String email, Model model) {
        // Si la redirección tiene el flash attribute, lo muestra.
        // Si el usuario llega aquí directamente o recarga, la vista sabrá qué hacer.

        if (email != null && !email.isBlank()) {
            model.addAttribute("email", email);
        } else {
            // En caso de que se recargue sin FlashAttribute (ej: recarga F5),
            // puedes mostrar un mensaje genérico.
            model.addAttribute("mensajeGenerico", "Revisa tu bandeja de entrada o spam para verificar tu cuenta.");
        }

        return "usuario/instrucciones_verificacion"; // Crearemos esta vista
    }

}
