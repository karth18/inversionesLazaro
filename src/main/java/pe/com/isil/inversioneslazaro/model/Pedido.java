package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // Importar para el código único

@Data
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // <-- ID Interno (rápido, para la BD)

    // --- ¡NUEVO! ---
    @Column(name = "codigo_pedido", unique = true, nullable = false, length = 36)
    private String codigoPedido; // <-- ID Público (para el cliente)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direccion_id", nullable = false)
    private Direccion direccion;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    // --- ¡MEJORADO! ---
    // Usamos un Enum para los estados
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoPedido estado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "stripe_charge_id")
    private String stripeChargeId;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoDetalle> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        // --- ¡NUEVO! ---
        // Genera un código único al crear el pedido
        if (this.codigoPedido == null) {
            // Genera un UUID (ej: f47ac10b-58cc-4372-a567-0e02b2c3d479)
            // y toma los primeros 8 caracteres para hacerlo más corto
            this.codigoPedido = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    // --- ¡NUEVO! ---
    // Enum para tus 4 estados
    public enum EstadoPedido {
        ORDEN_RECIBIDA, // (Estado inicial después de PENDIENTE)
        EN_PREPARACION,
        EN_CAMINO,
        FINALIZADO,
        PENDIENTE, // (Para el pago)
        CANCELADO
    }
}