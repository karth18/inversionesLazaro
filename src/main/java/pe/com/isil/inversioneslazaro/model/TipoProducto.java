package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class TipoProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTipo")
    private Long id;

    private String nombre;

    @ManyToOne
    @JoinColumn(name = "idCate", referencedColumnName = "idCate", nullable = false)
    private Categoria idCate;

    private boolean estado = true;
}
