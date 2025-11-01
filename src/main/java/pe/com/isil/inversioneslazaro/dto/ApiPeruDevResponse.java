package pe.com.isil.inversioneslazaro.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApiPeruDevResponse {

    private boolean success;
    private ApiPeruDevData data;

    @Data
    public static class ApiPeruDevData {

        // Mapea el campo "numero" del JSON a nuestra variable "numero"
        private String numero;

        // Mapea "nombre_completo"
        @JsonProperty("nombre_completo")
        private String nombreCompleto;

        private String nombres;

        @JsonProperty("apellido_paterno")
        private String apellidoPaterno;

        @JsonProperty("apellido_materno")
        private String apellidoMaterno;

        // El "codigo_verificacion" no lo necesitamos, así que no lo mapeamos.
    }
}