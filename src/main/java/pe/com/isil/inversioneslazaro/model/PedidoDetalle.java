package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor // Necesario para JPA
@Entity
@Table(name = "pedido_detalles") // La tabla que guarda los productos de un pedido
public class PedidoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Relación: A qué Pedido pertenece ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    // --- Relación: Qué Producto se compró ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private int cantidad;

    // --- Importante ---
    // Guarda el precio al momento de la compra,
    // por si el precio del producto cambia en el futuro.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    // Constructor útil que usa el PaymentController
    public PedidoDetalle(Pedido pedido, Producto producto, int cantidad, BigDecimal precioUnitario) {
        this.pedido = pedido;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }
}