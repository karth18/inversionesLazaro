package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.com.isil.inversioneslazaro.dto.DireccionDTO;
import pe.com.isil.inversioneslazaro.model.*;
import pe.com.isil.inversioneslazaro.repository.*;
import pe.com.isil.inversioneslazaro.service.UsuarioService;
import pe.com.isil.inversioneslazaro.dto.DistritoDTO;
import pe.com.isil.inversioneslazaro.dto.ProvinciaDTO;
import java.util.stream.Collectors;


import java.util.List;

@RestController
@RequestMapping("/api/direccion")
public class DireccionController {

    @Autowired private ProvinciaRepository provinciaRepository;
    @Autowired private DistritoRepository distritoRepository;
    @Autowired private DireccionRepository direccionRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    // @Autowired private UsuarioService usuarioService; // Si necesitas lógica extra de usuario
    @Autowired private UsuarioRepository usuarioRepository; // Para buscar por email si es necesario

    @GetMapping("/provincias/{departamentoId}")
    public List<ProvinciaDTO> getProvinciasPorDepartamento(@PathVariable Long departamentoId) {
        // Usa el método del repositorio que busca por ID de Departamento
        return provinciaRepository.findByDepartamento_IdOrderByNombreAsc(departamentoId)
                .stream()
                .map(ProvinciaDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/distritos/{provinciaId}")
    public List<DistritoDTO> getDistritosPorProvincia(@PathVariable Long provinciaId) {
        // Usa el método del repositorio que busca por ID de Provincia
        return distritoRepository.findByProvincia_IdOrderByNombreAsc(provinciaId)
                .stream()
                .map(DistritoDTO::new)
                .collect(Collectors.toList());
    }

    // Endpoint para guardar la nueva dirección desde el modal
//    @PostMapping("/nueva")
//    public Direccion guardarNuevaDireccion(@RequestBody Direccion direccionRequest,
//                                           @AuthenticationPrincipal UserDetails userDetails) { // <-- Inyectar usuario logueado
//
//        // 1. Obtener el Usuario Autenticado Completo
//        // Si userDetails es tu propia clase Usuario que implementa UserDetails, puedes castear.
//        // Si no, necesitas buscarlo usando el username (email en tu caso).
//        Usuario usuarioActual = usuarioRepository.findByEmail(userDetails.getUsername())
//                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en la base de datos"));
//
//        direccionRequest.setUsuario(usuarioActual); // Asignar el usuario real
//
//        // 2. Mapear IDs a Entidades (la lógica existente está bien)
//        Departamento dep = departamentoRepository.findById(direccionRequest.getDepartamento().getId())
//                .orElseThrow(() -> new RuntimeException("Departamento no encontrado"));
//        Provincia prov = provinciaRepository.findById(direccionRequest.getProvincia().getId())
//                .orElseThrow(() -> new RuntimeException("Provincia no encontrada"));
//        Distrito dist = distritoRepository.findById(direccionRequest.getDistrito().getId())
//                .orElseThrow(() -> new RuntimeException("Distrito no encontrado"));
//
//        direccionRequest.setDepartamento(dep);
//        direccionRequest.setProvincia(prov);
//        direccionRequest.setDistrito(dist);
//
//        // 3. Lógica Adicional (Desmarcar otras principales)
//        if (direccionRequest.isEsPrincipal()) {
//            // Implementa este método en tu DireccionRepository si lo necesitas
//            // direccionRepository.desmarcarOtrasPrincipales(usuarioActual.getId());
//            List<Direccion> direccionesActuales = direccionRepository.findByUsuarioId(usuarioActual.getId());
//            for(Direccion dir : direccionesActuales) {
//                if(dir.isEsPrincipal()) {
//                    dir.setEsPrincipal(false);
//                    direccionRepository.save(dir); // Guarda el cambio
//                }
//            }
//        }
//
//        // 4. Guardar la nueva dirección
//        return direccionRepository.save(direccionRequest);
//    }

    /**
     * GET /api/direccion/{id}
     * Obtiene una dirección específica.
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<DireccionDTO> getDireccionPorId(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuarioActual = getUsuarioActual(userDetails);
        Direccion direccion = direccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));

        // Seguridad: Asegurar que la dirección pertenezca al usuario logueado
        if (!direccion.getUsuario().getId().equals(usuarioActual.getId())) {
            throw new AccessDeniedException("No tienes permiso para ver esta dirección");
        }

        return ResponseEntity.ok(new DireccionDTO(direccion));
    }

    /**
     * POST /api/direccion/nueva
     * Guarda una dirección nueva.
     */
    @PostMapping("/nueva")
    @Transactional // Importante para la lógica de "esPrincipal"
    public ResponseEntity<DireccionDTO> guardarNuevaDireccion(@RequestBody Direccion direccionRequest,
                                                           @AuthenticationPrincipal UserDetails userDetails) {

        Usuario usuarioActual = getUsuarioActual(userDetails);
        direccionRequest.setUsuario(usuarioActual);

        // Si esta nueva dirección se marca como principal, desmarcamos todas las demás primero.
        if (direccionRequest.isEsPrincipal()) {
            direccionRepository.desmarcarTodasPrincipales(usuarioActual.getId());
        }

        // Asignamos las entidades completas (el request solo trae IDs)
        asignarEntidadesUbigeo(direccionRequest);

        Direccion direccionGuardada = direccionRepository.save(direccionRequest);
        return new ResponseEntity<>(new DireccionDTO(direccionGuardada), HttpStatus.CREATED);
    }

    /**
     * PUT /api/direccion/{id}
     * Actualiza una dirección existente.
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<DireccionDTO> actualizarDireccion(@PathVariable Long id,
                                                         @RequestBody Direccion direccionRequest,
                                                         @AuthenticationPrincipal UserDetails userDetails) {

        Usuario usuarioActual = getUsuarioActual(userDetails);

        // 1. Verificar que la dirección exista y pertenezca al usuario
        Direccion direccionExistente = direccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));

        if (!direccionExistente.getUsuario().getId().equals(usuarioActual.getId())) {
            throw new AccessDeniedException("No tienes permiso para modificar esta dirección");
        }

        // 2. Lógica de "esPrincipal"
        if (direccionRequest.isEsPrincipal()) {
            direccionRepository.desmarcarTodasPrincipales(usuarioActual.getId());
        }

        // 3. Actualizar los campos
        direccionExistente.setCalleAvenida(direccionRequest.getCalleAvenida());
        direccionExistente.setNumeroCalle(direccionRequest.getNumeroCalle());
        direccionExistente.setDptoInterior(direccionRequest.getDptoInterior());
        direccionExistente.setEsPrincipal(direccionRequest.isEsPrincipal());

        // Asignar nuevas entidades de Ubigeo
        asignarEntidadesUbigeo(direccionExistente, direccionRequest);

        Direccion direccionActualizada = direccionRepository.save(direccionExistente);
        return ResponseEntity.ok(new DireccionDTO(direccionActualizada));
    }

    /**
     * DELETE /api/direccion/{id}
     * Elimina una dirección.
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> eliminarDireccion(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {

        Usuario usuarioActual = getUsuarioActual(userDetails);

        // 1. Verificar que la dirección exista y pertenezca al usuario
        Direccion direccion = direccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));

        if (!direccion.getUsuario().getId().equals(usuarioActual.getId())) {
            throw new AccessDeniedException("No tienes permiso para eliminar esta dirección");
        }

        // 2. Lógica de eliminación (¿Qué pasa si borra la principal?)
        // Opcional: Si borra la principal, podrías asignar otra como principal.
        // Por ahora, simplemente la borramos.

        direccionRepository.delete(direccion);
        return ResponseEntity.ok().build(); // HTTP 200 OK (sin contenido)
    }

    // --- Métodos de Ayuda ---

    /**
     * Obtiene el objeto Usuario completo desde la base de datos.
     */
    private Usuario getUsuarioActual(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en la base de datos"));
    }

    /**
     * Asigna las entidades completas de Departamento, Provincia y Distrito
     * a una dirección, basado en los IDs que vienen en el request.
     * (Para creación)
     */
    private void asignarEntidadesUbigeo(Direccion direccion) {
        Departamento dep = departamentoRepository.findById(direccion.getDepartamento().getId())
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado"));
        Provincia prov = provinciaRepository.findById(direccion.getProvincia().getId())
                .orElseThrow(() -> new RuntimeException("Provincia no encontrada"));
        Distrito dist = distritoRepository.findById(direccion.getDistrito().getId())
                .orElseThrow(() -> new RuntimeException("Distrito no encontrado"));

        direccion.setDepartamento(dep);
        direccion.setProvincia(prov);
        direccion.setDistrito(dist);
    }

    /**
     * Sobrecarga para actualizar una entidad existente con datos de un request.
     * (Para actualización)
     */
    private void asignarEntidadesUbigeo(Direccion dirExistente, Direccion dirRequest) {
        Departamento dep = departamentoRepository.findById(dirRequest.getDepartamento().getId())
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado"));
        Provincia prov = provinciaRepository.findById(dirRequest.getProvincia().getId())
                .orElseThrow(() -> new RuntimeException("Provincia no encontrada"));
        Distrito dist = distritoRepository.findById(dirRequest.getDistrito().getId())
                .orElseThrow(() -> new RuntimeException("Distrito no encontrado"));

        dirExistente.setDepartamento(dep);
        dirExistente.setProvincia(prov);
        dirExistente.setDistrito(dist);
    }

}
