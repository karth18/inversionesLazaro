package pe.com.isil.inversioneslazaro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    // **IMPORTANTE:** Cambia 'http://localhost:8080' por la URL de tu servidor
    private final String URL_BASE = "http://localhost:8080/registrar/verificar?token=";

    public void enviarCorreoVerificacion(String destinatario, String token) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Verificación de Correo - Inversiones Lázaro");

        String urlVerificacion = URL_BASE + token;

        mensaje.setText(
                "¡Hola!\n\n" +
                        "Gracias por registrarte. Para completar tu registro y verificar tu cuenta, haz clic en el siguiente enlace:\n\n" +
                        urlVerificacion +
                        "\n\nEste enlace es válido por 24 horas.\n\n" +
                        "Atentamente,\nEquipo de Inversiones Lázaro"
        );

        mailSender.send(mensaje);
    }
}
