package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class ImagenProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ruta donde se guarda el archivo (ej: nombre_archivo.jpg)
    @Column(nullable = false, length = 255)
    private String ruta;

    // Campo para indicar si es la imagen principal o una secundaria
    private boolean esPrincipal = false;

    // Relación Muchos a Uno: Muchas imágenes pertenecen a Un producto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // Aún pendiente si agregar una entidad orden por evaluar pero ahi lo tengo porsiacaso
    // private Integer orden;
}