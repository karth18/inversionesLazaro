package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile; // <-- Importar

@Data
@NoArgsConstructor
@Entity
@Table(name = "banners_home")
public class BannerHome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String titulo;

    @Column(length = 255)
    private String subtitulo;

    // --- CAMBIO 1: Renombrar el campo ---
    @Column(name = "imagen_nombre") // Guardará "banner-12345.jpg"
    private String imagenNombre;

    @Column(length = 500)
    private String enlaceBoton;

    @Column(length = 50)
    private String textoBoton;

    private Boolean estaActivo = true;

    private Integer orden;

    // --- CAMBIO 2: Campo temporal para el archivo ---
    @Transient // No se guarda en la BD
    private MultipartFile archivoImagen;

    // --- CAMBIO 3: Método de ayuda ---
    // (Tu index.html usaba 'imagenFondoUrl', así que creamos un getter con ese nombre)
    public String getImagenFondoUrl() {
        if (this.imagenNombre == null || this.imagenNombre.isEmpty()) {
            return "/img/imagenFondoCocina.png"; // Imagen por defecto
        }
        // Devuelve la ruta web (la que ya usas para productos)
        return "/uploads/" + this.imagenNombre;
    }



}