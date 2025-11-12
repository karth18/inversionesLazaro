package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "carrito_items")
public class CarritoItemPersistente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario; // El dueño del carrito

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto; // El producto en el carrito

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private boolean seleccionado = true;

    // Constructor útil
    public CarritoItemPersistente(Usuario usuario, Producto producto, int cantidad, boolean seleccionado) {
        this.usuario = usuario;
        this.producto = producto;
        this.cantidad = cantidad;
        this.seleccionado = seleccionado;
    }
}