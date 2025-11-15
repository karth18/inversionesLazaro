package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
@SuppressWarnings("unused")
@Data
@Entity
@Table(name = "usuarios")
public class Usuario {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idusuario")
    private Integer id;

    @NotBlank
    @Column(nullable = false, length = 11)
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

    private boolean estado;

    @NotNull(message = "{politicasNoAceptadas.usuario.politica}")
    private Boolean politica;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_act")
    private LocalDateTime fechaActualizacion;

    @Column(name = "usuario_modificacion", length = 100)
    private String usuarioModificacion;

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
    @Column(length = 20)
    private Rol rol;

    public enum Rol {
        ADMIN,
        CLIENTE,
        DESPACHO,
        MARKETING

    }

    @PrePersist
    protected void onCreate(){
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        this.fechaActualizacion = LocalDateTime.now();
    }

    //validacion de correo electronico

    private String tokenVerificacion;

    private LocalDateTime fechaExpiracionToken;

}