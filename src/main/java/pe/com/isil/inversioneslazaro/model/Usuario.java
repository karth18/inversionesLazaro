package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {


    @Id
    @NotBlank
    @Column(nullable = false, unique = true, length = 8)
    private String dni;


    @NotBlank
    @Column(nullable = false, length = 50)
    private String nombres;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String apellidos;

    @NotBlank
    @Column(length = 10)
    private String celular;

    @NotBlank
    private String direccion;

    @NotEmpty
    @Email
    @Column(nullable = false,length = 100, unique = true)
    private String email;

    private String password;

    @Transient
    private String password1;

    @Transient
    private String password2;


    @Enumerated(EnumType.STRING)
    @NotNull(message = "Debe seleccionar un rol")
    private Rol rol;

    public enum Rol {
        ADMIN,
        CLIENTE

    }

}