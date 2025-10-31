package pe.com.isil.inversioneslazaro.dto;

import lombok.Data;
import pe.com.isil.inversioneslazaro.model.Direccion;

@Data
public class DireccionDTO {

    private Long id;
    private String calleAvenida;
    private String numeroCalle;
    private String dptoInterior;
    private boolean esPrincipal;

    // Almacenamos solo los nombres (String)
    private String departamento;
    private String provincia;
    private String distrito;

    private Long departamentoId;
    private Long provinciaId;
    private Long distritoId;

    // Constructor para mapear fácil desde la Entidad
    public DireccionDTO(Direccion direccion) {
        this.id = direccion.getId();
        this.calleAvenida = direccion.getCalleAvenida();
        this.numeroCalle = direccion.getNumeroCalle();
        this.dptoInterior = direccion.getDptoInterior();
        this.esPrincipal = direccion.isEsPrincipal();

        // Asumimos que los getters inicializarán el proxy
        // (Esto debe llamarse dentro de una transacción)
        this.departamento = direccion.getDepartamento().getNombre();
        this.provincia = direccion.getProvincia().getNombre();
        this.distrito = direccion.getDistrito().getNombre();

        this.departamentoId = direccion.getDepartamento().getId();
        this.provinciaId = direccion.getProvincia().getId();
        this.distritoId = direccion.getDistrito().getId();
    }
}