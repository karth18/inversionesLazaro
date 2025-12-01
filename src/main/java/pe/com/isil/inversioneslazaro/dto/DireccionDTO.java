package pe.com.isil.inversioneslazaro.dto;

import lombok.Data;
import pe.com.isil.inversioneslazaro.model.Direccion;

@Data
public class DireccionDTO {

    private Long id;
    private String calleAvenida;
    private String numeroCalle;
    private String dptoInterior;
    private String referencia;
    private boolean esPrincipal;

    // Campos simples (String) para mostrar en la vista
    private String departamento;
    private String provincia;
    private String distrito;

    // Campos de ID (Long) para el JavaScript (al editar)
    private Long departamentoId;
    private Long provinciaId;
    private Long distritoId;

    // Constructor que hace la "traducción"
    public DireccionDTO(Direccion direccion) {
        this.id = direccion.getId();
        this.calleAvenida = direccion.getCalleAvenida();
        this.numeroCalle = direccion.getNumeroCalle();
        this.dptoInterior = direccion.getDptoInterior();
        this.esPrincipal = direccion.isEsPrincipal();
        this.referencia = direccion.getReferencia();

        // Evita errores si alguna relación es nula (aunque no debería)
        if (direccion.getDepartamento() != null) {
            this.departamento = direccion.getDepartamento().getNombre();
            this.departamentoId = direccion.getDepartamento().getId();
        }
        if (direccion.getProvincia() != null) {
            this.provincia = direccion.getProvincia().getNombre();
            this.provinciaId = direccion.getProvincia().getId();
        }
        if (direccion.getDistrito() != null) {
            this.distrito = direccion.getDistrito().getNombre();
            this.distritoId = direccion.getDistrito().getId();
        }
    }
}