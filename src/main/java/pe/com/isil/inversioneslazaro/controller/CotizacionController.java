package pe.com.isil.inversioneslazaro.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pe.com.isil.inversioneslazaro.dto.SolicitudCotizacionDTO;

import java.util.Map;

@Controller
@RequestMapping("/cotizador")
public class CotizacionController {

    @GetMapping
    public String verCotizador() {
        return "personaliza/index";
    }

    @PostMapping("/enviar")
    @ResponseBody
    public ResponseEntity<?> procesarCotizacion(@RequestBody SolicitudCotizacionDTO cotizacion) {
        try {
            // 1. Aquí podrías guardar en BD en una tabla "Leads"
            // leadService.guardar(cotizacion);

            // 2. Aquí podrías enviar un email al asesor de ventas
            // emailService.enviarAlertaVentas(cotizacion);

            return ResponseEntity.ok(Map.of("success", true, "message", "¡Tu solicitud ha sido enviada a un asesor!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Error al procesar"));
        }
    }
}