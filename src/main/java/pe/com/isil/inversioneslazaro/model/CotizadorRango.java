package pe.com.isil.inversioneslazaro.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "cotizador_rangos")
public class CotizadorRango {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer minLargo; // Ej: 100 cm
    private Integer maxLargo; // Ej: 150 cm

    // Precios BASE para este rango de medidas
    private BigDecimal precio201; // Económico
    private BigDecimal precio304; // Quirúrgico

    @ManyToOne
    @JoinColumn(name = "producto_id")
    @JsonIgnore
    private CotizadorProducto producto;
}