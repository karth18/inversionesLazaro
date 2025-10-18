package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Marca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMarca")
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    private boolean estado = true;
}
