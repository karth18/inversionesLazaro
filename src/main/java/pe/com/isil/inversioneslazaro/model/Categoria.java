package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCate")
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String nombre;

    private boolean estado = true;
}
