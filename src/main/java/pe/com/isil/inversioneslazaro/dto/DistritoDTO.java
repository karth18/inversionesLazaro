package pe.com.isil.inversioneslazaro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.com.isil.inversioneslazaro.model.Distrito;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistritoDTO {
    private Long id;
    private String nombre;

    // Constructor para mapear fácil
    public DistritoDTO(Distrito distrito) {
        this.id = distrito.getId();
        this.nombre = distrito.getNombre();
    }
}