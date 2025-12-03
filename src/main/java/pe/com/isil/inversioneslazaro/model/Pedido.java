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


    // Usamos un Enum para los estados
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoPedido estado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "stripe_charge_id")
    private String stripeChargeId;

    @Column(length = 500)
    private String motivoCancelacion;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoDetalle> detalles = new ArrayList<>();

    // 1. ¿Quién está preparando el pedido en almacén?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almacenero_id")
    private Usuario almacenero;

    // 2. ¿Qué chofer lo está llevando?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chofer_id")
    private Usuario chofer;

    // 3. Prueba de entrega (nombre del archivo de la foto)
    @Column(name = "foto_entrega")
    private String fotoEntrega;

    // Control de concurrencia (Evita que dos personas editen al mismo tiempo)
    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        // 1. Fecha de creación
        this.fechaCreacion = LocalDateTime.now();

        // 2. Código único
        if (this.codigoPedido == null) {
            this.codigoPedido = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        // 3. CALCULO AUTOMÁTICO DE FECHA DE ENTREGA (NUEVO)
        if (this.fechaEntregaEstimada == null) {
            int diasTransporteEstandar = 3; // Tú defines cuántos días tarda el envío base
            int maxDiasProducto = 0;

            // Revisamos qué producto tarda más en estar listo
            if (this.detalles != null && !this.detalles.isEmpty()) {
                for (PedidoDetalle d : this.detalles) {
                    // Verificamos que el producto y sus días no sean nulos para evitar errores
                    if (d.getProducto() != null && d.getProducto().getDiasProcesamiento() != null) {
                        if (d.getProducto().getDiasProcesamiento() > maxDiasProducto) {
                            maxDiasProducto = d.getProducto().getDiasProcesamiento();
                        }
                    }
                }
            }
            // Fecha Estimada = Hoy + Días del producto más lento + Días de viaje
            this.fechaEntregaEstimada = LocalDateTime.now().plusDays(maxDiasProducto + diasTransporteEstandar);
        }

        // 4. Primer historial (Lo que ya tenías)
        if (this.historialEstados == null) {
            this.historialEstados = new ArrayList<>();
        }
        if (this.historialEstados.isEmpty()) {
            if (this.estado == null) {
                this.estado = EstadoPedido.PENDIENTE;
            }
            PedidoSeguimiento primerPaso = new PedidoSeguimiento();
            primerPaso.setPedido(this);
            primerPaso.setEstado(this.estado);
            primerPaso.setFechaCambio(LocalDateTime.now());
            primerPaso.setUsuarioResponsable("Sistema");
            primerPaso.setComentario("Orden creada. Fecha estimada de entrega calculada.");
            this.historialEstados.add(primerPaso);
        }
    }
//codigo nuevo revision
    @Column(name = "fecha_entrega_estimada")
    private LocalDateTime fechaEntregaEstimada;

    // Relación para poder pintar el historial en el HTML
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("fechaCambio ASC") // Importante para que salga en orden cronológico
    private List<PedidoSeguimiento> historialEstados = new ArrayList<>();

    // Método helper para calcular la entrega basada en los productos
    public void calcularFechaEntrega(int diasTransporteEstandar) {
        int maxDiasProducto = 0;
        for (PedidoDetalle d : this.detalles) {
            if (d.getProducto().getDiasProcesamiento() > maxDiasProducto) {
                maxDiasProducto = d.getProducto().getDiasProcesamiento();
            }
        }
        // Fecha hoy + lo que tarda el producto más lento + tiempo de viaje
        this.fechaEntregaEstimada = LocalDateTime.now().plusDays(maxDiasProducto + diasTransporteEstandar);
    }

//    fin de codigo nuevo


    // Enum para tus 4 estados
    public enum EstadoPedido {
        PENDIENTE,
        ORDEN_RECIBIDA, // (Estado inicial después de PENDIENTE)
        EN_PREPARACION,
        EMPAQUETADO,
        EN_CAMINO,
        REAGENDADO,
        ENTREGADO,
        FINALIZADO,
        CANCELADO

    }
}