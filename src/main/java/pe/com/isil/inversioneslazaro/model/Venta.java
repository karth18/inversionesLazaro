package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "venta_id")
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    // --- sustituimos la relación a Cliente por campos simples ---
    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "cliente_nombre", length = 250)
    private String clienteNombre;
    // -----------------------------------------------------------

    @Column(name = "total", nullable = false)
    private Double total;

    @Column(name = "metodo_pago", length = 60)
    private String metodoPago;

    @ManyToOne
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetalleVenta> detalles = new ArrayList<>();

    private String createdBy;
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (fecha == null) fecha = LocalDateTime.now();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public void addDetalle(DetalleVenta d) {
        d.setVenta(this);
        this.detalles.add(d);
    }
}