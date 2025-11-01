package pe.com.isil.inversioneslazaro.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.isil.inversioneslazaro.dto.DniConsultaResponse;
import pe.com.isil.inversioneslazaro.service.ApiPeruDevService;

@RestController
@RequestMapping("/api/v1/consulta") // URL base para nuestras APIs
public class DniConsultaController {

    private final ApiPeruDevService apiPeruDevService;

    public DniConsultaController(ApiPeruDevService apiPeruDevService) {
        this.apiPeruDevService = apiPeruDevService;
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<DniConsultaResponse> consultarDni(@PathVariable String dni) {
        // Validar DNI (8 dígitos)
        if (dni == null || !dni.matches("^\\d{8}$")) {
            return ResponseEntity.badRequest().build(); // Mal request
        }

        return apiPeruDevService.consultarDni(dni)
                .map(ResponseEntity::ok) // Si lo encuentra, 200 OK con el DTO
                .orElse(ResponseEntity.notFound().build()); // Si no, 404 Not Found
    }
}