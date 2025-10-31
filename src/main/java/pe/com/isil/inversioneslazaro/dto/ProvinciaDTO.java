package pe.com.isil.inversioneslazaro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.com.isil.inversioneslazaro.model.Provincia;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvinciaDTO {
    private Long id;
    private String nombre;

    // Constructor para mapear fácil
    public ProvinciaDTO(Provincia provincia) {
        this.id = provincia.getId();
        this.nombre = provincia.getNombre();
    }
}
