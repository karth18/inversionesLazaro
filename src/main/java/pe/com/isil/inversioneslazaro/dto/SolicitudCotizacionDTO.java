package pe.com.isil.inversioneslazaro.dto;

import lombok.Data;

@Data
public class SolicitudCotizacionDTO {
    private String tipoProducto; // "Mesa", "Cocina", "Carrito"
    private String calidadAcero; // "201 (Económico)", "304 (Quirúrgico)"
    private Integer largo; // en cm
    private Integer ancho; // en cm
    private Integer alto; // en cm (opcional)
    private String detallesAdicionales; // Checkboxes unidos (ej: "Con Ruedas, Con Repisa")
    private Double precioEstimado;

    // Datos de contacto
    private String nombreCliente;
    private String telefono;
    private String email;
}