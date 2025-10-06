package pe.com.isil.inversioneslazaro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class CategoriaDTO {
    public Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "Máximo 150 caracteres")
    public String nombre;

    @NotBlank(message = "El slug es obligatorio")
    @Size(max = 150, message = "Máximo 150 caracteres")
    public String slug;

    public String descripcion;
    public Boolean activo = true;
}
