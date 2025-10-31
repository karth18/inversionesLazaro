package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Debe seleccionar un departamento")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDepartamento", nullable = false)
    private Departamento departamento;

    @NotNull(message = "Debe seleccionar una provincia")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provincia_id", nullable = false)
    private Provincia provincia;

    @NotNull(message = "Debe seleccionar un distrito")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distrito_id", nullable = false)
    private Distrito distrito;

    @NotBlank(message = "La calle/avenida es obligatorio")
    @Column(nullable = false, length = 200)
    private String calleAvenida;

    @NotBlank(message = "El número es obligatorio")
    @Column(nullable = false, length = 10)
    private String numeroCalle;

    @Column(length = 100)
    private String dptoInterior; // Opcional

    private boolean esPrincipal = false;
}