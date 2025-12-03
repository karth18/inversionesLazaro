package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    @Column(length = 9)
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
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])[^=\\(\\)/\\\\*\\s]{8,}$",
            message = "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula y un número. No se permiten espacios ni los símbolos: = ( ) / \\ *"
    )
    private String password1;

    @Transient
    private String password2;


    @ElementCollection(fetch = FetchType.EAGER) // EAGER para que cargue los roles al iniciar sesión
    @CollectionTable(
            name = "usuario_roles", // Nombre de la nueva tabla en BD
            joinColumns = @JoinColumn(name = "usuario_id") // Llave foránea
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "rol") // Nombre de la columna en la tabla nueva
    private Set<Rol> roles = new HashSet<>();

    public enum Rol {
        ADMIN,
        CLIENTE,
        MARKETING,
        VENTAS,
        TALLER,
        ALMACEN,    // Nuevo: El que empaqueta
        DESPACHO,   // Nuevo: El jefe que asigna rutas
        CHOFER      // Nuevo: El que entrega


    }
    // Helper para agregar roles fácilmente
    public void agregarRol(Rol rol) {
        this.roles.add(rol);
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