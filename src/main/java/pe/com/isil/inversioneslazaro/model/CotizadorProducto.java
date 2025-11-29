package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "cotizador_productos")
public class CotizadorProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // Ej: Mesa de Trabajo
    private String imagenUrl; // URL de la foto

    private Double altoEstandar; // Nuevo campo que pediste (Altura)
    private Double fondoEstandar;

    private boolean activo = true; // Eliminación lógica

    // Relación con Rangos de Precio
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    private List<CotizadorRango> rangos;

    // Relación con Componentes Extras
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    private List<CotizadorComponente> componentes;
}