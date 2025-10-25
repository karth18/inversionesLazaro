package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
@Entity
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Usuario usuario;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String departamento;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String provincia;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String distrito;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String calleAvenida;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String numeroCalle;

    @Column(length = 100)
    private String dptoInterior; // Opcional

    private boolean esPrincipal = false;
}