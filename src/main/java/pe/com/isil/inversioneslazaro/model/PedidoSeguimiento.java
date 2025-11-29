package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "pedido_seguimiento")
public class PedidoSeguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el pedido principal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    // El estado en ese momento exacto
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Pedido.EstadoPedido estado;

    // Fecha exacta del cambio (para la línea de tiempo visual)
    @Column(nullable = false)
    private LocalDateTime fechaCambio;

    // Mensaje opcional: "Retraso por lluvias", "Cliente no estaba", etc.
    @Column(length = 500)
    private String comentario;

    // Usuario que hizo el cambio (opcional, para auditoría interna)
    private String usuarioResponsable;

    public PedidoSeguimiento(Pedido pedido, Pedido.EstadoPedido estado, String comentario, String usuarioResponsable) {
        this.pedido = pedido;
        this.estado = estado;
        this.comentario = comentario;
        this.usuarioResponsable = usuarioResponsable;
        this.fechaCambio = LocalDateTime.now();
    }
}