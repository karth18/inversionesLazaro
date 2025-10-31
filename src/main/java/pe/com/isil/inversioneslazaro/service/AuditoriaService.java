package pe.com.isil.inversioneslazaro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.com.isil.inversioneslazaro.model.Auditoria;
import pe.com.isil.inversioneslazaro.repository.AuditoriaRepository;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    /**
     * Este es el "pequeño código" universal que llamarás desde tus CRUDs.
     *
     * @param realizadoPor    El email/username del usuario que hace la acción.
     * @param entidadAfectada El nombre de la tabla (ej: "Producto").
     * @param entidadId       El ID del objeto afectado (ej: "123").
     * @param accion          La acción (CREAR, ACTUALIZAR, ELIMINAR).
     */
    public void registrarAccion(String realizadoPor, String entidadAfectada, String entidadId, Auditoria.AccionAuditoria accion) {
        try {
            Auditoria log = new Auditoria(entidadAfectada, entidadId, realizadoPor, accion);
            auditoriaRepository.save(log);
        } catch (Exception e) {
            // Manejar la excepción (ej. loggear un error)
            // No queremos que un fallo en la auditoría detenga la acción principal.
            System.err.println("Error al registrar auditoría: " + e.getMessage());
        }
    }

    // Sobrecarga conveniente para IDs numéricos (Long, Integer)
    public void registrarAccion(String realizadoPor, String entidadAfectada, Object entidadId, Auditoria.AccionAuditoria accion) {
        registrarAccion(realizadoPor, entidadAfectada, String.valueOf(entidadId), accion);
    }
}