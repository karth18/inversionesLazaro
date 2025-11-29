package pe.com.isil.inversioneslazaro.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "cotizador_componentes")
public class CotizadorComponente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // Ej: Ruedas, Repisa
    private BigDecimal precio;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    @JsonIgnore
    private CotizadorProducto producto;

    private boolean estado;
}