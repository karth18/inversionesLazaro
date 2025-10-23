package pe.com.isil.inversioneslazaro.dto;

import lombok.Data;
import pe.com.isil.inversioneslazaro.model.Producto;

import java.math.BigDecimal;

@Data
public class CarritoItem {

    private Long productoId;
    private String nombre;
    private String codigo;
    private String imagenUrl;
    private int cantidad;
    private boolean seleccionado;

    // --- CAMPOS MODIFICADOS ---
    private BigDecimal precioUnitario;    // El precio que SÍ va a pagar (con descuento si hay)
    private BigDecimal precioSinDescuento;  // El precio original (para tachar)
    private boolean tieneDescuento;

    // --- CONSTRUCTOR MODIFICADO ---
    public CarritoItem(Producto producto) {
        this.productoId = producto.getId();
        this.nombre = producto.getNomPro();
        this.codigo = producto.getCodPro();

        // ... (tu lógica de imagen)
        if (producto.getImagenes() != null && !producto.getImagenes().isEmpty()) {
            this.imagenUrl = "/uploads/" + producto.getImagenes().get(0).getRuta();
        } else {
            this.imagenUrl = "/images/placeholder.png";
        }

        this.cantidad = 1;
        this.seleccionado = true;

        // --- LÓGICA DE PRECIO CON DESCUENTO ---
        BigDecimal precioRegular = producto.getPrecio();
        BigDecimal precioOferta = producto.getPrecioOferta();

        // Comprueba si hay un precio de oferta válido
        if (precioOferta != null && precioOferta.compareTo(BigDecimal.ZERO) > 0 && precioOferta.compareTo(precioRegular) < 0) {
            // Si hay oferta, el precioUnitario es el de oferta
            this.precioUnitario = precioOferta;
            this.precioSinDescuento = precioRegular;
            this.tieneDescuento = true;
        } else {
            // Si no hay oferta, el precioUnitario es el regular
            this.precioUnitario = precioRegular;
            this.precioSinDescuento = null; // No hay precio original que mostrar
            this.tieneDescuento = false;
        }
    }

    // Este método AHORA usa el precioUnitario (que ya tiene el descuento)
    // No necesita cambios.
    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(new BigDecimal(cantidad));
    }
}