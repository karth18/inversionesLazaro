package pe.com.isil.inversioneslazaro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService; // El servicio de envío que crearás

    public Usuario registrarNuevoUsuario(Usuario usuario) {
        // 1. Configurar el token y la expiración
        String token = generarToken();
        usuario.setTokenVerificacion(token);
        usuario.setFechaExpiracionToken(LocalDateTime.now().plusDays(1)); // Válido por 24 horas
        usuario.setEstado(false);

        // 2. Guardar en la BD
        Usuario nuevoUsuario = usuarioRepository.save(usuario);

        // 3. Enviar el correo
        emailService.enviarCorreoVerificacion(nuevoUsuario.getEmail(), token);

        return nuevoUsuario;
    }

    public String generarToken() {
        return UUID.randomUUID().toString();
    }

    // nuevo metodo para la activacion el anterior no funcinaba veremos este
    @Transactional
    public void activarUsuario(Usuario usuario) {
        usuario.setEstado(true); // Cambia el estado a activo (true)
        usuario.setTokenVerificacion(null);
        usuario.setFechaExpiracionToken(null);
        // El save() ahora es seguro porque está en una transacción
        usuarioRepository.save(usuario);
    }
}
