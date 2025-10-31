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


    private BigDecimal precioUnitario;    // El precio que SÍ va a pagar (con descuento si hay)
    private BigDecimal precioSinDescuento;  // El precio original (para tachar)
    private boolean tieneDescuento;
    private int stock;


    public CarritoItem(Producto producto) {
        this.productoId = producto.getId();
        this.nombre = producto.getNomPro();
        this.codigo = producto.getCodPro();


        if (producto.getImagenes() != null && !producto.getImagenes().isEmpty()) {
            this.imagenUrl = "/uploads/" + producto.getImagenes().get(0).getRuta();
        } else {
            this.imagenUrl = "/images/placeholder.png";
        }

        this.cantidad = 1;
        this.seleccionado = true;
        this.stock = producto.getStock();


        BigDecimal precioRegular = producto.getPrecio();
        BigDecimal precioOferta = producto.getPrecioOferta();
        this.precioSinDescuento = precioRegular;

        if (precioOferta != null && precioOferta.compareTo(BigDecimal.ZERO) > 0 && precioOferta.compareTo(precioRegular) < 0) {

            this.precioUnitario = precioOferta;
            this.precioSinDescuento = precioRegular;
            this.tieneDescuento = true;
        } else {

            this.precioUnitario = precioRegular;
            this.precioSinDescuento = null;
            this.tieneDescuento = false;
        }
    }
    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(new BigDecimal(cantidad));
    }

    public BigDecimal getSubtotalOriginal() {

        return this.precioSinDescuento.multiply(new BigDecimal(this.cantidad));
    }
}