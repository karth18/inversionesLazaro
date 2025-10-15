package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Entity
@Table(name = "productos", uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo", name = "uk_producto_codigo")
})
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producto_id")
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String codigo; // código único del producto

    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    // precio con BigDecimal (mejor precisión para dinero)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    // ruta a la foto (puede ser nombre de archivo o url)
    @Column(name = "foto_path")
    private String fotoPath;

    // auditoría básica (opcional)
    private String createdBy;
    private java.time.LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = java.time.LocalDateTime.now();
    }
}
