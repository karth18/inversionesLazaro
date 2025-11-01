package pe.com.isil.inversioneslazaro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pe.com.isil.inversioneslazaro.dto.ApiPeruDevRequest;
import pe.com.isil.inversioneslazaro.dto.ApiPeruDevResponse;
import pe.com.isil.inversioneslazaro.dto.DniConsultaResponse;

import java.util.Optional;

@Service
public class ApiPeruDevService {

    private final RestTemplate restTemplate;

    @Value("${api.perudev.token}")
    private String apiToken;

    // URL CORRECTA (de tu documentación)
    private final String API_URL = "https://apiperu.dev/api/dni";

    public ApiPeruDevService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public Optional<DniConsultaResponse> consultarDni(String dni) {
        try {
            // 1. Configurar Headers (como dice la doc)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiToken);
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json"); // Añadido según tu doc

            // 2. Crear el Body (con el DTO que creamos)
            ApiPeruDevRequest requestBody = new ApiPeruDevRequest(dni);

            // 3. Crear la entidad HTTP (Headers + Body)
            HttpEntity<ApiPeruDevRequest> entity = new HttpEntity<>(requestBody, headers);

            // 4. Hacer la llamada POST (en lugar de GET)
            ResponseEntity<ApiPeruDevResponse> response = restTemplate.exchange(
                    API_URL,                // La URL base
                    HttpMethod.POST,        // El método POST
                    entity,                 // La entidad (headers + body)
                    ApiPeruDevResponse.class // La clase DTO para la respuesta
            );

            // 5. Procesar la respuesta (usando los campos correctos)
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess()) {
                ApiPeruDevResponse.ApiPeruDevData data = response.getBody().getData();

                // Concatenar apellidos
                String apellidosCompletos = data.getApellidoPaterno() + " " + data.getApellidoMaterno();

                // Crear nuestra respuesta limpia para el frontend
                DniConsultaResponse consulta = new DniConsultaResponse(
                        data.getNumero(), // Usamos 'getNumero()' en lugar de 'getDni()'
                        data.getNombres(),
                        apellidosCompletos.trim()
                );
                return Optional.of(consulta);
            }

        } catch (Exception e) {
            // Manejar errores (ej. DNI no encontrado, API caída, token inválido)
            System.err.println("Error consultando API de DNI: " + e.getMessage());
        }
        return Optional.empty(); // Retorna vacío si falla
    }
}