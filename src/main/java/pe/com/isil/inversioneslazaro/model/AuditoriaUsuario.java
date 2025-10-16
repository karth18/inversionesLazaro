package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "auditoriaUsuario")
public class AuditoriaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Usuario afectado (el que fue editado)
    @ManyToOne
    @JoinColumn(name = "usuario_afectado_id")
    private Usuario usuarioAfectado;

    // Usuario que realizó la acción (admin o cliente)
    @Column(name = "realizado_por", length = 100)
    private String realizadoPor;

    // Tipo de acción (CREACIÓN, EDICIÓN, ELIMINACIÓN)
    @Column(name = "accion", length = 20)
    private String accion;

    @Column(name = "fecha_accion")
    private LocalDateTime fechaAccion;

    @PrePersist
    protected void onCreate() {
        this.fechaAccion = LocalDateTime.now();
    }
}

