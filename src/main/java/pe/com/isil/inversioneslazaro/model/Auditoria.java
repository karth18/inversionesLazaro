package pe.com.isil.inversioneslazaro.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@SuppressWarnings("unused")
@Data
@Entity
@Table(name = "auditoriaGeneral")
@NoArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entidadAfectada", nullable = false, length = 100)
    private String entidadAfectada; // Ej: "Producto", "Usuario", "Categoria"

    @Column(name = "entidadId", nullable = false, length = 50)
    private String entidadId; // El ID del producto/usuario (lo guardamos como String)

    @Column(name = "realizadoPor", length = 100, nullable = false)
    private String realizadoPor; // El username/email de quien hizo la acción

    @Enumerated(EnumType.STRING) // Usar un Enum es más limpio
    @Column(name = "accion", length = 20, nullable = false)
    private AccionAuditoria accion;

    @Column(name = "fecha_accion", updatable = false)
    private LocalDateTime fechaAccion;

    @PrePersist
    protected void onCreate() {
        this.fechaAccion = LocalDateTime.now();
    }

    // Constructor conveniente
    public Auditoria(String entidadAfectada, String entidadId, String realizadoPor, AccionAuditoria accion) {
        this.entidadAfectada = entidadAfectada;
        this.entidadId = entidadId;
        this.realizadoPor = realizadoPor;
        this.accion = accion;
    }

    // Enum para estandarizar las acciones
    public enum AccionAuditoria {
        CREAR,
        ACTUALIZAR,
        ELIMINAR,
        HABILITAR
    }
}

