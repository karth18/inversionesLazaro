package pe.com.isil.inversioneslazaro.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pe.com.isil.inversioneslazaro.model.ConfiguracionEmpresa;
import pe.com.isil.inversioneslazaro.model.Pedido;
import pe.com.isil.inversioneslazaro.repository.ConfiguracionEmpresaRepository;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private ConfiguracionEmpresaRepository configRepo;

    @Value("${spring.mail.username}")
    private String remitente;

    private final String URL_BASE = "http://localhost:8080";

    // ==========================================
    // 1. MÉTODO ANTIGUO (Verificación - Texto Plano)
    // ==========================================
    @Async
    public void enviarCorreoVerificacion(String destinatario, String token) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setFrom(remitente);
            mensaje.setSubject("Verificación de Correo - Inversiones Lázaro");

            String urlVerificacion = URL_BASE + "/registrar/verificar?token=" + token;

            mensaje.setText(
                    "¡Hola!\n\n" +
                            "Gracias por registrarte. Para completar tu registro y verificar tu cuenta, haz clic en el siguiente enlace:\n\n" +
                            urlVerificacion +
                            "\n\nEste enlace es válido por 24 horas.\n\n" +
                            "Atentamente,\nEquipo de Inversiones Lázaro"
            );

            mailSender.send(mensaje);
        } catch (Exception e) {
            System.err.println("Error enviando correo de verificación: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. MÉTODO NUEVO (Estado Pedido - HTML Dinámico)
    // ==========================================
    @Async
    public void enviarCorreoEstadoPedido(Pedido pedido) {
        try {
            // 1. Obtener Configuración de la BD (Textos editables)
            ConfiguracionEmpresa config = configRepo.findById(1L).orElse(new ConfiguracionEmpresa());

            // Valores por defecto (fallback) por si la BD está vacía
            if (config.getUrlLogo() == null) config.setUrlLogo(URL_BASE + "/images/logo-default.png");
            if (config.getTextoBoton() == null) config.setTextoBoton("Ver mi Pedido");
            if (config.getFooterAgradecimiento() == null) config.setFooterAgradecimiento("Gracias por tu compra.");

            String titulo = "";
            String mensajeCuerpo = "";

            // 2. Seleccionar el mensaje según el estado y procesar variables
            switch (pedido.getEstado()) {

                // CASO 1: CLIENTE COMPRA (BIENVENIDA)
                case PENDIENTE:
                    titulo = config.getAsuntoBienvenida() != null ? config.getAsuntoBienvenida() : "¡Hemos recibido tu orden!";
                    mensajeCuerpo = procesarVariables(
                            config.getMensajeBienvenida() != null ? config.getMensajeBienvenida() : "Hola {cliente}, recibimos tu orden {codigo}.",
                            pedido, null
                    );
                    break;

                // CASO 2: EN CAMINO
                case EN_CAMINO:
                    titulo = config.getAsuntoEnCamino() != null ? config.getAsuntoEnCamino() : "Tu pedido está en camino";
                    mensajeCuerpo = procesarVariables(
                            config.getMensajeEnCamino() != null ? config.getMensajeEnCamino() : "Tu pedido {codigo} ha salido a ruta.",
                            pedido, null
                    );
                    break;

                // CASO 3: ENTREGADO
                case ENTREGADO:
                    titulo = config.getAsuntoEntregado() != null ? config.getAsuntoEntregado() : "¡Pedido Entregado!";
                    mensajeCuerpo = procesarVariables(
                            config.getMensajeEntregado() != null ? config.getMensajeEntregado() : "Esperamos que disfrutes tu compra.",
                            pedido, null
                    );
                    break;

                // CASO 4: REAGENDADO (Con Motivo y Fecha)
                case REAGENDADO:
                    titulo = config.getAsuntoReagendado() != null ? config.getAsuntoReagendado() : "Actualización de Entrega";

                    // Buscar motivo en el historial
                    String motivoReag = "motivos logísticos";
                    if (!pedido.getHistorialEstados().isEmpty()) {
                        String raw = pedido.getHistorialEstados().get(pedido.getHistorialEstados().size() - 1).getComentario();
                        if (raw.contains("REAGENDADO:")) {
                            int inicio = raw.indexOf("REAGENDADO:") + 11;
                            int fin = raw.indexOf(". Nueva fecha");
                            if (fin > inicio) motivoReag = raw.substring(inicio, fin).trim();
                        }
                    }

                    mensajeCuerpo = procesarVariables(
                            config.getMensajeReagendado() != null ? config.getMensajeReagendado() : "Reprogramado por: {motivo}. Nueva fecha: {fecha}.",
                            pedido, motivoReag
                    );
                    break;

                // CASO 5: CANCELADO
                case CANCELADO:
                    titulo = config.getAsuntoCancelado() != null ? config.getAsuntoCancelado() : "Pedido Cancelado";
                    mensajeCuerpo = procesarVariables(
                            config.getMensajeCancelado() != null ? config.getMensajeCancelado() : "Cancelado por: {motivo}.",
                            pedido, pedido.getMotivoCancelacion()
                    );
                    break;

                default:
                    // Para otros estados (como ORDEN_RECIBIDA o EN_PREPARACION) no enviamos correo.
                    return;
            }

            // 3. Preparar el HTML
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            Context context = new Context();
            context.setVariable("pedido", pedido);
            context.setVariable("config", config);
            context.setVariable("titulo", titulo);
            context.setVariable("mensaje", mensajeCuerpo);
            context.setVariable("linkMisPedidos", URL_BASE + "/misPedidos/detalle/" + pedido.getId());

            String html = templateEngine.process("email/plantilla-pedido", context);

            helper.setTo(pedido.getUsuario().getEmail());
            helper.setFrom(remitente);
            helper.setSubject(titulo + " - Orden " + pedido.getCodigoPedido());
            helper.setText(html, true);

            mailSender.send(message);
            System.out.println("Correo enviado (" + pedido.getEstado() + ") a: " + pedido.getUsuario().getEmail());

        } catch (MessagingException e) {
            System.err.println("Error al enviar correo HTML: " + e.getMessage());
        }
    }

    // ==========================================
    // 3. MÉTODO AUXILIAR PARA REEMPLAZAR VARIABLES
    // ==========================================
    private String procesarVariables(String textoBase, Pedido pedido, String motivoExtra) {
        if (textoBase == null) return "";

        String texto = textoBase;

        // Reemplazo básico
        texto = texto.replace("{cliente}", pedido.getUsuario().getNombres());
        texto = texto.replace("{codigo}", pedido.getCodigoPedido());

        // Reemplazo de fecha (solo si existe)
        if (pedido.getFechaEntregaEstimada() != null) {
            texto = texto.replace("{fecha}", pedido.getFechaEntregaEstimada().toLocalDate().toString());
        } else {
            texto = texto.replace("{fecha}", "Pronto");
        }

        // Reemplazo de motivo (si aplica)
        if (motivoExtra != null) {
            texto = texto.replace("{motivo}", motivoExtra);
        } else {
            texto = texto.replace("{motivo}", "");
        }

        return texto;
    }
}