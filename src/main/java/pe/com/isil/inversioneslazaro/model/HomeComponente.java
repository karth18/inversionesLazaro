package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@Entity
@Table(name = "home_componentes")
public class HomeComponente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El "tipo" de componente. Ej: OFERTA, CLASIFICACION, etc.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Seccion seccion;

    // El título que se mostrará (ej: "Industrial", "Oferta Verano")
    @Column(length = 100)
    private String titulo;

    // El nombre del archivo de imagen (ej: "12345_oferta-cocina.jpg")
    @Column(name = "imagen_nombre")
    private String imagenNombre;

    // La URL a la que se dirige (ej: /catalogo/producto/5)
    @Column(name = "enlace_url", length = 500)
    private String enlaceUrl;

    // Para ordenar los items (ej: Oferta 1, Oferta 2...)

    private Integer orden;

    // Para ocultar/mostrar secciones (como pediste para Ofertas)
    private Boolean estaActivo = true;

    // Campo temporal para subir la imagen
    @Transient
    private MultipartFile archivoImagen;

    // Enum para definir las secciones
    public enum Seccion {
        OFERTA,         // Para los 4 cuadros flotantes
        ANUNCIO_TIRA,   // Para el rectángulo largo
        CLASIFICACION   // Para Industrial, Hogar, etc.
    }

    // Getter de ayuda para construir la URL de la imagen
    public String getImagenUrl() {
        if (this.imagenNombre == null || this.imagenNombre.isEmpty()) {
            return null; // El HTML se encargará del 'no disponible'
        }
        return "/uploads/" + this.imagenNombre;
    }
}